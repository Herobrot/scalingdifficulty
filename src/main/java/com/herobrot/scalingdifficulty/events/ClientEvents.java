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

    // Método llamado desde el constructor principal del mod
    public static void registerConfigScreen(ModContainer modContainer) {
        // Le dice a NeoForge qué pantalla abrir al darle clic a "Config" en la lista de mods
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

        // Configuración de anclaje visual (Esquina superior izquierda)
        int x = 10;
        int y = 10;
        int step = client.font.lineHeight + 2; // Salto de línea automático
        int color = 0xFFFFFF; // Blanco

        // Título del HUD
        event.getGuiGraphics().drawString(client.font, "§lScalingDifficulty Debug§r", x, y, 0xFFAA00, true);
        y += step;

        // 1. MODO OBJETIVO: Mirando a una entidad
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) client.hitResult;

            if (entityHitResult.getEntity() instanceof Mob mob) {
                // Leemos los atributos sincronizados de forma nativa por Minecraft
                double maxHealth = mob.getAttributeValue(Attributes.MAX_HEALTH);
                double baseHealth = mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
                double damage = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double armor = mob.getAttributeValue(Attributes.ARMOR);
                double scale = mob.getAttributeValue(Attributes.SCALE);

                // Deducimos el multiplicador calculando la diferencia entre el valor base y el actual
                float multiplier = (float) (maxHealth / baseHealth);

                String entityName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();

                event.getGuiGraphics().drawString(client.font, "Target: §e" + entityName, x, y, color, true); y += step;
                event.getGuiGraphics().drawString(client.font, "Active Multiplier: §a" + String.format("%.2f", multiplier) + "x", x, y, color, true); y += step;

                event.getGuiGraphics().drawString(client.font, "Health: §c" + String.format("%.1f", mob.getHealth()) + " / " + String.format("%.1f", maxHealth) + " §8(Max Cap: " + config.maxFactorHealth + "x)", x, y, color, true); y += step;
                event.getGuiGraphics().drawString(client.font, "Damage: §c" + String.format("%.1f", damage) + " §8(Max Cap: " + config.maxFactorDamage + "x)", x, y, color, true); y += step;
                event.getGuiGraphics().drawString(client.font, "Armor: §b" + String.format("%.1f", armor) + " §8(Max Cap: " + config.maxFactorProtection + "x)", x, y, color, true); y += step;

                // Deducimos la variante especial leyendo la escala sincronizada
                if (scale > 1.0) {
                    event.getGuiGraphics().drawString(client.font, "§6[SPECIAL VARIANT: BIG ZOMBIE]", x, y, color, true);
                } else if (scale < 1.0) {
                    event.getGuiGraphics().drawString(client.font, "§b[SPECIAL VARIANT: SPEEDY ZOMBIE]", x, y, color, true);
                }
                return; // Cortamos la ejecución para no dibujar el Modo Global
            }
        }

        // 2. MODO GLOBAL: Seguimiento en vivo (No apuntando a un mob)
        Level level = player.level();
        BlockPos spawnPos = level.getSharedSpawnPos();
        float distance = Mth.sqrt((float) player.distanceToSqr(spawnPos.getX(), player.getY(), spawnPos.getZ()));
        long worldTime = level.getGameTime();

        // Matemáticas simuladas para mostrar en tiempo real
        float distElapsed = distance - config.startingDistance;
        int distDivided = distElapsed > 0 ? (int) (distElapsed / config.increasingDistance) : 0;
        double distBonus = distDivided * config.distanceFactor;

        long timeElapsed = worldTime - (config.startingTime * 1200L);
        int timeDivided = timeElapsed > 0 ? (int) (timeElapsed / (config.increasingTime * 1200L)) : 0;
        double timeBonus = timeDivided * config.timeFactor;

        int spawnHeightDivided = (Mth.floor(player.getY()) - config.startingHeight) / config.heightDistance;
        if (!config.positiveHeightIncrement && spawnHeightDivided > 0) spawnHeightDivided = 0;
        if (!config.negativeHeightIncrement && spawnHeightDivided < 0) spawnHeightDivided = 0;
        double heightBonus = Math.abs(spawnHeightDivided) * config.heightFactor;

        double totalFactor = config.startingFactor + distBonus + timeBonus + heightBonus;

        event.getGuiGraphics().drawString(client.font, "§7[Live Global Tracker]", x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Distance: §7" + (int)distance + "m §8-> §a+" + String.format("%.2f", distBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Time: §7" + (worldTime / 24000) + " days §8-> §a+" + String.format("%.2f", timeBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Height: §7" + (int)player.getY() + " Y §8-> §a+" + String.format("%.2f", heightBonus), x, y, color, true); y += step;
        event.getGuiGraphics().drawString(client.font, "Est. Next Spawn Factor: §e" + String.format("%.2f", totalFactor) + "x", x, y, color, true);
    }
}