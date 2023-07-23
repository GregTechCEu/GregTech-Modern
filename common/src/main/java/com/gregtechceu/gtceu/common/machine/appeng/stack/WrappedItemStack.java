package com.gregtechceu.gtceu.common.machine.appeng.stack;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @Author GlodBlock
 * @Date 2023/4/22-21:02
 */
public class WrappedItemStack implements IAEItemStack {

    @Nonnull
    ItemStack delegate;

    private WrappedItemStack(@Nonnull ItemStack itemStack) {
        this.delegate = itemStack;
    }

    @Nullable
    public static WrappedItemStack fromItemStack(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? null : new WrappedItemStack(stack);
    }

    public static WrappedItemStack fromNBT(NBTTagCompound i) {
        if (i == null) {
            return null;
        } else {
            ItemStack itemstack = new ItemStack(i);
            return fromItemStack(itemstack);
        }
    }

    public static WrappedItemStack fromPacket(ByteBuf data) {
        return fromNBT(ByteBufUtils.readTag(data));
    }

    public AEItemStack getAEStack() {
        return AEItemStack.fromItemStack(this.delegate);
    }

    @Override
    public ItemStack createItemStack() {
        return this.delegate.copy();
    }

    @Override
    public boolean hasTagCompound() {
        return this.delegate.hasTagCompound();
    }

    @Override
    public void add(IAEItemStack iaeItemStack) {
        this.delegate.grow((int) iaeItemStack.getStackSize());
    }

    @Override
    public long getStackSize() {
        return this.delegate.getCount();
    }

    @Override
    public IAEItemStack setStackSize(long l) {
        this.delegate.setCount((int) l);
        return this;
    }

    @Override
    public long getCountRequestable() {
        return 0;
    }

    @Override
    public IAEItemStack setCountRequestable(long l) {
        return this;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack setCraftable(boolean b) {
        return this;
    }

    @Override
    public IAEItemStack reset() {
        this.delegate.setCount(0);
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return !this.delegate.isEmpty();
    }

    @Override
    public void incStackSize(long l) {
        this.delegate.grow((int) l);
    }

    @Override
    public void decStackSize(long l) {
        this.delegate.shrink((int) l);
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
    public boolean fuzzyComparison(IAEItemStack stack, FuzzyMode fuzzyMode) {
        return stack.createItemStack().isItemEqual(this.delegate);
    }

    @Override
    public void writeToPacket(ByteBuf byteBuf) {
        ByteBufUtils.writeTag(byteBuf, this.delegate.serializeNBT());
    }

    @Override
    public IAEItemStack copy() {
        return new WrappedItemStack(this.delegate.copy());
    }

    @Override
    public IAEItemStack empty() {
        IAEItemStack copy = this.copy();
        copy.reset();
        return copy;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        return this.delegate;
    }

    @Override
    public Item getItem() {
        return this.delegate.getItem();
    }

    @Override
    public int getItemDamage() {
        return this.delegate.getItemDamage();
    }

    @Override
    public boolean sameOre(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public boolean isSameType(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public boolean isSameType(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack getDefinition() {
        return this.delegate;
    }

    @Override
    public boolean equals(ItemStack itemStack) {
        return this.delegate.isItemEqual(itemStack);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IAEItemStack) {
            return this.delegate.isItemEqual(((IAEItemStack) other).createItemStack());
        } if (other instanceof ItemStack) {
            return this.delegate.isItemEqual((ItemStack) other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.delegate.getItem().hashCode();
        result = 31 * result + this.delegate.getItemDamage();
        result = 31 * result + (this.delegate.getTagCompound() == null ? 0 : this.delegate.getTagCompound().hashCode());
        return result;
    }

    @Override
    public ItemStack getCachedItemStack(long l) {
        ItemStack copy = this.delegate.copy();
        copy.setCount((int) l);
        return copy;
    }

    @Override
    public void setCachedItemStack(ItemStack itemStack) {
        this.delegate = itemStack;
    }
}
