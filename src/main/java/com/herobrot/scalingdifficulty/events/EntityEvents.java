package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Guardian;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID)
public class EntityEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Mob mob && event.getLevel() instanceof ServerLevel serverLevel) {
            boolean isProcessed = mob.getData(ModAttachments.PROCESSED);
            if (isProcessed) return;
            mob.setData(ModAttachments.PROCESSED, true);
            DifficultyCalculator.applyScaling(mob, serverLevel);
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

                if (mob instanceof Creeper)
                    damageFactor *= (float) ScalingDifficulty.CONFIG.creeperExplosionFactor;

                if (damageFactor > 1.0f)
                    event.setAmount(event.getAmount() * damageFactor);
            }
        }
    }
}