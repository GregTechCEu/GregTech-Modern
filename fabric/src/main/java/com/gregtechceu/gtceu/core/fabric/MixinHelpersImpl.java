package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.world.level.material.FlowingFluid;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value.getFluid().get() instanceof FlowingFluid flowingFluid) {
            FluidVariantRendering.register(flowingFluid.getFlowing(), new GTFluidVariantRenderHandler());
            FluidVariantRendering.register(flowingFluid.getSource(), new GTFluidVariantRenderHandler());
            FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid.getFlowing(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
            FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid.getSource(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
        } else if (value.getFluid().get() != null) {
            FluidVariantRendering.register(value.getFluid().get(), new GTFluidVariantRenderHandler());
            FluidRenderHandlerRegistry.INSTANCE.register(value.getFluid().get(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
        }
    }
}
