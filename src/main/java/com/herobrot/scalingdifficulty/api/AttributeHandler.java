package com.herobrot.scalingdifficulty.api;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.herobrot.scalingdifficulty.ScalingDifficulty;

public class AttributeHandler {

    public static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "health_scaling");
    public static final ResourceLocation DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "damage_scaling");
    public static final ResourceLocation ARMOR_MOD_ID = ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "armor_scaling");
    public static final ResourceLocation SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "speed_scaling");

    public static void applyModifier(Mob mob, Holder<Attribute> attribute, ResourceLocation id, double factor) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            // Un factor de 1.0 significa que no hay cambio. Un factor de 3.0 significa añadir un 200% (+2.0).
            double amount = factor - 1.0D;
            if (amount <= 0) return;

            // Limpiamos por seguridad, aunque la bandera "processed" ya nos protege
            instance.removeModifier(id);

            // ADD_MULTIPLIED_BASE escala respetando la base Vanilla (vital para que el bonus de Zombie Líder no explote)
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

            // Si es salud, curamos al mob para que su vida actual coincida con su nueva vida máxima
            if (attribute.equals(Attributes.MAX_HEALTH)) {
                mob.setHealth(mob.getMaxHealth());
            }
        }
    }
}