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

        provider.addTag(CustomTags.END_INHABITORS)
                .add(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.SHULKER, EntityType.ENDER_DRAGON)
                // also add some guesses at other tags people might use instead of the one I picked
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "endermen"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "end_mobs"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "ender_mobs"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "ender"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "enders"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "shulkers"))
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "endermites"));

        provider.addTag(CustomTags.SENSITIVE_TO_DISJUNCTION)
                .addTag(CustomTags.END_INHABITORS);
    }
}
