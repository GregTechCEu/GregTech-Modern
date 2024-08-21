package com.gregtechceu.gtceu.api.graphnet.predicate.test;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class ItemTestObject implements IPredicateTestObject, Predicate<ItemStack> {

    public final Item item;
    public final CompoundTag tag;

    public final int stackLimit;

    private final int hash;

    public ItemTestObject(@NotNull ItemStack stack) {
        item = stack.getItem();
        tag = stack.getTag();
        stackLimit = stack.getMaxStackSize();
        this.hash = Objects.hash(item, tag);
    }

    @Override
    @Contract(" -> new")
    public ItemStack recombine() {
        return new ItemStack(item, 1, tag);
    }

    @Contract("_ -> new")
    public ItemStack recombine(int amount) {
        assert amount <= getStackLimit() && amount > 0;
        return new ItemStack(item, amount, tag);
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.stackLimit == stack.getMaxStackSize() && this.item == stack.getItem()) {
            CompoundTag other = stack.getTag();
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
        return Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
