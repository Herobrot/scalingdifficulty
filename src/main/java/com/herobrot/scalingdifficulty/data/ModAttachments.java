package com.herobrot.scalingdifficulty.data;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.herobrot.scalingdifficulty.ScalingDifficulty;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ScalingDifficulty.MOD_ID);
    
    public static final Supplier<AttachmentType<Boolean>> PROCESSED = ATTACHMENT_TYPES.register(
            "processed",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Float>> DIFFICULTY_MULTIPLIER = ATTACHMENT_TYPES.register(
            "difficulty_multiplier",
            () -> AttachmentType.builder(() -> 1.0f)
                    .serialize(Codec.FLOAT)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> BIG_ZOMBIE = ATTACHMENT_TYPES.register(
            "big_zombie",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> SPEEDY_ZOMBIE = ATTACHMENT_TYPES.register(
            "speedy_zombie",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .build()
    );
}