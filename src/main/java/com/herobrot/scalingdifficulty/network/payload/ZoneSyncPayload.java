package com.herobrot.scalingdifficulty.network.payload;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.zone.DifficultyZone;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record ZoneSyncPayload(List<ZoneSyncPayload.ZoneEntry> zones) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ZoneSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "zone_sync"));

    public static final StreamCodec<FriendlyByteBuf, ZoneSyncPayload> STREAM_CODEC = StreamCodec.of(
            ZoneSyncPayload::write,
            ZoneSyncPayload::read
    );

    private static void write(FriendlyByteBuf buf, ZoneSyncPayload payload) {
        buf.writeVarInt(payload.zones.size());
        for (ZoneEntry entry : payload.zones) entry.write(buf);
    }

    private static ZoneSyncPayload read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ZoneEntry> zones = new ArrayList<>(size);
        for (int i = 0; i < size; i++) zones.add(ZoneEntry.read(buf));
        return new ZoneSyncPayload(zones);
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public record ZoneEntry(String dimension, DifficultyZone.Shape shape, double factor, @Nullable String name, String shortId,
                            int minX, int minY, int minZ, int maxX, int maxY, int maxZ, double radius) {

        public static ZoneEntry from(DifficultyZone zone) {
            if (zone.getShape() == DifficultyZone.Shape.BOX)
                return new ZoneEntry(zone.getDimension(), zone.getShape(), zone.getFactor(), zone.getName(), zone.getShortId(),
                        zone.getBoxMin().getX(), zone.getBoxMin().getY(), zone.getBoxMin().getZ(),
                        zone.getBoxMax().getX(), zone.getBoxMax().getY(), zone.getBoxMax().getZ(), 0);
            else return new ZoneEntry(zone.getDimension(), zone.getShape(), zone.getFactor(), zone.getName(), zone.getShortId(),
                        zone.getCenter().getX(), zone.getCenter().getY(), zone.getCenter().getZ(),
                        0, 0, 0, zone.getRadius());

        }

        void write(FriendlyByteBuf buf) {
            buf.writeUtf(dimension);
            buf.writeEnum(shape);
            buf.writeDouble(factor);
            buf.writeBoolean(name != null);
            if (name != null) buf.writeUtf(name);
            buf.writeUtf(shortId);
            buf.writeInt(minX);
            buf.writeInt(minY);
            buf.writeInt(minZ);
            buf.writeInt(maxX);
            buf.writeInt(maxY);
            buf.writeInt(maxZ);
            buf.writeDouble(radius);
        }

        static ZoneEntry read(FriendlyByteBuf buf) {
            return new ZoneEntry(buf.readUtf(), buf.readEnum(DifficultyZone.Shape.class), buf.readDouble(),
                    buf.readBoolean() ? buf.readUtf() : null, buf.readUtf(),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readDouble());
        }

        public boolean contains(String dimensionKey, double x, double y, double z) {
            if (!dimension.equals(dimensionKey)) return false;
            if (shape == DifficultyZone.Shape.BOX)
                return x >= minX && x <= maxX + 1 && y >= minY && y <= maxY + 1 && z >= minZ && z <= maxZ + 1;
            else {
                double dx = x - (minX + 0.5);
                double dy = y - (minY + 0.5);
                double dz = z - (minZ + 0.5);
                return dx * dx + dy * dy + dz * dz <= radius * radius;
            }
        }

        public String getDisplayName() { return (name != null && !name.isBlank()) ? name : shortId; }
    }
}