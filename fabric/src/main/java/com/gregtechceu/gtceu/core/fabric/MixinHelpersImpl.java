package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.common.data.GTFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidProperty prop) {
        for (FluidStorageKey key : FluidStorageKey.allKeys()) {
            Fluid value = prop.getStorage().get(key);
            if (value instanceof FlowingFluid flowingFluid) {
                FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid.getFlowing(), new SimpleFluidRenderHandler(prop.getStillTexture(), prop.getFlowTexture(), material.getMaterialRGB()));
            }
            if (value != null) {
                FluidRenderHandlerRegistry.INSTANCE.register(value, new SimpleFluidRenderHandler(prop.getStillTexture(), prop.getFlowTexture(), material.getMaterialRGB()));
            }
        }
    }
}
