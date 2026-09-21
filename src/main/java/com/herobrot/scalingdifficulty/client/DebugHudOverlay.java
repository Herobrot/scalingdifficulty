package com.herobrot.scalingdifficulty.client;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import com.herobrot.scalingdifficulty.compat.HerosLevelsCompat;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.DimensionDifficultyLoader;
import com.herobrot.scalingdifficulty.data.DimensionSettings;
import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import com.herobrot.scalingdifficulty.zone.ClientZoneTracker;
import com.herobrot.scalingdifficulty.zone.DifficultyZone;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.HashMap;
import java.util.Map;

import static com.herobrot.scalingdifficulty.zone.DifficultyZone.formatCoords;

public class DebugHudOverlay {

    private static final int WHITE = 0xFFFFFF;
    private static final int GRAY = 0xAAAAAA;
    private static final int DARK_GRAY = 0x404040;
    private static final int YELLOW = 0xFFFF55;
    private static final int GREEN = 0x55FF55;
    private static final int RED = 0xFF5555;
    private static final int AQUA = 0x55FFFF;
    private static final int GOLD = 0xFFAA55;

    private static final Map<String, Component> TRACKER_LABELS = new HashMap<>();

    private static ZoneSyncPayload.ZoneEntry cachedZone;
    private static Component cachedZoneTitle;
    private static Component cachedZoneBounds;
    private static Component cachedZoneDimension;

    private static boolean labelsInitialized = false;
    private static Component TITLE;
    private static Component TARGET_LABEL;
    private static Component MULTIPLIER_LABEL;
    private static Component HEALTH_LABEL;
    private static Component HEALTH_CAP;
    private static Component DAMAGE_LABEL;
    private static Component DAMAGE_CAP;
    private static Component NO_DAMAGE_LABEL;
    private static Component ARMOR_LABEL;
    private static Component ARMOR_CAP;
    private static Component BIG_ZOMBIE_LABEL;
    private static Component SPEEDY_ZOMBIE_LABEL;
    private static Component DISTANCE_LABEL;
    private static Component TIME_LABEL;
    private static Component HEIGHT_LABEL;
    private static Component NEXT_FACTOR_LABEL;
    private static Component ZONE_LABEL;
    private static Component LEVEL_LABEL;

    public static void renderDebugHud(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (client.options.hideGui || !ScalingDifficulty.CONFIG.hudTesting || player == null) return;
        ensureLabels();
        GuiGraphics graphics = event.getGuiGraphics();
        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;
        int x = 10;
        int y = 10;
        int step = client.font.lineHeight + 2;
        graphics.drawString(client.font, TITLE, x, y, WHITE, true);
        y += step;
        if (client.hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof Mob mob)
            renderEntityInfo(graphics, client.font, mob, config, x, y, step);
        else renderWorldInfo(graphics, client.font, player, config, x, y, step);
    }

    private static int drawText(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
        return x + font.width(text);
    }

