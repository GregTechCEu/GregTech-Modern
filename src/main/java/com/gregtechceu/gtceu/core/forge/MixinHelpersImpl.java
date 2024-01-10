package com.gregtechceu.gtceu.core.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value != null) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
            if (extensions instanceof GTClientFluidTypeExtensions gtExtensions) {
                gtExtensions.setFlowingTexture(value.getFlowTexture());
                gtExtensions.setStillTexture(value.getStillTexture());
                gtExtensions.setTintColor(material.getMaterialARGB());
            }
        }
    }
}
