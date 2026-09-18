package brachy.modularui.utils;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

public class MultiFluidTankHandler implements IMultiFluidTankHandler {

    private final IFluidTank[] tanks;

    public MultiFluidTankHandler(IFluidTank... tanks) {
        Objects.requireNonNull(tanks);
        for (IFluidTank tank : tanks) {
            Objects.requireNonNull(tank);
        }
        this.tanks = Arrays.copyOf(tanks, tanks.length);
    }

    public MultiFluidTankHandler(int count, int capacity) {
        this(count, i -> new FluidTank(capacity));
    }

    public MultiFluidTankHandler(int count, IntFunction<IFluidTank> tankBuilder) {
        this.tanks = new IFluidTank[count];
        for (int i = 0; i < count; i++) {
            this.tanks[i] = Objects.requireNonNull(tankBuilder.apply(i));
        }
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return getFluidTank(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getFluidTank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getFluidTank(tank).isFluidValid(stack);
    }

    @Override
    public IFluidTank getFluidTank(int index) {
        return this.tanks[index];
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int fillAmount = resource.getAmount();
        FluidStack toFill = resource.copy();
        // first find tanks with matching fluid
        for (IFluidTank tank : this.tanks) {
            if (!tank.getFluid().isEmpty() && resource.isFluidEqual(tank.getFluid())) {
                fillAmount -= tank.fill(toFill, action);
                toFill.setAmount(fillAmount);
                if (fillAmount <= 0) return resource.getAmount();
            }
        }
        // if still fluid there, insert into empty tanks
        for (IFluidTank tank : this.tanks) {
            if (tank.getFluid().isEmpty()) {
                fillAmount -= tank.fill(toFill, action);
                toFill.setAmount(fillAmount);
                if (fillAmount <= 0) return resource.getAmount();
            }
        }
        return resource.getAmount() - fillAmount;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        return drain(0, resource, action);
    }

    private @NotNull FluidStack drain(int startIndex, FluidStack resource, FluidAction action) {
        if (startIndex >= this.tanks.length) return FluidStack.EMPTY;
        int drainAmount = resource.getAmount();
        for (int i = startIndex; i < this.tanks.length; i++) {
            IFluidTank tank = this.tanks[i];
            if (tank.getFluid().isEmpty() || !resource.isFluidEqual(tank.getFluid())) continue;
            FluidStack d = this.tanks[i].drain(drainAmount, action);
            if (!d.isEmpty()) {
                drainAmount -= d.getAmount();
                if (drainAmount <= 0) return resource.copy();
            }
        }
        if (drainAmount == resource.getAmount()) return FluidStack.EMPTY;
        FluidStack drained = resource.copy();
        drained.shrink(drainAmount);
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (int i = 0; i < this.tanks.length; i++) {
            FluidStack drained = this.tanks[i].drain(maxDrain, action);
            if (!drained.isEmpty()) {
                // if already drained enough from this slot return
                if (drained.getAmount() >= maxDrain) return drained;
                // otherwise drain from all other slots with the same fluid
                FluidStack toDrain = drained.copy();
                toDrain.setAmount(maxDrain - toDrain.getAmount());
                FluidStack drained2 = drain(i + 1, toDrain, action);
                if (drained.isEmpty()) return drained;
                drained.grow(drained2.getAmount());
                return drained;
            }
        }
        return FluidStack.EMPTY;
    }
}
