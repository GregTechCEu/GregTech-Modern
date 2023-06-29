package com.gregtechceu.gtceu.core.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        if (prop == null || !prop.hasFluidSupplier()) return;
        GTClientFluidTypeExtensions extensions = GTClientFluidTypeExtensions.FLUID_TYPES.get(ForgeRegistries.FLUIDS.getKey(prop.getFluid()));
        if (extensions != null) {
            extensions.setFlowingTexture(prop.getFlowTexture());
            extensions.setStillTexture(prop.getStillTexture());
            extensions.setTintColor(material.getMaterialRGB());
        }
    }
}
