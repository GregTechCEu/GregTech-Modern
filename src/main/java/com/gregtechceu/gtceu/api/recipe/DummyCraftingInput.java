package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class DummyCraftingInput extends CraftingInput {

    private final IItemHandler itemTransfer;

    public DummyCraftingInput(IItemHandler itemHandler) {
        super(0, 0, createInventory(itemHandler));
        this.itemTransfer = itemHandler;
    }

    @Override
    public int size() {
        return this.itemTransfer.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.size(); slot++) {
            if (!this.getItem(slot).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return slot >= this.size() || slot < 0 ? ItemStack.EMPTY : this.itemTransfer.getStackInSlot(slot);
    }

    private static NonNullList<ItemStack> createInventory(IItemHandler itemHandler) {
        NonNullList<ItemStack> inv = NonNullList.withSize(itemHandler.getSlots(), ItemStack.EMPTY);

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);

            if (stack.isEmpty())
                continue;

            ItemStack stackCopy = stack.copy();
            inv.set(slot, stackCopy);
        }

        return inv;
    }
}
