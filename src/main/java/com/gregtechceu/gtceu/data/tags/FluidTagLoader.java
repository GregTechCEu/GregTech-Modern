package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class FluidTagLoader {

    public static void init(RegistrateTagsProvider.IntrinsicImpl<Fluid> provider) {
        provider.addTag(GTTags.Fluids.LIGHTER_FLUIDS).add(GTMaterials.Butane.getFluid(), GTMaterials.Propane.getFluid());
        provider.addTag(GTTags.Fluids.HPCA_COOLANTS).add(GTMaterials.PCBCoolant.getFluid());
    }
}
