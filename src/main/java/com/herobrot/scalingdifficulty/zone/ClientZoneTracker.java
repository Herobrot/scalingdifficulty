package com.herobrot.scalingdifficulty.zone;

import com.herobrot.scalingdifficulty.api.events.ZoneEnterEvent;
import com.herobrot.scalingdifficulty.network.payload.ZoneRequestPayload;
import com.herobrot.scalingdifficulty.network.payload.ZoneSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class ClientZoneTracker {

    private static List<ZoneSyncPayload.ZoneEntry> zones = List.of();
    @Nullable
    private static LocalPlayer trackedPlayer = null;
    @Nullable
    private static BlockPos lastCheckedPos = null;
    @Nullable
    private static ZoneSyncPayload.ZoneEntry currentZone = null;
    public static List<ZoneSyncPayload.ZoneEntry> getZones() { return zones; }

    public static void updateZones(List<ZoneSyncPayload.ZoneEntry> newZones) {
        zones = newZones;
        lastCheckedPos = null;
    }

    @SuppressWarnings("resource")
    public static void onClientTick(Minecraft client) {
        if (client.player == null) {
            trackedPlayer = null;
            return;
        }

        if (trackedPlayer != client.player) {
            trackedPlayer = client.player;
            lastCheckedPos = null;
            currentZone = null;
            requestZoneSync();
        }

        if (zones.isEmpty()) {
            if (currentZone != null) {
                currentZone = null;
                NeoForge.EVENT_BUS.post(new ZoneEnterEvent(client.player, null));
            }
            return;
        }

        BlockPos pos = client.player.blockPosition();
        if (pos.equals(lastCheckedPos)) return;
        lastCheckedPos = pos;

        String dimension = client.player.level().dimension().location().toString();
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        ZoneSyncPayload.ZoneEntry newZone = zones.stream()
                .filter(zone -> zone.contains(dimension, x, y, z))
                .findFirst()
                .orElse(null);

        if (newZone != currentZone) {
            currentZone = newZone;
            NeoForge.EVENT_BUS.post(new ZoneEnterEvent(client.player, currentZone));
        }
    }

    @Nullable
    public static ZoneSyncPayload.ZoneEntry getCurrentZone() { return currentZone; }

    private static void requestZoneSync() {
        try {
            PacketDistributor.sendToServer(ZoneRequestPayload.INSTANCE);
        } catch (UnsupportedOperationException ignored) {
            // Esto solo por si un cliente con mod entra a un server sin mod. :P
        }
    }
}