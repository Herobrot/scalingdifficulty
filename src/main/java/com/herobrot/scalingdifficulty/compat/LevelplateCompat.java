package com.herobrot.scalingdifficulty.compat;

import com.herobrot.levelplate.Levelplate;
import com.herobrot.levelplate.data.LevelplateAttachments;
import com.herobrot.scalingdifficulty.data.ModAttachments;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

public class LevelplateCompat {
    public static int getMobLevel(Mob mob) {
        LevelplateAttachments.MobLevelData data = mob.getData(LevelplateAttachments.MOB_DATA);

        if (data.level > 1) {
            return data.level;
        }

        int levelMultiplier = Levelplate.CONFIG.levelMultiplier;

        if (Levelplate.CONFIG.useScalingDifficultyLvl) {
            float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);
            if (multiplier <= 0.0f) multiplier = 1.0f;
            int level = (int) (levelMultiplier * multiplier - levelMultiplier);
            return Math.max(level, 1);
        }

        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> type = (EntityType<? extends LivingEntity>) mob.getType();
        if (DefaultAttributes.hasSupplier(type)) {
            double baseMaxHealth = DefaultAttributes.getSupplier(type).getBaseValue(Attributes.MAX_HEALTH);
            if (baseMaxHealth > 0) {
                int level = (int) (levelMultiplier * mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / baseMaxHealth)
                        - levelMultiplier + 1;
                return Math.max(level, 1);
            }
        }

        return 1;
    }
    public static int getLevelFromMultiplier(float multiplier) {
        int levelMultiplier = Levelplate.CONFIG.levelMultiplier;
        if (multiplier <= 0.0f) multiplier = 1.0f;
        int level = (int) (levelMultiplier * multiplier - levelMultiplier);
        return Math.max(level, 1);
    }
}