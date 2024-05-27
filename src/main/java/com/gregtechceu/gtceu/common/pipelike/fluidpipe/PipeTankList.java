package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class PipeTankList implements IFluidTransfer, Iterable<FluidStorage> {

    private final FluidPipeBlockEntity pipe;
    private final FluidStorage[] tanks;
    private final Direction facing;

    public PipeTankList(FluidPipeBlockEntity pipe, Direction facing, FluidStorage... fluidTanks) {
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
    public long getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        if (pipe.isBlocked(facing) || resource == null || resource.getAmount() <= 0)
            return 0;

        return fill(resource, simulate, tank);
    }

    @Override
    public boolean supportsFill(int tank) {
        return true;
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        FluidStorage storage = tanks[tank];
        FluidStack drained = storage.drain(resource, simulate, notifyChanges);
        if (!drained.isEmpty()) return drained;
        else return FluidStack.empty();
    }

    @Override
    public boolean supportsDrain(int tank) {
        return true;
    }

    @Override
    public long fill(FluidStack resource, boolean simulate, boolean notifyChanges) {
        int channel;
        if (pipe.isBlocked(facing) || resource == null || resource.getAmount() <= 0 ||
                (channel = findChannel(resource)) < 0)
            return 0;

        return fill(resource, simulate, channel);
    }

    private long fullCapacity() {
        return tanks.length * pipe.getCapacityPerTank();
    }

    private long fill(FluidStack resource, boolean simulate, int channel) {
        if (channel >= tanks.length) return 0;
        FluidStorage tank = tanks[channel];
        FluidStack currentFluid = tank.getFluid();

        if (currentFluid.isEmpty() || currentFluid.getAmount() <= 0) {
            FluidStack newFluid = resource.copy();
            newFluid.setAmount(Math.min(pipe.getCapacityPerTank(), newFluid.getAmount()));
            if (!simulate) {
                tank.setFluid(newFluid);
                pipe.receivedFrom(facing);
                pipe.checkAndDestroy(newFluid);
            }
            return newFluid.getAmount();
        }
        if (currentFluid.isFluidEqual(resource)) {
            long toAdd = Math.min(tank.getCapacity() - currentFluid.getAmount(), resource.getAmount());
            if (toAdd > 0) {
                if (!simulate) {
                    currentFluid.setAmount(currentFluid.getAmount() + toAdd);
                    pipe.receivedFrom(facing);
                    pipe.checkAndDestroy(currentFluid);
                }
                return toAdd;
            }
        }

        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean doDrain, boolean notifyChanges) {
        if (maxDrain <= 0) return FluidStack.empty();
        for (FluidStorage tank : tanks) {
            FluidStack drained = tank.drain(maxDrain, doDrain, notifyChanges);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return new Object();
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {}

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, boolean doDrain) {
        if (fluidStack.isEmpty() || fluidStack.getAmount() <= 0) return FluidStack.empty();
        fluidStack = fluidStack.copy();
        for (FluidStorage tank : tanks) {
            FluidStack drained = tank.drain(fluidStack, doDrain);
            if (!drained.isEmpty()) return drained;
        }
        return FluidStack.empty();
    }

    @Override
    @NotNull
    public Iterator<FluidStorage> iterator() {
        return Arrays.stream(tanks).iterator();
    }
}
