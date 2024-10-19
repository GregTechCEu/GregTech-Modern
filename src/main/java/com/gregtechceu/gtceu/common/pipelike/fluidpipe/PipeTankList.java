package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class PipeTankList implements IFluidHandlerModifiable, Iterable<CustomFluidTank> {

    private final FluidPipeBlockEntity pipe;
    private final CustomFluidTank[] tanks;
    private final Direction facing;

    public PipeTankList(FluidPipeBlockEntity pipe, Direction facing, CustomFluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.pipe = pipe;
        this.facing = facing;
    }

    private int findChannel(FluidStack stack) {
        if (stack.isEmpty() || tanks == null)
            return -1;
        int empty = -1;
        for (int i = tanks.length - 1; i >= 0; i--) {
            FluidStack f = tanks[i].getFluid();
            if (f.isEmpty())
                empty = i;
            else if (f.isFluidEqual(stack))
                return i;
        }
        return empty;
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks[tank].getFluid();
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        tanks[tank].setFluid(fluidStack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }

    private int fullCapacity() {
        return tanks.length * pipe.getCapacityPerTank();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int channel;
        if (pipe.isBlocked(facing) || resource.getAmount() < 0 || (channel = findChannel(resource)) < 0) return 0;
        return fill(resource, action, channel);
    }

    private int fill(FluidStack resource, FluidAction action, int channel) {
        if (channel >= tanks.length) return 0;
        CustomFluidTank tank = tanks[channel];
        FluidStack currentFluid = tank.getFluid();

        if (currentFluid.isEmpty() || currentFluid.getAmount() <= 0) {
            FluidStack newFluid = resource.copy();
            newFluid.setAmount(Math.min(pipe.getCapacityPerTank(), newFluid.getAmount()));
            if (action.execute()) {
                tank.setFluid(newFluid);
                pipe.receivedFrom(facing);
                pipe.checkAndDestroy(newFluid);
            }
            return newFluid.getAmount();
        }
        if (currentFluid.isFluidEqual(resource)) {
            int toAdd = Math.min(tank.getCapacity() - currentFluid.getAmount(), resource.getAmount());
            if (toAdd > 0) {
                if (action.execute()) {
                    currentFluid.setAmount(currentFluid.getAmount() + toAdd);
                    pipe.receivedFrom(facing);
                    pipe.checkAndDestroy(currentFluid);
                }
                return toAdd;
            }
        }

        return 0;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (CustomFluidTank tank : tanks) {
            FluidStack drained = tank.drain(maxDrain, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.getAmount() <= 0) return FluidStack.EMPTY;
        resource = resource.copy();
        for (CustomFluidTank tank : tanks) {
            FluidStack drained = tank.drain(resource, action);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    @NotNull
    public Iterator<CustomFluidTank> iterator() {
        return Arrays.stream(tanks).iterator();
    }
}
