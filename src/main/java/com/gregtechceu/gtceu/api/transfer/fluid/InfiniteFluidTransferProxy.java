package com.gregtechceu.gtceu.api.transfer.fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InfiniteFluidTransferProxy extends FluidTransferDelegate {

    private final boolean infiniteSource;
    private final boolean infiniteSink;

    public InfiniteFluidTransferProxy(IFluidTransfer delegate, boolean infiniteSource, boolean infiniteSink) {
        super(delegate);

        this.infiniteSource = infiniteSource;
        this.infiniteSink = infiniteSink;
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (infiniteSink)
            return resource.getAmount();

        return super.fill(tank, resource, simulate, notifyChanges);
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (infiniteSource)
            return resource.copy();

        return super.drain(tank, resource, simulate, notifyChanges);
    }
}
