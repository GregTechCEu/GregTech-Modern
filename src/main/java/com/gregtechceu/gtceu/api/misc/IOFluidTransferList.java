package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote IOFluidTransferList
 */
public class IOFluidTransferList extends FluidTransferList implements IFluidHandlerModifiable {

    @Getter
    private final IO io;

    public IOFluidTransferList(List<IFluidHandler> transfers, IO io, Predicate<FluidStack> filter) {
        super(transfers);
        this.io = io;
        setFilter(filter);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (io != IO.IN && io != IO.BOTH) return 0;
        return super.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.EMPTY;
        return super.drain(maxDrain, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.EMPTY;
        return super.drain(resource, action);
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        int index = 0;
        for (IFluidHandler transfer : transfers) {
            if (transfer instanceof IFluidHandlerModifiable modifiable) {
                if (tank - index < transfer.getTanks()) {
                    modifiable.setFluidInTank(tank - index, fluidStack);
                }
                return;
            }
            index += transfer.getTanks();
        }
    }
}
