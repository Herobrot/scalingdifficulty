package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.compat.LevelplateCompat;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID)
public class EntityEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Mob mob) {
            boolean isProcessed = mob.getData(ModAttachments.PROCESSED);
            if (isProcessed) return;

            mob.setData(ModAttachments.PROCESSED, true);
            DifficultyCalculator.applyScaling(mob, (ServerLevel) event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (!ScalingDifficulty.CONFIG.extraXp) return;
        if (event.getEntity() instanceof Mob mob) {
            float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
            if (multiplier > 1.0f) {
                float cappedMultiplier = Math.min(multiplier, ScalingDifficulty.CONFIG.maxXPFactor);
                event.setDroppedExperience((int) (event.getDroppedExperience() * cappedMultiplier));
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!ScalingDifficulty.CONFIG.dropMoreLoot) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        float dropChance;

        if (ScalingDifficulty.isLevelplateLoaded) {
            int mobLevel = LevelplateCompat.getMobLevel(mob);
            if (mobLevel <= 1) return;
            dropChance = mobLevel * ScalingDifficulty.CONFIG.moreLootChance;
        } else {
            float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
            if (multiplier <= 1.0f) return;
            int simulatedLevel = (int) (10 * multiplier - 10);
            if (simulatedLevel < 1) simulatedLevel = 1;
            dropChance = simulatedLevel * ScalingDifficulty.CONFIG.moreLootChance;
        }

        dropChance = Math.min(dropChance, ScalingDifficulty.CONFIG.maxLootChance);

        if (mob.level().random.nextFloat() <= dropChance) {
            for (ItemEntity drop : event.getDrops()) {
                if (mob.level().random.nextFloat() >= ScalingDifficulty.CONFIG.chanceForEachItem) {
                    ItemStack stack = drop.getItem();
                    int bonus = (int) (stack.getCount() * dropChance);
                    if (bonus > 0) {
                        stack.grow(bonus);
                        drop.setItem(stack);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (attacker instanceof Mob mob) {
            boolean isIndirectOrSpecial = directEntity != mob ||
                    source.is(DamageTypeTags.IS_PROJECTILE) ||
                    source.is(DamageTypeTags.IS_EXPLOSION) ||
                    mob instanceof EnderDragon ||
                    mob instanceof Guardian;

            if (isIndirectOrSpecial) {
                float damageFactor = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);

                if (mob instanceof Creeper) {
                    damageFactor *= (float) ScalingDifficulty.CONFIG.creeperExplosionFactor;
                }

                if (damageFactor > 1.0f) {
                    event.setAmount(event.getAmount() * damageFactor);
                }
            }
        }
    }
}