package com.herobrot.scalingdifficulty;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID, value = Dist.CLIENT)
public class ScalingDifficultyClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // TODO: Migrar el HudRenderCallback de Fabric a RenderGuiEvent de NeoForge para el hudTesting
    }
}