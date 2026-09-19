package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.client.DebugHudOverlay;
import com.herobrot.scalingdifficulty.client.ZoneBorderRenderer;
import com.herobrot.scalingdifficulty.config.ScalingDifficultyConfig;
import com.herobrot.scalingdifficulty.zone.ClientZoneTracker;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) ->
                AutoConfig.getConfigScreen(ScalingDifficultyConfig.class, parentScreen).get()
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientZoneTracker.onClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) { DebugHudOverlay.renderDebugHud(event); }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!ScalingDifficulty.CONFIG.hudTesting) return;
        if (ClientZoneTracker.getZones().isEmpty()) return;
        ZoneBorderRenderer.render(event);
    }
}