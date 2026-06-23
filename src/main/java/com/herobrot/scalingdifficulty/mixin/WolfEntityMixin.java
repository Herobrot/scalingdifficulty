package com.herobrot.scalingdifficulty.mixin;

import com.herobrot.scalingdifficulty.api.AttributeHandler;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin {

    // Se inyecta justo después de que el lobo es domesticado y su vida es forzada a 40
    @Inject(method = "applyTamingSideEffects", at = @At("TAIL"))
    @SuppressWarnings("resource")
    private void scalingdifficulty$onTamed(CallbackInfo ci) {
        Wolf wolf = (Wolf) (Object) this;

        if (wolf.level().isClientSide()) return;

        // Recuperamos el multiplicador que le calculamos cuando spawneó
        float multiplier = wolf.getData(ModAttachments.DIFFICULTY_MULTIPLIER);

        if (multiplier > 1.0f) {
            // Re-aplicamos el modificador sobre la nueva vida base de 40.0
            AttributeHandler.applyModifier(wolf, Attributes.MAX_HEALTH, AttributeHandler.HEALTH_MOD_ID, multiplier);
        }
    }
}