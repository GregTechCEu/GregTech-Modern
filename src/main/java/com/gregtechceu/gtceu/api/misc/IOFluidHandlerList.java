package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote IOFluidTransferList
 */
public class IOFluidHandlerList extends FluidHandlerList implements IFluidHandlerModifiable {

    @Getter
    private final IO io;

    public IOFluidHandlerList(List<IFluidHandler> handlers, IO io, Predicate<FluidStack> filter) {
        super(handlers);
        this.io = io;
        setFilter(filter);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (io != IO.IN && io != IO.BOTH) return 0;
        return super.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.EMPTY;
        return super.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (io != IO.OUT && io != IO.BOTH) return FluidStack.EMPTY;
        return super.drain(maxDrain, action);
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        int index = 0;
        for (IFluidHandler handler : handlers) {
            if (handler instanceof IFluidHandlerModifiable modifiable) {
                if (tank - index < handler.getTanks()) modifiable.setFluidInTank(tank - index, stack);
                return;
            }
            index += handler.getTanks();
        }
    }
}
