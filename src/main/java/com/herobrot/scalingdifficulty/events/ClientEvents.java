package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    
    public static void registerConfigScreen(ModContainer modContainer) {
        
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) ->
                AutoConfig.getConfigScreen(ScalingDifficultyConfig.class, parentScreen).get()
        );
    }

    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (client.options.hideGui || !ScalingDifficulty.CONFIG.hudTesting || player == null) return;

        ScalingDifficultyConfig config = ScalingDifficulty.CONFIG;

        int x = 10;
        int y = 10;
        int step = client.font.lineHeight + 2; 
        int color = 0xFFFFFF; 

        event.getGuiGraphics().drawString(client.font, "§lScalingDifficulty Debug§r", x, y, 0xFFAA00, true);
        y += step;

        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) client.hitResult;
            if (entityHitResult.getEntity() instanceof Mob mob) {
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

                String entityName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();

                event.getGuiGraphics().drawString(client.font, "Target: §e" + entityName, x, y, color, true); y += step;
                event.getGuiGraphics().drawString(client.font, "Active Multiplier: §a" + String.format("%.2f", baseMultiplier) + "x", x, y, color, true); y += step;
                event.getGuiGraphics().drawString(client.font, "Health: §c" + String.format("%.1f", mob.getHealth()) + " / " + String.format("%.1f", maxHealth) + " §8(Max Cap: " + config.maxFactorHealth + "x)", x, y, color, true); y += step;

                if (baseDamage > 0) {
                    event.getGuiGraphics().drawString(client.font, "Damage: §c" + String.format("%.1f", displayDamage) + " §8(Max Cap: " + config.maxFactorDamage + "x)", x, y, color, true);
                } else {
                    event.getGuiGraphics().drawString(client.font, "Damage: §7[No Base Damage]", x, y, color, true);
                }

                y += step;

                if (baseArmor > 0) {
                    event.getGuiGraphics().drawString(client.font, "Armor: §b" + String.format("%.1f", displayArmor) + " §8(Max Cap: " + config.maxFactorProtection + "x)", x, y, color, true); y += step;
                }
                
                if (scale > 1.0) {
                    event.getGuiGraphics().drawString(client.font, "§6[SPECIAL VARIANT: BIG ZOMBIE]", x, y, color, true);
                } else if (scale < 1.0) {
                    event.getGuiGraphics().drawString(client.font, "§b[SPECIAL VARIANT: SPEEDY ZOMBIE]", x, y, color, true);
                }
                return; 
            }
        }

        Level level = player.level();
        
        String dimensionKey = level.dimension().location().toString();
        com.herobrot.scalingdifficulty.data.DimensionSettings settings = com.herobrot.scalingdifficulty.data.DimensionDifficultyLoader.getSettings(dimensionKey);

        BlockPos spawnPos = level.getSharedSpawnPos();

        int spawnX = settings.distanceCoordinatesX != null ? settings.distanceCoordinatesX : spawnPos.getX();
        int spawnZ = settings.distanceCoordinatesZ != null ? settings.distanceCoordinatesZ : spawnPos.getZ();

        float distance = Mth.sqrt((float) player.distanceToSqr(spawnX, player.getY(), spawnZ));
        long worldTime = level.getGameTime();
        
        float distElapsed = distance - settings.startingDistance;
        int distDivided = distElapsed > 0 ? (int) (distElapsed / settings.increasingDistance) : 0;
        if (config.excludeDistanceInOtherDimension && level.dimension() != Level.OVERWORLD) distDivided = 0;
        double distBonus = distDivided * settings.distanceFactor;

        long timeElapsed = worldTime - (settings.startingTime * 1200L);
        int timeDivided = timeElapsed > 0 ? (int) (timeElapsed / (settings.increasingTime * 1200L)) : 0;
        if (config.excludeTimeInOtherDimension && level.dimension() != Level.OVERWORLD) timeDivided = 0;
        double timeBonus = timeDivided * settings.timeFactor;

        int spawnHeightDivided = (Mth.floor(player.getY()) - settings.startingHeight) / settings.heightDistance;
        if (!settings.positiveHeightIncrement && spawnHeightDivided > 0) spawnHeightDivided = 0;
        if (!settings.negativeHeightIncrement && spawnHeightDivided < 0) spawnHeightDivided = 0;
        if (config.excludeHeightInOtherDimension && level.dimension() != Level.OVERWORLD) spawnHeightDivided = 0;
        double heightBonus = Math.abs(spawnHeightDivided) * settings.heightFactor;

        double totalFactor = settings.startingFactor + distBonus + timeBonus + heightBonus;
        
        totalFactor = Math.min(totalFactor, settings.maxFactorHealth);

        event.getGuiGraphics().drawString(client.font, "§7[Live Global Tracker - " + dimensionKey + "]", x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Distance: §7" + (int)distance + "m §8-> §a+" + String.format("%.2f", distBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Time: §7" + (worldTime / 24000) + " days §8-> §a+" + String.format("%.2f", timeBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Height: §7" + (int)player.getY() + " Y §8-> §a+" + String.format("%.2f", heightBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Est. Next Spawn Factor: §e" + String.format("%.2f", totalFactor) + "x", x, y, color, true);
    }
}