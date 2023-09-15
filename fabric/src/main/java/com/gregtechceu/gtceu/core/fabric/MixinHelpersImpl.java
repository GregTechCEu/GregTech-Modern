package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.common.data.GTFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        if (prop == null || !prop.hasFluidSupplier()) return;
        var fluids = GTFluids.MATERIAL_FLUID_FLOWING.get(prop);
        if (fluids != null) {
            FluidRenderHandlerRegistry.INSTANCE.register(fluids.get().getFirst(), fluids.get().getSecond(), new SimpleFluidRenderHandler(prop.getStillTexture(), prop.getFlowTexture(), material.getMaterialRGB()));
        }
    }
}
