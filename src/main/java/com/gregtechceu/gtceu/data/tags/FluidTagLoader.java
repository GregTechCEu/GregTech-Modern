package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class FluidTagLoader {

    public static void init(RegistrateTagsProvider<Fluid> provider) {
        create(provider, CustomTags.LIGHTER_FLUIDS, GTMaterials.Butane.getFluid(), GTMaterials.Propane.getFluid());
    }

    public static void create(RegistrateTagsProvider<Fluid> provider, TagKey<Fluid> tag, ResourceLocation... rls) {
        var builder = provider.addTag(tag);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }

    public static void create(RegistrateTagsProvider<Fluid> provider, TagKey<Fluid> tag, Fluid... fluids) {
        var builder = provider.addTag(tag);
        for (Fluid fluid : fluids) {
            builder.add(BuiltInRegistries.FLUID.getResourceKey(fluid).get());
        }
    }
}
