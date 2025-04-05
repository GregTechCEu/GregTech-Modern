package com.gregtechceu.gtceu.integration.xei.handlers.fluid;

import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CycleFluidStackHandler implements IFluidHandlerModifiable {

    private final List<FluidStackList> stacks;

    public CycleFluidStackHandler(List<List<FluidStack>> stacks) {
        this.stacks = new ArrayList<>();
        for (var list : stacks) {
            this.stacks.add(FluidStackList.of(list));
        }
    }

    @Override
    public int getTanks() {
        return this.stacks.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        List<FluidStack> stackList = stacks.get(tank).getStacks();
        return stackList != null && !stackList.isEmpty() ?
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000L) % stackList.size())) :
                FluidStack.EMPTY;
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        if (tank >= 0 && tank < this.stacks.size()) {
            this.stacks.set(tank, FluidStackList.of(fluidStack));
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.getFluidInTank(tank).getAmount();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return false;
    }

    public FluidStackList getStackList(int i) {
        return this.stacks.get(i);
    }
}
