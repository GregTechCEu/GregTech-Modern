package com.gregtechceu.gtceu.data.tags;

import net.minecraft.world.entity.EntityType;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<EntityType<?>> provider) {
        provider.addTag(GTTags.EntityTypes.HEAT_IMMUNE)
                .add(EntityType.BLAZE, EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER_SKELETON, EntityType.WITHER);
        provider.addTag(GTTags.EntityTypes.CHEMICAL_IMMUNE)
                .add(EntityType.SKELETON, EntityType.STRAY);
    }
}
