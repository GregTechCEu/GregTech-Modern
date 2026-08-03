package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.entity.EntityType;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<EntityType<?>> provider) {
        provider.addTag(CustomTags.HEAT_IMMUNE)
                .add(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER_SKELETON, EntityType.WITHER);

        provider.addTag(CustomTags.CHEMICAL_IMMUNE)
                .addTag(EntityTypeTags.SKELETONS);

        provider.addTag(CustomTags.IRON_GOLEMS).add(EntityType.IRON_GOLEM);
    }
}
