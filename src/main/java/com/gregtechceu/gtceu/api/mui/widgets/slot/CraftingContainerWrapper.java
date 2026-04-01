package com.gregtechceu.gtceu.api.mui.widgets.slot;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A crafting inventory that wraps a {@link IItemHandlerModifiable}.
 * This inventory creates a content list which is used here to detect changes from the item handler.
 * This is required as interacting with a slot will update the content, but will not notify the container to check for
 * new recipes.
 */
public class CraftingContainerWrapper extends TransientCraftingContainer {

    @Getter
    private final IItemHandlerModifiable delegate;
    private final ModularCraftingSlot slot;
    private final int size;
    @Getter
    private final int startIndex;

    public CraftingContainerWrapper(ModularCraftingSlot slot, int width, int height,
                                    IItemHandlerModifiable delegate, int startIndex) {
        super(slot.getSyncHandler().getSyncManager().getContainer(), width, height);
        this.slot = slot;
        this.size = width * height;
        this.delegate = delegate;
        this.startIndex = startIndex;

        if (startIndex + this.size > delegate.getSlots()) {
            throw new IllegalArgumentException("Inventory does not have enough slots for given size. Requires " +
                    (startIndex + this.size) + " slots, but only has " + delegate.getSlots() + " slots!");
        }
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.size; i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.size) return ItemStack.EMPTY;
        return this.delegate.getStackInSlot(slot + this.startIndex);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= this.size) return;
        this.setSlot(slot + this.startIndex, stack, true);
    }

    public void setSlot(int slot, @NotNull ItemStack stack, boolean notify) {
        this.delegate.setStackInSlot(slot + this.startIndex, stack);
        if (notify) this.notifyContainer();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= this.size || amount <= 0) return ItemStack.EMPTY;

        ItemStack stack = this.delegate.extractItem(slot + this.startIndex, amount, false);

        if (!stack.isEmpty()) {
            this.notifyContainer();
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= this.size) return ItemStack.EMPTY;

        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.setSlot(slot, ItemStack.EMPTY, false);
        return stack;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; i++) {
            this.setSlot(i, ItemStack.EMPTY, false);
        }
    }

    @Override
    public @NotNull List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < this.size; i++) {
            items.add(this.getItem(i));
        }
        return items;
    }

    @Override
    public void fillStackedContents(@NotNull StackedContents contents) {
        for (int i = 0; i < this.size; i++) {
            contents.accountStack(this.getItem(i));
        }
    }

    public void notifyContainer() {
        this.slot.setChanged();
    }
}
