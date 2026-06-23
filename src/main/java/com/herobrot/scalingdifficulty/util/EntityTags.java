package com.herobrot.scalingdifficulty.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class EntityTags {
    // Etiqueta estándar de la comunidad para jefes (c:bosses)
    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", "bosses"));

    // Podemos crear una etiqueta custom para animales bebé si lo necesitamos en el futuro
    // public static final TagKey<EntityType<?>> ANIMAL_BABIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", "animal_babies"));
}