package com.gregtechceu.gtceu.api.fluid.forge;

import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.fluid.GTFluid;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.extensions.IFluidExtension;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class GTFluidImpl extends GTFluid implements IFluidExtension {

    private final Supplier<FluidType> fluidType;

    public GTFluidImpl(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid,
                       Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block,
                       Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
        super(state, stillFluid, flowingFluid, block, bucket, burnTime);
        this.fluidType = fluidType;
    }

    @Override
    public FluidType getFluidType() {
        return fluidType.get();
    }

    public static class Source extends GTFluidImpl {

        public Source(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid,
                      Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block,
                      Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
            super(state, stillFluid, flowingFluid, block, bucket, burnTime, fluidType);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return true;
        }
    }

    public static class Flowing extends GTFluidImpl {

        public Flowing(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid,
                       Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block,
                       Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
            super(state, stillFluid, flowingFluid, block, bucket, burnTime, fluidType);
            // registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, net.minecraft.world.level.material.FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return false;
        }
    }
}
