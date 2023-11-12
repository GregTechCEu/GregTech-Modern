package com.gregtechceu.gtceu.core.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.HashSet;
import java.util.Set;

public class MixinHelpersImpl {

    private static final Set<Pair<Material, ResourceLocation>> registeredFluidTextures = new HashSet<>();

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        if (registeredFluidTextures.contains(Pair.of(material, value.getStillTexture()))) return;
        registeredFluidTextures.add(Pair.of(material, value.getStillTexture()));
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
