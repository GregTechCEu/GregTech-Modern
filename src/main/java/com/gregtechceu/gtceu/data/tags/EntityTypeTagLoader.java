package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<EntityType<?>> provider) {
        provider.addTag(CustomTags.HEAT_IMMUNE)
                .add(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE)
                .add(EntityType.WITHER_SKELETON, EntityType.WITHER);

        provider.addTag(CustomTags.CHEMICAL_IMMUNE)
                .addTag(EntityTypeTags.SKELETONS);

        provider.addTag(CustomTags.CONSTRUCTS).add(EntityType.IRON_GOLEM)
                .addTag(Tags.EntityTypes.MINECARTS)
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "teletor"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "notor"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "magnetron"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "boundroid"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "boundroid_winch"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexscaves", "nucleeper"));
    }
}
