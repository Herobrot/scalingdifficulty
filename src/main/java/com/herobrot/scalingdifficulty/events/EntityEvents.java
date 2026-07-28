package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
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
        if (event.getEntity() instanceof Mob mob) {
            int newXp = DifficultyCalculator.scaleExperience(mob, event.getDroppedExperience());
            event.setDroppedExperience(newXp);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Mob mob) {
            float newDamage = DifficultyCalculator.scaleDamage(mob, event.getAmount(), source);
            if (newDamage != event.getAmount()) {
                event.setAmount(newDamage);
            }
        }
    }
}