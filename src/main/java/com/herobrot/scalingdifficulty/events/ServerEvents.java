package com.herobrot.scalingdifficulty.events;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.commands.DiagnosticCommand;
import com.herobrot.scalingdifficulty.data.DimensionDifficultyLoader;
import com.herobrot.scalingdifficulty.network.ZoneSyncManager;
import com.herobrot.scalingdifficulty.commands.DifficultyZoneCommand;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ScalingDifficulty.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DimensionDifficultyLoader());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (ScalingDifficulty.CONFIG.devMode) DiagnosticCommand.register(event.getDispatcher());
        DifficultyZoneCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) ZoneSyncManager.removePlayer(player.getUUID());
    }
}