package com.gregtechceu.gtceu.api.recipe;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class DummyCraftingContainer extends TransientCraftingContainer {

    private final IItemTransfer itemTransfer;

    public DummyCraftingContainer(IItemTransfer itemHandler) {
        super(null, 0, 0);
        this.itemTransfer = itemHandler;
    }

    @Override
    public int getContainerSize() {
        return this.itemTransfer.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            if (!this.getItem(slot).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.itemTransfer.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return this.itemTransfer.extractItem(slot, Integer.MAX_VALUE, false, false);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        return this.itemTransfer.extractItem(slot, count, false, true);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.itemTransfer.setStackInSlot(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.itemTransfer.getSlots(); ++i) {
            this.itemTransfer.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void fillStackedContents(@NotNull StackedContents helper) {}

    private static NonNullList<ItemStack> createInventory(IItemTransfer itemHandler) {
        NonNullList<ItemStack> inv = NonNullList.create();

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);

            if (stack.isEmpty())
                continue;

            ItemStack stackCopy = stack.copy();
            inv.add(stackCopy);
        }

        return inv;
    }
}
