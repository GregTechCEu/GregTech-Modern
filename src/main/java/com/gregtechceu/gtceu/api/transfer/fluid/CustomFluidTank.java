package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank
                             implements IFluidHandlerModifiable, INBTSerializable<CompoundTag>, IContentChangeAware {

    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};

    public CustomFluidTank(int capacity) {
        this(capacity, e -> true);
    }

    public CustomFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public CustomFluidTank(FluidStack stack) {
        super(stack.getAmount());
        setFluid(stack);
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

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return writeToNBT(provider, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        readFromNBT(provider, nbt);
    }
}
