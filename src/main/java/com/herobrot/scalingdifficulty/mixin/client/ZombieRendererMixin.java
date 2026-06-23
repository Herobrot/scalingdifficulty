package com.herobrot.scalingdifficulty.mixin.client;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.data.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Apuntamos a la clase que realmente posee el método compilado
@Mixin(LivingEntityRenderer.class)
public class ZombieRendererMixin {

    // Usamos el nombre del mé-todo simple y los parámetros de su firma borrada (LivingEntity)
    @Inject(method = "scale", at = @At("HEAD"))
    protected void scalingdifficulty$scaleBigZombie(LivingEntity entity, PoseStack poseStack, float partialTickTime, CallbackInfo ci) {

        // Filtramos que sea un Zombie y leemos nuestro Attachment
        if (entity instanceof Zombie zombie && zombie.getData(ModAttachments.BIG_ZOMBIE)) {
            float size = ScalingDifficulty.CONFIG.bigZombieSize;
            poseStack.scale(size, size, size);
        }
    }
}