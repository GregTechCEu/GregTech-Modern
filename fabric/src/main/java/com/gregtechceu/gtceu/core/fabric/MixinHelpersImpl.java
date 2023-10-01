package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.FlowingFluid;

public class MixinHelpersImpl {

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (value.getFluid().get() instanceof FlowingFluid flowingFluid) {
            FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid.getFlowing(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
            FluidRenderHandlerRegistry.INSTANCE.register(flowingFluid.getSource(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
        } else if (value.getFluid().get() != null) {
            FluidRenderHandlerRegistry.INSTANCE.register(value.getFluid().get(), new SimpleFluidRenderHandler(value.getStillTexture(), value.getFlowTexture(), material.getMaterialRGB()));
        }
        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register((atlas, registry) -> {
            if (value.getStillTexture() != null) registry.register(value.getStillTexture());
            if (value.getFlowTexture() != null) registry.register(value.getFlowTexture());
        });
    }
}
