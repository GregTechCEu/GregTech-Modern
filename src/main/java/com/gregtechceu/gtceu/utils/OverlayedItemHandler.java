package com.gregtechceu.gtceu.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class OverlayedItemHandler {

    private final OverlayedItemHandlerSlot[] originalSlots;
    private final OverlayedItemHandlerSlot[] slots;
    private final IItemHandlerModifiable overlayedHandler;

    public OverlayedItemHandler(@NotNull IItemHandlerModifiable toOverlay) {
        this.slots = new OverlayedItemHandlerSlot[toOverlay.getSlots()];
        this.originalSlots = new OverlayedItemHandlerSlot[toOverlay.getSlots()];
        this.overlayedHandler = toOverlay;
    }

    /**
     * Resets the {slots} array to the state when the handler was
     * first mirrored
     */
    public void reset() {
        for (int i = 0; i < this.originalSlots.length; i++) {
            if (this.originalSlots[i] != null) {
                this.slots[i] = this.originalSlots[i].copy();
            }
        }
    }

    public int getSlots() {
        return overlayedHandler.getSlots();
    }

    /**
     * Populates the {@code originalSlots} and {@code slots}arrays with the current state of the inventory.
     *
     * @param slot the slot to populate
     */

    private void initSlot(int slot) {
        if (this.originalSlots[slot] == null) {
            ItemStack stackToMirror = overlayedHandler.getStackInSlot(slot);
            int slotLimit = overlayedHandler.getSlotLimit(slot);
            this.originalSlots[slot] = new OverlayedItemHandlerSlot(stackToMirror, slotLimit);
            this.slots[slot] = new OverlayedItemHandlerSlot(stackToMirror, slotLimit);
        }
    }

    public int insertStackedItemStack(@NotNull ItemStack stack, int amountToInsert) {
        int lastKnownPopulatedSlot = 0;
        // loop through all slots, looking for ones matching the key
        for (int i = 0; i < this.slots.length; i++) {
            // populate the slot if it's not already populated
            initSlot(i);
            // if it's the same item or there is no item in the slot
            ItemStack slotKey = this.slots[i].getItemStack();
            if (slotKey.isEmpty() || ItemStack.isSameItemSameTags(slotKey, stack)) {
                // if the slot is not full
                int canInsertUpTo = Math.min(this.slots[i].getSlotLimit() - this.slots[i].getCount(),
                        stack.getMaxStackSize());
                if (canInsertUpTo > 0) {
                    int insertedAmount = Math.min(canInsertUpTo, amountToInsert);
                    this.slots[i].setItemStack(stack.copy()); // this copy may not be need, needs further tests
                    this.slots[i].setCount(this.slots[i].getCount() + insertedAmount);
                    amountToInsert -= insertedAmount;
                }
            }
            lastKnownPopulatedSlot = i;

            // early exit if finished inserting everything
            if (amountToInsert == 0) {
                return 0;
            }
        }

        // if the amountToInsert is still greater than 0, we need to insert it into a new slot
        if (amountToInsert > 0) {
            // loop through all slots, starting from after the last seen slot with items in it, looking for empty ones.
            for (int i = lastKnownPopulatedSlot + 1; i < this.slots.length; i++) {
                OverlayedItemHandlerSlot slot = this.slots[i];
                // if the slot is empty
                if (slot.getItemStack().isEmpty()) {
                    int canInsertUpTo = Math.min(stack.getMaxStackSize(), slot.getSlotLimit());
                    if (canInsertUpTo > 0) {
                        int insertedAmount = Math.min(canInsertUpTo, amountToInsert);
                        slot.setItemStack(stack.copy()); // this copy may not be need, needs further tests
                        slot.setCount(insertedAmount);
                        amountToInsert -= insertedAmount;
                    }
                    if (amountToInsert == 0) {
                        return 0;
                    }
                }
            }
        }
        // return the amount that wasn't inserted
        return amountToInsert;
    }

    private static class OverlayedItemHandlerSlot {

        private ItemStack itemStack = ItemStack.EMPTY;
        private int count = 0;
        private int slotLimit;

        protected OverlayedItemHandlerSlot(@NotNull ItemStack stackToMirror, int slotLimit) {
            if (!stackToMirror.isEmpty()) {
                this.itemStack = stackToMirror.copy();
                this.count = stackToMirror.getCount();
                this.slotLimit = Math.min(itemStack.getMaxStackSize(), slotLimit);
            } else {
                this.slotLimit = slotLimit;
            }
        }

        protected OverlayedItemHandlerSlot(@NotNull ItemStack itemStack, int slotLimit, int count) {
            this.itemStack = itemStack;
            this.count = count;
            this.slotLimit = slotLimit;
        }

        public int getSlotLimit() {
            return slotLimit;
        }

        public int getCount() {
            return count;
        }

        /**
         * Storage of this ItemStack elsewhere will require copying it
         * 
         * @return the stored ItemStack
         */
        @NotNull
        public ItemStack getItemStack() {
            return this.itemStack;
        }

        public void setItemStack(@NotNull ItemStack itemStack) {
            if (!ItemStack.isSameItemSameTags(this.itemStack, itemStack)) {
                this.itemStack = itemStack;
                this.slotLimit = Math.min(itemStack.getMaxStackSize(), slotLimit);
            }
        }

        public void setCount(int count) {
            this.count = count;
        }

        @NotNull
        OverlayedItemHandlerSlot copy() {
            return new OverlayedItemHandlerSlot(this.itemStack, this.slotLimit, this.count);
        }
    }
}
