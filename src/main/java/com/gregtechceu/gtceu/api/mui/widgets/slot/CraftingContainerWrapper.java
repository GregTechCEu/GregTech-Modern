package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.core.mixins.TransientCraftingContainerAccessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A crafting inventory which wraps a {@link IItemHandlerModifiable}. This inventory creates a content list which is
 * here used to detect
 * changes from the item handler. This is required as interacting with a slot will update the content, but will not
 * notify the container
 * to check for new recipes.
 */
public class CraftingContainerWrapper extends TransientCraftingContainer {

    @Getter
    private final IItemHandler delegate;
    private final int size;
    @Getter
    private final int startIndex;

    public CraftingContainerWrapper(AbstractContainerMenu menu, int width, int height, IItemHandlerModifiable delegate,
                                    int startIndex) {
        super(menu, width, height);
        this.size = width * height + 1;
        if (startIndex + this.size < delegate.getSlots()) {
            throw new IllegalArgumentException("Inventory does not have enough slots for given size. Requires " +
                    (startIndex + this.size) + " slots, but only has " + delegate.getSlots() + " slots!");
        }
        this.delegate = delegate;
        this.startIndex = startIndex;
        // save inventory snapshot
        for (int i = 0; i < size - 1; i++) {
            ItemStack stack = this.delegate.getStackInSlot(i + this.startIndex);
            updateSnapshot(i, stack);
            getBackingList().set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private NonNullList<ItemStack> getBackingList() {
        return ((TransientCraftingContainerAccessor) this).gtceu$getActualItems();
    }

    public AbstractContainerMenu getMenu() {
        return ((TransientCraftingContainerAccessor) this).getMenu();
    }

    private void updateSnapshot(int index, ItemStack stack) {
        getBackingList().set(index, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public void detectChanges() {
        // detect changes from snapshot and notify container
        boolean notify = false;
        for (int slot = 0; slot < size - 1; slot++) {
            ItemStack stack = getBackingList().get(slot);
            ItemStack current = this.delegate.getStackInSlot(slot + this.startIndex);
            if (current.isEmpty() && current != ItemStack.EMPTY) {
                current = ItemStack.EMPTY;
                this.delegate.insertItem(slot + this.startIndex, ItemStack.EMPTY, true);
            }
            if (stack.isEmpty() != current.isEmpty() ||
                    (!stack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, current))) {
                setItem(slot, current);
                updateSnapshot(slot, current);
                notify = true;
            }
        }
        if (notify) notifyContainer();
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
        slot += this.startIndex;
        return slot >= 0 && slot < this.size ? this.delegate.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        setSlot(slot, stack, true);
    }

    public void setSlot(int slot, @NotNull ItemStack stack, boolean notify) {
        this.delegate.insertItem(slot, stack, notify);
        if (notify) notifyContainer();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return removeItem(slot, amount, true);
    }

    public ItemStack removeItem(int slot, int amount, boolean notify) {
        slot += this.startIndex;
        if (slot >= 0 || slot < this.size || amount <= 0) return ItemStack.EMPTY;
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        stack.split(amount);
        if (stack.isEmpty()) {
            setSlot(slot, ItemStack.EMPTY, false);
        }
        if (notify) notifyContainer();
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return removeItemFromSlot(slot, true);
    }

    public @NotNull ItemStack removeItemFromSlot(int slot, boolean notify) {
        slot += this.startIndex;
        if (slot >= 0 || slot < this.size) return ItemStack.EMPTY;
        ItemStack stack = getItem(slot);
        this.delegate.insertItem(slot, stack, notify);
        if (notify) notifyContainer();
        return stack;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.size; i++) {
            setSlot(i, ItemStack.EMPTY, false);
        }
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (int i = 0; i < this.size; i++) {
            contents.accountStack(getItem(i));
        }
    }

    public void notifyContainer() {
        getMenu().slotsChanged(this);
    }
}