    private static int drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
        return x + font.width(text);
    }

    private static void renderEntityInfo(GuiGraphics graphics, Font font, Mob mob, ScalingDifficultyConfig config, int x, int y, int step) {
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

        int cx = drawText(graphics, font, TARGET_LABEL, x, y, WHITE);
        drawText(graphics, font, entityName, cx, y, YELLOW);
        y += step;
        cx = drawText(graphics, font, MULTIPLIER_LABEL, x, y, WHITE);
        drawText(graphics, font, String.format("%.2fx", baseMultiplier), cx, y, GREEN);
        y += step;
        cx = drawText(graphics, font, HEALTH_LABEL, x, y, WHITE);
        cx = drawText(graphics, font, String.format("%.1f / %.1f", mob.getHealth(), maxHealth), cx, y, RED);
        drawText(graphics, font, HEALTH_CAP, cx, y, DARK_GRAY);
        y += step;
        if (baseDamage > 0) {
            cx = drawText(graphics, font, DAMAGE_LABEL, x, y, WHITE);
            cx = drawText(graphics, font, String.format("%.1f", displayDamage), cx, y, RED);
            drawText(graphics, font, DAMAGE_CAP, cx, y, DARK_GRAY);
        } else drawText(graphics, font, NO_DAMAGE_LABEL, x, y, GRAY);
        y += step;
        if (baseArmor > 0) {
            cx = drawText(graphics, font, ARMOR_LABEL, x, y, WHITE);
            cx = drawText(graphics, font, String.format("%.1f", displayArmor), cx, y, AQUA);
            drawText(graphics, font, ARMOR_CAP, cx, y, DARK_GRAY);
        }
        y += step;
        if (scale > 1.0) drawText(graphics, font, BIG_ZOMBIE_LABEL, x, y, GOLD);
        else if (scale < 1.0) drawText(graphics, font, SPEEDY_ZOMBIE_LABEL, x, y, AQUA);
    }

    @SuppressWarnings("resource")
    private static void renderWorldInfo(GuiGraphics graphics, Font font, Player player, ScalingDifficultyConfig config, int x, int y, int step) {
        ZoneSyncPayload.ZoneEntry currentZone = ClientZoneTracker.getCurrentZone();
        if (currentZone != null) {
            renderZoneInfo(graphics, font, currentZone, x, y, step);
            return;
        }
        Level level = player.level();
        String dimensionKey = level.dimension().location().toString();
        DimensionSettings settings = DimensionDifficultyLoader.getSettings(dimensionKey);
        BlockPos spawnPos = level.getSharedSpawnPos();
        int spawnX = settings.getDistanceCoordinatesX() != null ? settings.getDistanceCoordinatesX() : spawnPos.getX();
        int spawnZ = settings.getDistanceCoordinatesZ() != null ? settings.getDistanceCoordinatesZ() : spawnPos.getZ();
        float distance = Mth.sqrt((float) player.distanceToSqr(spawnX, player.getY(), spawnZ));
        long worldTime = level.getDayTime();

        float distElapsed = distance - settings.getStartingDistance();
        double distBonus = 0.0;
        if (distElapsed > 0 && !(config.excludeDistanceInOtherDimension && level.dimension() != Level.OVERWORLD)) {
            distBonus = ((double) (int) distElapsed / settings.getIncreasingDistance()) * settings.getDistanceFactor();
        }

        long timeElapsed = worldTime - (settings.getStartingTime() * DifficultyCalculator.TICKS_PER_MINUTE);
        int timeDivided = timeElapsed > 0 ? (int) (timeElapsed / (settings.getIncreasingTime() * DifficultyCalculator.TICKS_PER_MINUTE)) : 0;
        if (config.excludeTimeInOtherDimension && level.dimension() != Level.OVERWORLD) timeDivided = 0;
        double timeBonus = timeDivided * settings.getTimeFactor();

        int spawnHeightDivided = (Mth.floor(player.getY()) - settings.getStartingHeight()) / settings.getHeightDistance();
        if (!settings.isPositiveHeightIncrement() && spawnHeightDivided > 0) spawnHeightDivided = 0;
        if (!settings.isNegativeHeightIncrement() && spawnHeightDivided < 0) spawnHeightDivided = 0;
        if (config.excludeHeightInOtherDimension && level.dimension() != Level.OVERWORLD) spawnHeightDivided = 0;
        double heightBonus = Math.abs(spawnHeightDivided) * settings.getHeightFactor();
        double levelBonus = 0.0;
        if (HerosLevelsCompat.shouldApplyLevelFactor(settings))
            levelBonus = HerosLevelsCompat.getClientPlayerLevel(player) * settings.getLevelFactor();
        double totalFactor = settings.getStartingFactor() + distBonus + timeBonus + heightBonus + levelBonus;
        totalFactor = Math.min(totalFactor, settings.getMaxFactorHealth());

        drawText(graphics, font, TRACKER_LABELS.computeIfAbsent(dimensionKey,
                key -> Component.translatable("debug.scalingdifficulty.tracker", key)), x, y, GRAY);
        y += step;
        int cx = drawText(graphics, font, DISTANCE_LABEL, x, y, WHITE);
        cx = drawText(graphics, font, (int) distance + "m", cx, y, GRAY);
        cx = drawText(graphics, font, " -> ", cx, y, DARK_GRAY);
        drawText(graphics, font, "+" + String.format("%.2f", distBonus), cx, y, GREEN);
        y += step;
        cx = drawText(graphics, font, TIME_LABEL, x, y, WHITE);
        cx = drawText(graphics, font, (worldTime / DifficultyCalculator.TICKS_PER_MINUTE) + " minutes", cx, y, GRAY);
        cx = drawText(graphics, font, " -> ", cx, y, DARK_GRAY);
        drawText(graphics, font, "+" + String.format("%.2f", timeBonus), cx, y, GREEN);
        y += step;
        cx = drawText(graphics, font, HEIGHT_LABEL, x, y, WHITE);
        cx = drawText(graphics, font, (int) player.getY() + " Y", cx, y, GRAY);
        cx = drawText(graphics, font, " -> ", cx, y, DARK_GRAY);
        drawText(graphics, font, "+" + String.format("%.2f", heightBonus), cx, y, GREEN);
        y += step;
        if (HerosLevelsCompat.shouldApplyLevelFactor(settings)) {
            cx = drawText(graphics, font, LEVEL_LABEL, x, y, WHITE);
            cx = drawText(graphics, font, String.valueOf(HerosLevelsCompat.getClientPlayerLevel(player)), cx, y, GRAY);
            cx = drawText(graphics, font, " -> ", cx, y, DARK_GRAY);
            drawText(graphics, font, "+" + String.format("%.2f", levelBonus), cx, y, GREEN);
            y += step;
        }
        cx = drawText(graphics, font, NEXT_FACTOR_LABEL, x, y, WHITE);
        drawText(graphics, font, String.format("%.2fx", totalFactor), cx, y, YELLOW);
    }

    private static void renderZoneInfo(GuiGraphics graphics, Font font, ZoneSyncPayload.ZoneEntry zone, int x, int y, int step) {
        if (zone != cachedZone) {
            cachedZone = zone;
            cachedZoneTitle = Component.literal(zone.getDisplayName()).withStyle(ChatFormatting.GOLD);
            MutableComponent factor = Component.literal(String.format("%.2f", zone.factor())).withStyle(ChatFormatting.YELLOW);
            MutableComponent shape = Component.translatable(zone.shape() == DifficultyZone.Shape.BOX
                    ? "command.scalingdifficulty.zone.shape.box" : "command.scalingdifficulty.zone.shape.sphere").withStyle(ChatFormatting.GOLD);
            MutableComponent origin = Component.literal(formatCoords(zone.minX(), zone.minY(), zone.minZ())).withStyle(ChatFormatting.AQUA);
            if (zone.shape() == DifficultyZone.Shape.BOX) {
                MutableComponent dest = Component.literal(formatCoords(zone.maxX(), zone.maxY(), zone.maxZ())).withStyle(ChatFormatting.GREEN);
                cachedZoneBounds = Component.translatable("command.scalingdifficulty.zone.desc.box", shape, origin, dest, factor);
            } else {
                MutableComponent radius = Component.literal(String.format("%.1f", zone.radius())).withStyle(ChatFormatting.GREEN);
                cachedZoneBounds = Component.translatable("command.scalingdifficulty.zone.desc.sphere", shape, origin, radius, factor);
            }
            cachedZoneDimension = Component.translatable("debug.scalingdifficulty.zone.dimension", zone.dimension());
        }
        int cx = drawText(graphics, font, ZONE_LABEL, x, y, WHITE);
        drawText(graphics, font, cachedZoneTitle, cx, y, WHITE);
        y += step;
        drawText(graphics, font, cachedZoneBounds, x, y, WHITE);
        y += step;
        drawText(graphics, font, cachedZoneDimension, x, y, GRAY);
    }

    private static void ensureLabels() {
        if (labelsInitialized) return;
        labelsInitialized = true;
        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;
        TITLE = Component.translatable("debug.scalingdifficulty.title").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        TARGET_LABEL = Component.translatable("debug.scalingdifficulty.target");
        MULTIPLIER_LABEL = Component.translatable("debug.scalingdifficulty.multiplier");
        HEALTH_LABEL = Component.translatable("debug.scalingdifficulty.health");
        HEALTH_CAP = Component.translatable("debug.scalingdifficulty.cap", String.valueOf(config.maxFactorHealth));
        DAMAGE_LABEL = Component.translatable("debug.scalingdifficulty.damage");
        DAMAGE_CAP = Component.translatable("debug.scalingdifficulty.cap", String.valueOf(config.maxFactorDamage));
        NO_DAMAGE_LABEL = Component.translatable("debug.scalingdifficulty.no_damage");
        ARMOR_LABEL = Component.translatable("debug.scalingdifficulty.armor");
        ARMOR_CAP = Component.translatable("debug.scalingdifficulty.cap", String.valueOf(config.maxFactorProtection));
        BIG_ZOMBIE_LABEL = Component.translatable("debug.scalingdifficulty.variant_big");
        SPEEDY_ZOMBIE_LABEL = Component.translatable("debug.scalingdifficulty.variant_speedy");
        DISTANCE_LABEL = Component.translatable("debug.scalingdifficulty.distance");
        TIME_LABEL = Component.translatable("debug.scalingdifficulty.time");
        HEIGHT_LABEL = Component.translatable("debug.scalingdifficulty.height");
        NEXT_FACTOR_LABEL = Component.translatable("debug.scalingdifficulty.next_factor");
        ZONE_LABEL = Component.translatable("debug.scalingdifficulty.zone");
        LEVEL_LABEL = Component.translatable("debug.scalingdifficulty.level");
    }
}