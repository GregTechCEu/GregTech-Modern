package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote IOFluidTransferList
 */
public class IOFluidTransferList extends FluidTransferList {
    @Getter
    private final IO io;

    public IOFluidTransferList(List<IFluidTransfer> transfers, IO io, Predicate<FluidStack> filter) {
        super(transfers);
        this.io = io;
        setFilter(filter);
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (io != IO.IN && io != IO.BOTH) return 0;
        return super.fill(tank, resource, simulate, notifyChanges);
    }

    @Override
    public long fill(FluidStack resource, boolean simulate, boolean notifyChanged) {
        if (io != IO.IN && io != IO.BOTH) return 0;
        return super.fill(resource, simulate, notifyChanged);
    }

    @Override
    public @NotNull FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.empty();
        return super.drain(tank, resource, simulate, notifyChanges);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanged) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.empty();
        return super.drain(resource, simulate, notifyChanged);
    }

    @Override
    public @NotNull FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanged) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.empty();
        return super.drain(maxDrain, simulate, notifyChanged);
    }

    @Override
    public boolean supportsFill(int tank) {
        return io == IO.IN || io == IO.BOTH;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return io == IO.OUT || io == IO.BOTH;
    }
}
