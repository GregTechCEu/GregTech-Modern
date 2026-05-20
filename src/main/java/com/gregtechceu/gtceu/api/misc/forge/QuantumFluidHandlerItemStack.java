package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumFluidHandlerItemStack implements IFluidHandlerItem, ICapabilityProvider {

    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
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
        CompoundTag tagCompound = this.container.getTag();
        if (tagCompound == null || !tagCompound.contains("stored") || !tagCompound.contains("storedAmount")) {
            return FluidStack.EMPTY;
        }
        FluidStack stack = FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("stored"));
        if (!stack.isEmpty()) {
            stack.setAmount(GTMath.saturatedCast(tagCompound.getLong("storedAmount")));
        }
        return stack;
    }

    public long getFluidAmount() {
        CompoundTag tagCompound = this.container.getTag();
        if (tagCompound == null || !tagCompound.contains("storedAmount")) return 0;
        return tagCompound.getLong("storedAmount");
    }

    private void setFluid(FluidStack fluid, long amount) {
        fluid.setAmount(GTMath.saturatedCast(amount));

        CompoundTag fluidTag = new CompoundTag();
        fluid.writeToNBT(fluidTag);

        CompoundTag containerTag = this.container.getOrCreateTag();
        containerTag.put("stored", fluidTag);
        containerTag.putLong("storedAmount", amount);
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
        } else if (contained.isFluidEqual(resource)) {

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
        return this.container.getCount() == 1 && !resource.isEmpty() && resource.isFluidEqual(this.getFluid()) ?
                this.drain(resource.getAmount(), action) : FluidStack.EMPTY;
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
        this.container.removeTagKey("stored");
        this.container.removeTagKey("storedAmount");
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, this.holder);
    }
}
