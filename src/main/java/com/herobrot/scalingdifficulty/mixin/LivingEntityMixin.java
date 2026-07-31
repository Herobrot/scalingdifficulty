package com.herobrot.scalingdifficulty.mixin;

import com.herobrot.scalingdifficulty.ScalingDifficulty;
import com.herobrot.scalingdifficulty.api.DifficultyCalculator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void heroslib$dropMoreLoot(DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci,
                                       ResourceKey<LootTable> resourcekey, LootTable loottable,
                                       LootParams.Builder builder, LootParams lootparams) {
        if ((Object) this instanceof Mob mob) {
            DifficultyCalculator.dropMoreLoot(mob, loottable, lootparams);
        }
    }
}