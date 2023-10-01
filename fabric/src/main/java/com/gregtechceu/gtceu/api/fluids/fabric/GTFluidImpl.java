package com.gregtechceu.gtceu.api.fluids.fabric;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public abstract class GTFluidImpl extends GTFluid {
    public GTFluidImpl(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime) {
        super(state, stillFluid, flowingFluid, block, bucket, burnTime);
    }
    
    public static class Source extends GTFluidImpl {

        public Source(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime) {
            super(state, stillFluid, flowingFluid, block, bucket, burnTime);
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

        public Flowing(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime) {
            super(state, stillFluid, flowingFluid, block, bucket, burnTime);
            //registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
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
