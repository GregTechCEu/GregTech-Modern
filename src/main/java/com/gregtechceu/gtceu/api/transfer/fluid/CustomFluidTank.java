package com.gregtechceu.gtceu.api.transfer.fluid;

import lombok.Getter;
import lombok.Setter;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank implements IFluidHandlerModifiable {

    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};

    public CustomFluidTank(int capacity) {
        this(capacity, e -> true);
    }

    public CustomFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    @Override
    protected void onContentsChanged() {
        onContentsChanged.run();
    }

    public CustomFluidTank copy() {
        FluidStack copiedStack = this.fluid.copy();
        CustomFluidTank copied = new CustomFluidTank(this.capacity, this.validator);
        copied.setFluid(copiedStack);
        return copied;
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        this.setFluid(stack);
        this.onContentsChanged();
    }

    public int fill(int tank, FluidStack resource, FluidAction simulate) {
        return this.fill(resource, simulate);
    }

    public FluidStack drain(int tank, FluidStack resource, FluidAction simulate) {
        return this.drain(resource, simulate);
    }
}
