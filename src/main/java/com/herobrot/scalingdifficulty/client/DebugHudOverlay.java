package com.herobrot.scalingdifficulty.client;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.DimensionDifficultyLoader;
import com.herobrot.scalingdifficulty.data.DimensionSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class DebugHudOverlay {

    public static void renderDebugHud(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (client.options.hideGui || !ScalingDifficulty.CONFIG.hudTesting || player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();
        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;

        int x = 10;
        int y = 10;
        int step = client.font.lineHeight + 2;
        int color = 0xFFFFFF;

        graphics.drawString(client.font, "§lScalingDifficulty Debug§r", x, y, 0xFFAA00, true);
        y += step;

        if (client.hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof Mob mob) {
            renderEntityInfo(graphics, client, mob, config, x, y, step, color);
        } else {
            renderWorldInfo(graphics, client, player, config, x, y, step, color);
        }
    }

    private static void renderEntityInfo(GuiGraphics graphics, Minecraft client, Mob mob, ScalingDifficultyConfig config, int x, int y, int step, int color) {
        double maxHealth = mob.getAttributes().hasAttribute(Attributes.MAX_HEALTH) ? mob.getAttributeValue(Attributes.MAX_HEALTH) : 0.0;
        double baseHealth = mob.getAttributes().hasAttribute(Attributes.MAX_HEALTH) ? mob.getAttributeBaseValue(Attributes.MAX_HEALTH) : 1.0;
        double baseDamage = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE) ? mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) : 0.0;
        double baseArmor = mob.getAttributes().hasAttribute(Attributes.ARMOR) ? mob.getAttributeBaseValue(Attributes.ARMOR) : 0.0;
        double scale = mob.getAttributes().hasAttribute(Attributes.SCALE) ? mob.getAttributeValue(Attributes.SCALE) : 1.0;

        float rawMultiplier = (float) (maxHealth / baseHealth);
        float baseMultiplier = rawMultiplier;
        float damageMultiplier = rawMultiplier;

        if (scale > 1.0f && baseHealth > 0) {
            double healthBonus = config.bigZombieBonusLifePoints / baseHealth;
            baseMultiplier = (float) (rawMultiplier - healthBonus);
            double damageBonus = baseDamage > 0 ? (config.bigZombieBonusDamage / baseDamage) : 0;
            damageMultiplier = (float) (baseMultiplier + damageBonus);
        } else if (scale < 1.0f && baseHealth > 0) {
            double healthMalus = config.speedZombieMalusLifePoints / baseHealth;
            baseMultiplier = (float) (rawMultiplier + healthMalus);
            damageMultiplier = baseMultiplier;
        }

        damageMultiplier = (float) Math.min(damageMultiplier, config.maxFactorDamage);
        float armorMultiplier = (float) Math.min(baseMultiplier, config.maxFactorProtection);

        double displayDamage = baseDamage * damageMultiplier;
        double displayArmor = baseArmor * armorMultiplier;
        String entityName = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();

        graphics.drawString(client.font, "Target: §e" + entityName, x, y, color, true); y += step;
        graphics.drawString(client.font, "Active Multiplier: §a" + String.format("%.2f", baseMultiplier) + "x", x, y, color, true); y += step;
        graphics.drawString(client.font, "Health: §c" + String.format("%.1f", mob.getHealth()) + " / " + String.format("%.1f", maxHealth) + " §8(Max Cap: " + config.maxFactorHealth + "x)", x, y, color, true); y += step;

        if (baseDamage > 0) {
            graphics.drawString(client.font, "Damage: §c" + String.format("%.1f", displayDamage) + " §8(Max Cap: " + config.maxFactorDamage + "x)", x, y, color, true);
        } else {
            graphics.drawString(client.font, "Damage: §7[No Base Damage]", x, y, color, true);
        }
        y += step;

        if (baseArmor > 0) {
            graphics.drawString(client.font, "Armor: §b" + String.format("%.1f", displayArmor) + " §8(Max Cap: " + config.maxFactorProtection + "x)", x, y, color, true); y += step;
        }

        if (scale > 1.0) {
            graphics.drawString(client.font, "§6[SPECIAL VARIANT: BIG ZOMBIE]", x, y, color, true);
        } else if (scale < 1.0) {
            graphics.drawString(client.font, "§b[SPECIAL VARIANT: SPEEDY ZOMBIE]", x, y, color, true);
        }
    }

    @SuppressWarnings("resource")
    private static void renderWorldInfo(GuiGraphics graphics, Minecraft client, Player player, ScalingDifficultyConfig config, int x, int y, int step, int color) {
        Level level = player.level();
        String dimensionKey = level.dimension().location().toString();
        DimensionSettings settings = DimensionDifficultyLoader.getSettings(dimensionKey);
        BlockPos spawnPos = level.getSharedSpawnPos();

        int spawnX = settings.distanceCoordinatesX != null ? settings.distanceCoordinatesX : spawnPos.getX();
        int spawnZ = settings.distanceCoordinatesZ != null ? settings.distanceCoordinatesZ : spawnPos.getZ();

        float distance = Mth.sqrt((float) player.distanceToSqr(spawnX, player.getY(), spawnZ));
        long worldTime = level.getDayTime();

        float distElapsed = distance - settings.startingDistance;
        int distDivided = distElapsed > 0 ? (int) (distElapsed / settings.increasingDistance) : 0;
        if (config.excludeDistanceInOtherDimension && level.dimension() != Level.OVERWORLD) distDivided = 0;
        double distBonus = distDivided * settings.distanceFactor;

        long timeElapsed = worldTime - (settings.startingTime * DifficultyCalculator.TICKS_PER_MINUTE);
        int timeDivided = timeElapsed > 0 ? (int) (timeElapsed / (settings.increasingTime * DifficultyCalculator.TICKS_PER_MINUTE)) : 0;
        if (config.excludeTimeInOtherDimension && level.dimension() != Level.OVERWORLD) timeDivided = 0;
        double timeBonus = timeDivided * settings.timeFactor;

        int spawnHeightDivided = (Mth.floor(player.getY()) - settings.startingHeight) / settings.heightDistance;
        if (!settings.positiveHeightIncrement && spawnHeightDivided > 0) spawnHeightDivided = 0;
        if (!settings.negativeHeightIncrement && spawnHeightDivided < 0) spawnHeightDivided = 0;
        if (config.excludeHeightInOtherDimension && level.dimension() != Level.OVERWORLD) spawnHeightDivided = 0;
        double heightBonus = Math.abs(spawnHeightDivided) * settings.heightFactor;

        double totalFactor = settings.startingFactor + distBonus + timeBonus + heightBonus;
        totalFactor = Math.min(totalFactor, settings.maxFactorHealth);

        graphics.drawString(client.font, "§7[Live Global Tracker - " + dimensionKey + "]", x, y, color, true); y += step;
        graphics.drawString(client.font, "Distance: §7" + (int)distance + "m §8-> §a+" + String.format("%.2f", distBonus), x, y, color, true); y += step;
        graphics.drawString(client.font, "Time: §7" + (worldTime / DifficultyCalculator.TICKS_PER_MINUTE) + " minutes §8-> §a+" + String.format("%.2f", timeBonus), x, y, color, true); y += step;
        graphics.drawString(client.font, "Height: §7" + (int)player.getY() + " Y §8-> §a+" + String.format("%.2f", heightBonus), x, y, color, true); y += step;
        graphics.drawString(client.font, "Est. Next Spawn Factor: §e" + String.format("%.2f", totalFactor) + "x", x, y, color, true);
    }
}