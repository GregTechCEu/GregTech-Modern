package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class FluidTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<Fluid> provider) {
        tag(provider, CustomTags.LIGHTER_FLUIDS).add(GTMaterials.Butane.getFluid(), GTMaterials.Propane.getFluid());
        tag(provider, CustomTags.HPCA_COOLANTS).add(GTMaterials.PCBCoolant.getFluid());
    }

    private static TagAppender<Fluid, Fluid> tag(RegistrateTagsProvider.IntrinsicImpl<Fluid> provider,
                                                 TagKey<Fluid> tagKey) {
        return provider.tag(tagKey);
    }
}
