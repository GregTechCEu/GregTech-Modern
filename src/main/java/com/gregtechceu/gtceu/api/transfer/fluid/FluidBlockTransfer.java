package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

public class FluidBlockTransfer implements IFluidHandler {

    protected final LiquidBlock fluidBlock;
    protected final BlockState blockState;
    protected final Level world;
    protected final BlockPos blockPos;

    public FluidBlockTransfer(LiquidBlock fluidBlock, Level world, BlockPos blockPos) {
        this.fluidBlock = fluidBlock;
        this.world = world;
        this.blockPos = blockPos;
        this.blockState = world.getBlockState(blockPos);
    }

    public Fluid getFluid() {
        return fluidBlock.fluid;
    }

    public int getTanks() {
        return 1;
    }

    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? new FluidStack(getFluid(), FluidHelper.getBucket()) : FluidStack.EMPTY;
    }

    public int getTankCapacity(int tank) {
        return FluidHelper.getBucket();
    }

    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid() == getFluid();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return drain(new FluidStack(this.getFluid(), maxDrain), action);
    }

    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!resource.isEmpty() && resource.getFluid() == getFluid() && resource.getAmount() >= getTankCapacity(0)) {
            FluidStack drained = getFluidInTank(0).copy();
            if (action == FluidAction.EXECUTE) {
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
            return drained;
        }

        return FluidStack.EMPTY;
    }
}
