package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.fluid.InvertedFluidRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public static final ResourceLocation FLUID_SCREEN_OVERLAY = GTCEu.id("textures/misc/fluid_screen_overlay.png");

    public GTClientFluidTypeExtensions(FluidType fluidType,
                                       @Nullable ResourceLocation stillTexture,
                                       @Nullable ResourceLocation flowingTexture,
                                       int tintColor) {
        this.fluidType = fluidType;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    private final FluidType fluidType;
    @Getter
    @Setter
    @Nullable
    private ResourceLocation flowingTexture, stillTexture;
    @Getter
    private final int tintColor;

    @Override
    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return FLUID_SCREEN_OVERLAY;
    }

    @Override
    public boolean renderFluid(FluidState fluidState, BlockAndTintGetter level, BlockPos pos, VertexConsumer consumer,
                               BlockState blockState) {
        // use inverted rendering :3
        // the method checks all conditions by itself; we don't need to do anything special here
        return InvertedFluidRenderer.maybeRenderFluidInverted(fluidType, fluidState, blockState, level, pos, consumer);
    }
}
