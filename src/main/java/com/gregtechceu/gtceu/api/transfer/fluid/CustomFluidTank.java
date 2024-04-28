package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank implements IFluidHandlerModifiable, INBTSerializable<CompoundTag>, IContentChangeAware {

    @Getter @Setter
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
        CompoundTag tag = new CompoundTag();
        if (!this.fluid.isEmpty()) {
            this.fluid.save(provider, tag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.fluid = FluidStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).getOrThrow();
    }
}
