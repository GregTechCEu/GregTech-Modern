package com.gregtechceu.gtceu.api.graphnet.predicate.test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class ItemTestObject implements IPredicateTestObject, Predicate<ItemStack> {

    public final Item item;
    public final int meta;
    public final CompoundTag tag;

    public final int stackLimit;

    private final int hash;

    public ItemTestObject(@NotNull ItemStack stack) {
        item = stack.getItem();
        meta = stack.getMetadata();
        tag = stack.getTagCompound();
        stackLimit = stack.getMaxStackSize();
        this.hash = Objects.hash(item, meta, tag);
    }

    @Override
    @Contract(" -> new")
    public ItemStack recombine() {
        return new ItemStack(item, 1, meta, tag);
    }

    @Contract("_ -> new")
    public ItemStack recombine(int amount) {
        assert amount <= getStackLimit() && amount > 0;
        return new ItemStack(item, amount, meta, tag);
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.stackLimit == stack.getMaxStackSize() && this.item == stack.getItem() &&
                this.meta == stack.getMetadata()) {
            CompoundTag other = stack.getTagCompound();
            return Objects.equals(this.tag, other);
        }
        return false;
    }

    public int getStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTestObject that = (ItemTestObject) o;
        return meta == that.meta && Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
