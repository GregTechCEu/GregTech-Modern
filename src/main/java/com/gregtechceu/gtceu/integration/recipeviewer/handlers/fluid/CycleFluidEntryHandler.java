package com.gregtechceu.gtceu.integration.recipeviewer.handlers.fluid;

import com.gregtechceu.gtceu.integration.recipeviewer.entry.fluid.FluidEntryList;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CycleFluidEntryHandler implements IFluidHandler {

    @Getter
    private final List<FluidEntryList> entries;

    private List<List<FluidStack>> unwrapped = null;

    public CycleFluidEntryHandler(List<FluidEntryList> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public List<List<FluidStack>> getUnwrapped() {
        if (unwrapped == null) {
            unwrapped = entries.stream()
                    .map(CycleFluidEntryHandler::getStacksNullable)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return unwrapped;
    }

    private static @Nullable List<FluidStack> getStacksNullable(FluidEntryList list) {
        if (list == null) return null;
        return list.getStacks();
    }

    public FluidEntryList getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public int getTanks() {
        return entries.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        List<FluidStack> stackList = getUnwrapped().get(tank);
        return stackList == null || stackList.isEmpty() ? FluidStack.EMPTY :
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size()));
    }

    /*
     * @Override
     * public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
     * if (tank >= 0 && tank < entries.size()) {
     * entries.set(tank, FluidStackList.of(fluidStack));
     * unwrapped = null;
     * }
     * }
     */

    @Override
    public int getTankCapacity(int tank) {
        return getFluidInTank(tank).getAmount();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    // @Override
    // public boolean supportsFill(int tank) {
    // return false;
    // }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    // @Override
    // public boolean supportsDrain(int tank) {
    // return false;
    // }
}
