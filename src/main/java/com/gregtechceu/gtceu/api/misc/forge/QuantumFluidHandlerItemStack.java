package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.api.item.datacomponents.LargeFluidContent;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class QuantumFluidHandlerItemStack implements IFluidHandlerItem {

    @Getter
    protected @NotNull ItemStack container;
    protected long capacity;

    public QuantumFluidHandlerItemStack(@NotNull ItemStack container, long capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    // Retrieve the capacity clamped to an int.
    protected int getClampedCapacity() {
        return GTMath.saturatedCast(this.capacity);
    }

    // For Fluid IO, clamping to int is fine.
    // For internal structures, make sure to use getFluidAmount() alongside this.
    public @NotNull FluidStack getFluid() {
        LargeFluidContent content = this.container.getOrDefault(GTDataComponents.LARGE_FLUID_CONTENT,
                LargeFluidContent.EMPTY);
        if (content.amount() <= 0L || content.stored().isEmpty()) {
            return FluidStack.EMPTY;
        }
        return content.stored().copyWithAmount(GTMath.saturatedCast(content.amount()));
    }

    public long getFluidAmount() {
        return this.container.getOrDefault(GTDataComponents.LARGE_FLUID_CONTENT, LargeFluidContent.EMPTY).amount();
    }

    private void setFluid(FluidStack fluid, long amount) {
        fluid.setAmount(GTMath.saturatedCast(amount));
        this.container.set(GTDataComponents.LARGE_FLUID_CONTENT, new LargeFluidContent(fluid.copy(), amount));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getClampedCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction doFill) {
        if (this.container.getCount() != 1 || resource.isEmpty() || !this.canFillFluidType(resource)) {
            return 0;
        }
        FluidStack contained = this.getFluid();
        long amount = this.getFluidAmount();
        if (contained.isEmpty()) {
            int fillAmount = Math.min(getClampedCapacity(), resource.getAmount());
            if (doFill.execute()) {
                FluidStack filled = resource.copy();
                this.setFluid(filled, fillAmount);
            }

            return fillAmount;
        } else if (FluidStack.isSameFluidSameComponents(contained, resource)) {
            int fillAmount = Math.min(GTMath.saturatedCast(this.capacity - amount), resource.getAmount());
            if (doFill.execute() && fillAmount > 0) {
                long fluidAmountAfterFill = amount + (long) fillAmount;
                this.setFluid(contained, fluidAmountAfterFill);
            }

            return fillAmount;
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (!FluidStack.isSameFluidSameComponents(this.getFluid(), resource)) {
            return FluidStack.EMPTY;
        }
        return this.drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (this.container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack contained = this.getFluid();
        long fluidAmount = this.getFluidAmount();
        if (fluidAmount <= 0 || !this.canDrainFluidType(contained)) {
            return FluidStack.EMPTY;
        }

        // Can drain at most Integer.MAX_VALUE
        int drainAmount = GTMath.saturatedCast(Math.min(fluidAmount, maxDrain));
        FluidStack drained = contained.copy();
        drained.setAmount(drainAmount);
        if (action.execute()) {
            long fluidAfterDrain = fluidAmount - (long) drainAmount;
            contained.setAmount(GTMath.saturatedCast(fluidAfterDrain));
            if (contained.isEmpty()) {
                this.setContainerToEmpty();
            } else {
                this.setFluid(contained, fluidAfterDrain);
            }
        }

        return drained;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    protected void setContainerToEmpty() {
        this.container.remove(GTDataComponents.LARGE_FLUID_CONTENT);
    }
}
