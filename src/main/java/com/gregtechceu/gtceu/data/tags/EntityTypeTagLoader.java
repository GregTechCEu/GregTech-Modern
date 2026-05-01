package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<EntityType<?>> provider) {
        tag(provider, CustomTags.HEAT_IMMUNE)
                .add(EntityType.BLAZE, EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER_SKELETON, EntityType.WITHER);
        tag(provider, CustomTags.CHEMICAL_IMMUNE)
                .add(EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED);
        tag(provider, CustomTags.IRON_GOLEMS).add(EntityType.IRON_GOLEM);
        tag(provider, CustomTags.SPIDERS)
                .add(EntityType.SPIDER, EntityType.CAVE_SPIDER);
    }

    private static TagAppender<EntityType<?>, EntityType<?>> tag(
                                                                 RegistrateTagsProvider.IntrinsicImpl<EntityType<?>> provider,
                                                                 TagKey<EntityType<?>> tagKey) {
        return provider.tag(tagKey);
    }
}
