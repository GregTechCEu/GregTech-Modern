package com.gregtechceu.gtceu.core.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.common.data.GTFluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        if (prop == null || !prop.hasFluidSupplier() || prop.getFluid() == null) return;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(prop.getFluid());
        if (extensions instanceof GTClientFluidTypeExtensions gtExtensions) {
            gtExtensions.setFlowingTexture(prop.getFlowTexture());
            gtExtensions.setStillTexture(prop.getStillTexture());
            gtExtensions.setTintColor(material.getMaterialARGB());
        }
    }
}
