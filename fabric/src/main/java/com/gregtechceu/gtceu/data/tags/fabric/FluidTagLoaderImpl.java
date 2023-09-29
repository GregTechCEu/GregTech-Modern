package com.gregtechceu.gtceu.data.tags.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class FluidTagLoaderImpl {
    public static void addPlatformSpecificFluidTags(RegistrateTagsProvider<Fluid> provider) {
        create(provider, FluidTags.WATER,
                GTCEu.id("oil"), GTCEu.id("flowing_oil"),
                GTCEu.id("oil_heavy"), GTCEu.id("flowing_oil_heavy"),
                GTCEu.id("oil_medium"), GTCEu.id("flowing_oil_medium"),
                GTCEu.id("oil_light"), GTCEu.id("flowing_oil_light"),
                GTCEu.id("natural_gas"), GTCEu.id("flowing_natural_gas"));
    }

    private static void create(RegistrateTagsProvider<Fluid> provider, TagKey<Fluid> tagKey, ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.add(rl);
        }
    }
}
