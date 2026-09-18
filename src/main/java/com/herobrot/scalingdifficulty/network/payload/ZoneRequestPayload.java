package com.herobrot.scalingdifficulty.network.payload;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ZoneRequestPayload() implements CustomPacketPayload {

    public static final ZoneRequestPayload INSTANCE = new ZoneRequestPayload();

    public static final CustomPacketPayload.Type<ZoneRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "zone_request"));

    public static final StreamCodec<FriendlyByteBuf, ZoneRequestPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}