package com.herobrot.scalingdifficulty.network;

import com.herobrot.scalingdifficulty.network.payload.ZoneRequestPayload;
import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import com.herobrot.scalingdifficulty.zone.ClientZoneTracker;
import com.herobrot.scalingdifficulty.zone.DifficultyZone;
import com.herobrot.scalingdifficulty.zone.DifficultyZoneSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ZoneSyncManager {

    private static final Set<UUID> MODDED_PLAYERS = new HashSet<>();

    public static void handleSync(ZoneSyncPayload payload, IPayloadContext context) {
        ClientZoneTracker.updateZones(payload.zones());
    }

    public static void handleRequest(ZoneRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            MODDED_PLAYERS.add(player.getUUID());
            PacketDistributor.sendToPlayer(player, createPayload(player.getServer()));
        }
    }

    public static void syncToAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
            if (MODDED_PLAYERS.contains(player.getUUID()))
                PacketDistributor.sendToPlayer(player, createPayload(server));
    }

    public static void removePlayer(UUID playerId) { MODDED_PLAYERS.remove(playerId); }

    public static boolean isModdedPlayer(Player player) { return MODDED_PLAYERS.contains(player.getUUID()); }

    private static ZoneSyncPayload createPayload(MinecraftServer server) {
        List<DifficultyZone> zones = DifficultyZoneSavedData.get(server).getZones();
        return new ZoneSyncPayload(zones.stream().map(ZoneSyncPayload.ZoneEntry::from).toList());
    }
}