package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
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
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        if (client.options.hideGui || !ScalingDifficulty.CONFIG.hudTesting) return;

        // Verificamos a qué está mirando el jugador
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) client.hitResult;

            if (entityHitResult.getEntity() instanceof Mob mob) {
                // Leemos nuestro multiplicador guardado
                float multiplier = mob.getData(ModAttachments.DIFFICULTY_MULTIPLIER);

                int scaledWidth = event.getGuiGraphics().guiWidth();
                int scaledHeight = event.getGuiGraphics().guiHeight();

                String entityName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();

                // Dibujamos el texto usando GuiGraphics (el reemplazo moderno de DrawContext)
                event.getGuiGraphics().drawString(client.font, entityName, (int) (scaledWidth * 0.01f), (int) (scaledHeight * 0.95f), 16777215, true);
                event.getGuiGraphics().drawString(client.font, "Health: " + mob.getHealth() + " (Factor: x" + multiplier + ")", (int) (scaledWidth * 0.01f), (int) (scaledHeight * 0.91f), 16777215, true);
            }
        }
    }
}