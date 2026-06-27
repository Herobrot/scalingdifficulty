package com.herobrot.scalingdifficulty.api;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.DimensionDifficultyLoader;
import com.herobrot.scalingdifficulty.data.DimensionSettings;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import com.herobrot.scalingdifficulty.util.EntityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class DifficultyCalculator {
    public static float calculateRawMultiplier(ServerLevel level, BlockPos pos, boolean isBoss) {
        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;
        String dimensionKey = level.dimension().location().toString();
        DimensionSettings settings = DimensionDifficultyLoader.getSettings(dimensionKey);

        double factor = settings.startingFactor;

        BlockPos spawnPos = level.getSharedSpawnPos();
        int spawnX = settings.distanceCoordinatesX != null ? settings.distanceCoordinatesX : spawnPos.getX();
        int spawnZ = settings.distanceCoordinatesZ != null ? settings.distanceCoordinatesZ : spawnPos.getZ();

        float worldSpawnDistance = Mth.sqrt((float) pos.distToCenterSqr(spawnX, pos.getY(), spawnZ));
        long worldTime = level.getDayTime();
        int spawnHeight = pos.getY();
        if (settings.increasingDistance != 0) {
            float distance = worldSpawnDistance - settings.startingDistance;
            if (distance > 0) {
                if (!isBoss && config.excludeDistanceInOtherDimension && level.dimension() != Level.OVERWORLD) distance = 0;
                factor += ((double) (int) distance / settings.increasingDistance) * (isBoss ? config.bossDistanceFactor : settings.distanceFactor);
            }
        }
        if (settings.increasingTime != 0) {
            long time = worldTime - (settings.startingTime * 1200L);
            if (time > 0) {
                if (!isBoss && config.excludeTimeInOtherDimension && level.dimension() != Level.OVERWORLD) time = 0;
                factor += ((int) (time / (settings.increasingTime * 1200L))) * (isBoss ? config.bossTimeFactor : settings.timeFactor);
            }
        }
        if (!isBoss && settings.heightDistance != 0) {
            int spawnHeightDivided = (spawnHeight - settings.startingHeight) / settings.heightDistance;
            if (!settings.positiveHeightIncrement && spawnHeightDivided > 0) spawnHeightDivided = 0;
            if (!settings.negativeHeightIncrement && spawnHeightDivided < 0) spawnHeightDivided = 0;
            if (config.excludeHeightInOtherDimension && level.dimension() != Level.OVERWORLD) spawnHeightDivided = 0;
            factor += Math.abs(spawnHeightDivided) * settings.heightFactor;
        }
        return (float) factor;
    }

    public static void applyScaling(Mob mob, ServerLevel level) {
        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;

        String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (config.excludedEntity.contains(entityName)) return;

        boolean isBoss = mob.getType().is(EntityTags.BOSSES);
        if (isBoss && !config.affectBosses) return;
        if (mob.isBaby() && !config.affectAnimalBabies) return;

        DimensionSettings settings = DimensionDifficultyLoader.getSettings(level.dimension().location().toString());

        float rawMultiplier = calculateRawMultiplier(level, mob.blockPosition(), isBoss);
        if (isBoss && config.dynamicBossModification) {
            int playersNearby = 0;
            double radiusSqr = config.bossDistance * config.bossDistance;
            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator() && player.distanceToSqr(mob) <= radiusSqr) {
                    playersNearby++;
                }
            }
            if (playersNearby > 1) {
                rawMultiplier += (float) ((playersNearby - 1) * config.dynamicBossModificator);
            }
        }
        double mobHealthFactor = Math.min(rawMultiplier, isBoss ? config.bossMaxFactor : settings.maxFactorHealth);
        double mobDamageFactor = Math.min(rawMultiplier, settings.maxFactorDamage);
        double mobProtectionFactor = Math.min(rawMultiplier, settings.maxFactorProtection);
        double mobSpeedFactor = 1.0D;

        if (config.allowRandomValues && level.random.nextFloat() <= (config.randomChance / 100f)) {
            float rFactor = config.randomFactor / 100f;
            double randomModifier = 1.0 - rFactor + (level.random.nextDouble() * rFactor * 2f);
            mobHealthFactor *= randomModifier;
            mobDamageFactor *= randomModifier;
        }
        mobHealthFactor = Math.round(mobHealthFactor * 100.0) / 100.0;
        mobDamageFactor = Math.round(mobDamageFactor * 100.0) / 100.0;
        mobProtectionFactor = Math.round(mobProtectionFactor * 100.0) / 100.0;

        mob.setData(ModAttachments.DIFFICULTY_MULTIPLIER, (float) mobHealthFactor);
        if (config.allowSpecialZombie && !mob.isBaby() && mob instanceof Zombie) {
            if (level.random.nextFloat() < (config.speedZombieChance / 100f)) {
                mobHealthFactor -= (config.speedZombieMalusLifePoints / mob.getAttributeBaseValue(Attributes.MAX_HEALTH));
                mobSpeedFactor *= config.speedZombieSpeedFactor;
                mobSpeedFactor = Math.min(mobSpeedFactor, settings.maxFactorSpeed);
                mob.setData(ModAttachments.SPEEDY_ZOMBIE, true);
                AttributeHandler.applyModifier(mob, Attributes.SCALE, AttributeHandler.SCALE_MOD_ID, 0.85);
            } else if (level.random.nextFloat() < (config.bigZombieChance / 100f)) {
                mobSpeedFactor *= config.bigZombieSlownessFactor;
                double healthBonusFactor = config.bigZombieBonusLifePoints / mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
                double damageBonusFactor = config.bigZombieBonusDamage / mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                mobHealthFactor += healthBonusFactor;
                mobDamageFactor += damageBonusFactor;
                mob.setData(ModAttachments.BIG_ZOMBIE, true);
                AttributeHandler.applyModifier(mob, Attributes.SCALE, AttributeHandler.SCALE_MOD_ID, config.bigZombieSize);
            }
        }
        AttributeHandler.applyModifier(mob, Attributes.MAX_HEALTH, AttributeHandler.HEALTH_MOD_ID, mobHealthFactor);
        if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            AttributeHandler.applyModifier(mob, Attributes.ATTACK_DAMAGE, AttributeHandler.DAMAGE_MOD_ID, mobDamageFactor);
        }
        if (mob.getAttributes().hasAttribute(Attributes.ARMOR)) {
            AttributeHandler.applyModifier(mob, Attributes.ARMOR, AttributeHandler.ARMOR_MOD_ID, mobProtectionFactor);
        }
        if (mob.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            AttributeHandler.applyModifier(mob, Attributes.MOVEMENT_SPEED, AttributeHandler.SPEED_MOD_ID, mobSpeedFactor);
        }
    }
}