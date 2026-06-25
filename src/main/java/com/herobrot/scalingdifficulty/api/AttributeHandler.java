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
    public static final ResourceLocation SCALE_MOD_ID = ResourceLocation.fromNamespaceAndPath(ScalingDifficulty.MOD_ID, "size_scaling");

    public static void applyModifier(Mob mob, Holder<Attribute> attribute, ResourceLocation id, double factor) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            double amount = factor - 1.0D;

            // Corrección: Solo ignoramos si el factor es exactamente 1.0 (sin cambio).
            // Esto permite que factores negativos (-0.5) pasen correctamente.
            if (Math.abs(amount) < 0.001D) return;

            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

            // Si estamos modificando la salud, curamos al mob a su nuevo máximo,
            // O si le bajamos la vida (Speedy Zombie), Minecraft lo recorta automáticamente.
            if (attribute.equals(Attributes.MAX_HEALTH)) {
                mob.setHealth(mob.getMaxHealth());
            }
        }
    }
}