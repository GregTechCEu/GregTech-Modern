package com.lowdragmc.gtceu.api.misc;

import com.lowdragmc.gtceu.api.capability.recipe.IO;
import com.lowdragmc.lowdraglib.msic.FluidTransferList;
import com.lowdragmc.lowdraglib.msic.ItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
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
    public long fill(FluidStack resource, boolean simulate) {
        if (io != IO.IN && io != IO.BOTH) return 0;
        return super.fill(resource, simulate);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, boolean simulate) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.empty();
        return super.drain(resource, simulate);
    }

    @Override
    public @NotNull FluidStack drain(long maxDrain, boolean simulate) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.empty();
        return super.drain(maxDrain, simulate);
    }
}
