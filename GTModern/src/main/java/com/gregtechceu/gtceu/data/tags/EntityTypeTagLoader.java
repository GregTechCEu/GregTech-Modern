package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider<EntityType<?>> provider) {
        create(provider, CustomTags.HEAT_IMMUNE, EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON,
                EntityType.WITHER);
        create(provider, CustomTags.CHEMICAL_IMMUNE, EntityType.SKELETON, EntityType.STRAY);
    }

    public static void create(RegistrateTagsProvider<EntityType<?>> provider, TagKey<EntityType<?>> tagKey,
                              EntityType<?>... rls) {
        var builder = provider.addTag(tagKey);
        for (EntityType<?> entityType : rls) {
            builder.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).get());
        }
    }

    public static void create(RegistrateTagsProvider<EntityType<?>> provider, TagKey<EntityType<?>> tagKey,
                              ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }
}
