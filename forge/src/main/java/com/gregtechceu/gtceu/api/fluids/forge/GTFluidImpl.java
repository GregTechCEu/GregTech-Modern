package com.gregtechceu.gtceu.api.fluids.forge;

import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class GTFluidImpl extends GTFluid implements IForgeFluid {
    private final Supplier<FluidType> fluidType;

    public GTFluidImpl(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
        super(state, stillFluid, flowingFluid, block, bucket, burnTime);
        this.fluidType = fluidType;
    }

    @Override
    public FluidType getFluidType() {
        return fluidType.get();
    }

    public static class Source extends GTFluidImpl {

        public Source(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
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

        public Flowing(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime, Supplier<FluidType> fluidType) {
            super(state, stillFluid, flowingFluid, block, bucket, burnTime, fluidType);
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
