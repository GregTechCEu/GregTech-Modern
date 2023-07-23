package com.gregtechceu.gtceu.common.machine.appeng.stack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * @Author GlodBlock
 * @Date 2023/4/22-19:25
 */
public class WrappedFluidStack implements IAEFluidStack {

    @Nonnull
    FluidStack delegate;

    private WrappedFluidStack(@Nonnull FluidStack stack) {
        this.delegate = stack;
    }

    public static WrappedFluidStack fromFluidStack(FluidStack fluidStack) {
        return fluidStack == null ? null : new WrappedFluidStack(fluidStack);
    }

    public static WrappedFluidStack fromNBT(NBTTagCompound data) {
        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(data);
        return fromFluidStack(fluidStack);
    }

    public static WrappedFluidStack fromPacket(ByteBuf buffer) {
        byte len = buffer.readByte();
        byte[] name = new byte[len];
        buffer.readBytes(name, 0, len);
        int amt = buffer.readInt();
        FluidStack fluidStack = FluidRegistry.getFluidStack(new String(name, StandardCharsets.UTF_8), amt);
        return fromFluidStack(fluidStack);
    }

    public AEFluidStack getAEStack() {
        return AEFluidStack.fromFluidStack(this.delegate);
    }

    @Nonnull
    public FluidStack getDelegate() {
        return this.delegate;
    }

    @Override
    public FluidStack getFluidStack() {
        return this.delegate.copy();
    }

    @Override
    public void add(IAEFluidStack iaeFluidStack) {
        this.delegate.amount += iaeFluidStack.getStackSize();
    }

    @Override
    public long getStackSize() {
        return this.delegate.amount;
    }

    @Override
    public IAEFluidStack setStackSize(long l) {
        this.delegate.amount = (int) l;
        return this;
    }

    @Override
    public long getCountRequestable() {
        return 0;
    }

    @Override
    public IAEFluidStack setCountRequestable(long l) {
        return this;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEFluidStack setCraftable(boolean b) {
        return this;
    }

    @Override
    public IAEFluidStack reset() {
        this.delegate.amount = 0;
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return this.delegate.amount > 0;
    }

    @Override
    public void incStackSize(long l) {
        this.delegate.amount += l;
    }

    @Override
    public void decStackSize(long l) {
        this.delegate.amount -= l;
    }

    @Override
    public void incCountRequestable(long l) {
        // NO-OP
    }

    @Override
    public void decCountRequestable(long l) {
        // NO-OP
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        this.delegate.writeToNBT(nbtTagCompound);
    }

    @Override
    public boolean fuzzyComparison(IAEFluidStack stack, FuzzyMode fuzzyMode) {
        return this.delegate.getFluid() == stack.getFluid();
    }

    @Override
    public void writeToPacket(ByteBuf buffer) {
        byte[] name = this.delegate.getFluid().getName().getBytes(StandardCharsets.UTF_8);
        buffer.writeByte((byte)name.length);
        buffer.writeBytes(name);
        buffer.writeInt(this.delegate.amount);
    }

    @Override
    public IAEFluidStack copy() {
        return new WrappedFluidStack(this.delegate.copy());
    }

    @Override
    public IAEFluidStack empty() {
        IAEFluidStack dup = new WrappedFluidStack(this.delegate.copy());
        dup.reset();
        return dup;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        return ItemStack.EMPTY;
    }

    @Override
    public Fluid getFluid() {
        return this.delegate.getFluid();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof WrappedFluidStack) {
            return ((WrappedFluidStack) other).delegate.isFluidEqual(this.delegate);
        } else if (other instanceof FluidStack) {
            return ((FluidStack) other).isFluidEqual(this.delegate);
        } return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.delegate.getFluid().hashCode();
        result = 31 * result + (this.delegate.tag == null ? 0 : this.delegate.tag.hashCode());
        return result;
    }
}
