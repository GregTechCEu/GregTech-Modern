package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import org.jetbrains.annotations.NotNull;

public class QuantumTankFluidHandlerItemStack extends FluidHandlerItemStack {

    private long storedAmount;
    private final long maxAmount;
    private FluidStack stored = FluidStack.EMPTY;

    public QuantumTankFluidHandlerItemStack(@NotNull ItemStack container, long capacity) {
        super(container, Integer.MAX_VALUE);
        maxAmount = capacity;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return new FluidStack(stored, GTMath.saturatedCast(storedAmount));
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!resource.isFluidEqual(stored)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (stored.isEmpty()) return FluidStack.EMPTY;
        long toDrain = Math.min(storedAmount, maxDrain);
        var copy = new FluidStack(stored, (int) toDrain);
        if (action.execute() && toDrain > 0) {
            storedAmount -= toDrain;
            if (storedAmount == 0) stored = FluidStack.EMPTY;
        }
        return copy.isEmpty() ? FluidStack.EMPTY : copy;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        long free = maxAmount - storedAmount;
        long canFill = 0;
        if (stored.isEmpty() || stored.isFluidEqual(resource)) {
            canFill = Math.min(resource.getAmount(), free);
        }
        if (action.execute() && canFill > 0) {
            if (stored.isEmpty()) stored = new FluidStack(resource, 1000);
            storedAmount = Math.min(maxAmount, storedAmount + canFill);
        }
        return (int) canFill;
    }

    @Override
    public int getTankCapacity(int tank) {
        return GTMath.saturatedCast(maxAmount);
    }
}
