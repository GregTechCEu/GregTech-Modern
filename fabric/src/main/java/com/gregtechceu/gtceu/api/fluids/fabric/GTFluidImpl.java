package com.gregtechceu.gtceu.api.fluids.fabric;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class GTFluidImpl extends GTFluid {
    public GTFluidImpl(@NotNull ResourceLocation fluidName, @NotNull FluidState state, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime) {
        super(fluidName, state, flowingFluid, block, bucket, burnTime);
    }
}
