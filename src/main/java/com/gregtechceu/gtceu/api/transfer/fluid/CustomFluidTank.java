package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank
                             implements IFluidHandlerModifiable, ITagSerializable<CompoundTag>, IContentChangeAware {

    @Getter
    @Setter
    protected @NotNull Runnable onContentsChanged = () -> {};

    public CustomFluidTank(int capacity) {
        super(capacity, e -> true);
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

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        setFluid(stack);
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        this.onContentsChanged();
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
