package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank
                             implements IFluidHandlerModifiable, ITagSerializable<CompoundTag> {

    public CustomFluidTank(int capacity) {
        super(capacity);
    }

    public CustomFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public CustomFluidTank(FluidStack stack) {
        super(stack.getAmount());
        setFluid(stack);
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        setFluid(stack);
    }

    @Override
    public CompoundTag serializeNBT() {
        return writeToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        readFromNBT(nbt);
    }
}
