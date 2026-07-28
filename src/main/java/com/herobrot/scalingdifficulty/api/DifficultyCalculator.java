package com.herobrot.scalingdifficulty.api;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.compat.LevelplateCompat;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

public class DifficultyCalculator {

    public static final long TICKS_PER_MINUTE = 1200L;

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
            long time = worldTime - (settings.startingTime * TICKS_PER_MINUTE);
            if (time > 0) {
                if (!isBoss && config.excludeTimeInOtherDimension && level.dimension() != Level.OVERWORLD) time = 0;
                factor += ((int) (time / (settings.increasingTime * TICKS_PER_MINUTE))) * (isBoss ? config.bossTimeFactor : settings.timeFactor);
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
            double baseHealth = mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
            // Guardia para prevenir división por cero o valores nulos
            if (baseHealth > 0) {
                if (level.random.nextFloat() < (config.speedZombieChance / 100f)) {
                    mobHealthFactor -= (config.speedZombieMalusLifePoints / baseHealth);
                    mobSpeedFactor *= config.speedZombieSpeedFactor;
                    mobSpeedFactor = Math.min(mobSpeedFactor, settings.maxFactorSpeed);
                    mob.setData(ModAttachments.SPEEDY_ZOMBIE, true);
                    AttributeHandler.applyModifier(mob, Attributes.SCALE, AttributeHandler.SCALE_MOD_ID, 0.85);
                } else if (level.random.nextFloat() < (config.bigZombieChance / 100f)) {
                    mobSpeedFactor *= config.bigZombieSlownessFactor;
                    double healthBonusFactor = config.bigZombieBonusLifePoints / baseHealth;
                    double baseDamage = mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                    double damageBonusFactor = baseDamage > 0 ? (config.bigZombieBonusDamage / baseDamage) : 0.0;

                    mobHealthFactor += healthBonusFactor;
                    mobDamageFactor += damageBonusFactor;
                    mob.setData(ModAttachments.BIG_ZOMBIE, true);
                    AttributeHandler.applyModifier(mob, Attributes.SCALE, AttributeHandler.SCALE_MOD_ID, config.bigZombieSize);
                }
            }
        }

        AttributeHandler.applyModifier(mob, Attributes.MAX_HEALTH, AttributeHandler.HEALTH_MOD_ID, mobHealthFactor);
        AttributeHandler.applyModifier(mob, Attributes.ATTACK_DAMAGE, AttributeHandler.DAMAGE_MOD_ID, mobDamageFactor);
        AttributeHandler.applyModifier(mob, Attributes.ARMOR, AttributeHandler.ARMOR_MOD_ID, mobProtectionFactor);
        AttributeHandler.applyModifier(mob, Attributes.MOVEMENT_SPEED, AttributeHandler.SPEED_MOD_ID, mobSpeedFactor);
    }

    @SuppressWarnings("resource")
    public static void dropMoreLoot(Mob mob, LootTable lootTable, LootParams lootParams) {
        if (!ScalingDifficulty.CONFIG.dropMoreLoot) return;
        float multiplier;
        if (ScalingDifficulty.isLevelplateLoaded) {
            int mobLevel = LevelplateCompat.getMobLevel(mob);
            if (mobLevel <= 1) return;
            multiplier = mobLevel;
        } else {
            multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
            if (multiplier <= 1.0f) return;
        }
        float dropChance = multiplier * ScalingDifficulty.CONFIG.moreLootChance;
        dropChance = Math.min(dropChance, ScalingDifficulty.CONFIG.maxLootChance);

        if (mob.level().random.nextFloat() <= dropChance) {
            lootTable.getRandomItems(lootParams, mob.getLootTableSeed(), stack -> {
                if (!stack.isEmpty() && mob.level().random.nextFloat() <= ScalingDifficulty.CONFIG.chanceForEachItem) {
                    mob.spawnAtLocation(stack);
                }
            });
        }
    }

    public static float calculateDropChance(Mob mob) {
        float multiplier;

        if (ScalingDifficulty.isLevelplateLoaded) {
            int mobLevel = LevelplateCompat.getMobLevel(mob);
            if (mobLevel <= 1) return 0.0f;
            multiplier = mobLevel;
        } else {
            multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
            if (multiplier <= 1.0f) return 0.0f;
        }

        float dropChance = multiplier * ScalingDifficulty.CONFIG.moreLootChance;
        return Math.min(dropChance, ScalingDifficulty.CONFIG.maxLootChance);
    }
}