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

public class IOFluidHandlerList extends FluidHandlerList implements IFluidHandlerModifiable {

    @Getter
    private final IO io;
    private final Predicate<FluidStack> inFilter;
    private final Predicate<FluidStack> outFilter;

    public IOFluidHandlerList(List<IFluidHandler> handlers, IO io, Predicate<FluidStack> inFilter,
                              Predicate<FluidStack> outFilter) {
        super(handlers);
        this.io = io;
        this.inFilter = inFilter;
        this.outFilter = outFilter;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!io.support(IO.IN) || !inFilter.test(resource)) return 0;
        return super.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!io.support(IO.OUT) || !outFilter.test(resource)) return FluidStack.EMPTY;
        return super.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (!io.support(IO.OUT)) return FluidStack.EMPTY;
        var fluidStack = super.drain(maxDrain, FluidAction.SIMULATE);
        if (fluidStack.isEmpty() || !outFilter.test(fluidStack)) return FluidStack.EMPTY;
        return action.simulate() ? fluidStack : super.drain(maxDrain, action);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        Predicate<FluidStack> filter = s -> true;
        if (io.support(IO.IN)) filter = inFilter.and(filter);
        if (io.support(IO.OUT)) filter = outFilter.and(filter);
        return filter.test(stack) && super.isFluidValid(tank, stack);
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
