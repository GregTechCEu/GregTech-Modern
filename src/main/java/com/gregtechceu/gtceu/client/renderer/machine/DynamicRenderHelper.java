package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.impl.*;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DynamicRenderHelper {

    public static DynamicRender<?, ?> makeBoilerPartRender(BoilerFireboxType fireboxType,
                                                           Supplier<? extends Block> casingBlock) {
        return new BoilerMultiPartRender(fireboxType, casingBlock);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static DynamicRender<?, ?> makeFluidAreaRender(FluidBlockRenderer fluidBlockRenderer,
                                                          Optional<Fluid> fixedFluid,
                                                          List<RelativeDirection> drawFaces) {
        return new FluidAreaRender(fluidBlockRenderer, fixedFluid, drawFaces);
    }

    public static DynamicRender<?, ?> makeRecipeFluidAreaRender() {
        return makeFluidAreaRender(FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer(), Optional.empty(), FluidAreaRender.DEFAULT_FACES);
    }

    public static DynamicRender<?, ?> createPBFLavaRender() {
        return makeFluidAreaRender(FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer(), Optional.of(Fluids.LAVA.getSource()), FluidAreaRender.DEFAULT_FACES);
    }

    public static DynamicRender<?, ?> createFusionRingRender() {
        return new FusionRingRender();
    }

    public static DynamicRender<?, ?> createQuantumChestRender() {
        return new QuantumChestItemRender();
    }

    public static DynamicRender<?, ?> createQuantumTankRender() {
        return new QuantumTankFluidRender();
    }

    public static DynamicRender<?, ?> createCentralMonitorRender() {
        return new CentralMonitorRender();
    }
}
