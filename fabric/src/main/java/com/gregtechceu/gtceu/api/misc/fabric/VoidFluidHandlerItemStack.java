package com.gregtechceu.gtceu.api.misc.fabric;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class VoidFluidHandlerItemStack extends FluidHandlerItemStack {
    public VoidFluidHandlerItemStack(@NotNull ContainerItemContext container, long capacity) {
        super(container, capacity);
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return FluidStack.empty();
    }

    @Override
    protected boolean setFluid(FluidStack fluid, TransactionContext tx) {
        return true;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource == null || maxAmount <= 0)
            return 0;
        return Math.min(this.capacity, maxAmount);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean canFillFluidType(FluidVariant variant, long amount) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidVariant variant, long amount) {
        return true;
    }
}
