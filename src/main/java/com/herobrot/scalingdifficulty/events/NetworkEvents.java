package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.network.ZoneSyncManager;
import com.herobrot.scalingdifficulty.network.payload.ZoneRequestPayload;
import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID)
public class NetworkEvents {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ScalingDifficulty.getModVersion()).optional();
        registrar.playToClient(ZoneSyncPayload.TYPE, ZoneSyncPayload.STREAM_CODEC, ZoneSyncManager::handleSync);
        registrar.playToServer(ZoneRequestPayload.TYPE, ZoneRequestPayload.STREAM_CODEC, ZoneSyncManager::handleRequest);
    }
}