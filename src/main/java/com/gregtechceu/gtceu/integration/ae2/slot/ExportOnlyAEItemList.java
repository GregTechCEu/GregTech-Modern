package com.gregtechceu.gtceu.integration.ae2.slot;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExportOnlyAEItemList extends NotifiableItemStackHandler implements IConfigurableSlotList {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ExportOnlyAEItemList.class,
            NotifiableItemStackHandler.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    protected ExportOnlyAEItemSlot[] inventory;

    private ItemStackTransfer itemTransfer;

    public ExportOnlyAEItemList(MetaMachine holder, int slots) {
        super(holder, 0, IO.IN);
        this.inventory = new ExportOnlyAEItemSlot[slots];
        for (int i = 0; i < slots; i++) {
            this.inventory[i] = new ExportOnlyAEItemSlot(null, null);
        }
        for (ExportOnlyAEItemSlot slot : this.inventory) {
            slot.setOnContentsChanged(this::onContentsChanged);
        }
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        this.machine.onChanged();
    }

    public ItemStackTransfer getTransfer() {
        if (this.itemTransfer == null) {
            this.itemTransfer = new ItemStackTransferDelegate(inventory);
        }
        return itemTransfer;
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                              @Nullable String slotName, boolean simulate) {
        return handleIngredient(io, recipe, left, simulate, this.handlerIO, getTransfer());
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        // NO-OP
    }

    @Override
    public int getSlots() {
        return inventory.length;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < inventory.length) {
            return this.inventory[slot].getStackInSlot(0);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= 0 && slot < inventory.length) {
            return this.inventory[slot].extractItem(0, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public IConfigurableSlot getConfigurableSlot(int index) {
        return inventory[index];
    }

    @Override
    public int getConfigurableSlots() {
        return inventory.length;
    }

    public boolean isAutoPull() {
        return false;
    }

    public boolean isStocking() {
        return false;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static class ItemStackTransferDelegate extends ItemStackTransfer {

        private final ExportOnlyAEItemSlot[] inventory;

        public ItemStackTransferDelegate(ExportOnlyAEItemSlot[] inventory) {
            super();
            this.inventory = inventory;
        }

        @Override
        public int getSlots() {
            return inventory.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory[slot].getStackInSlot(0);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            // NO-OP
        }

        @Override
        public ItemStack insertItem(
                                    int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            if (amount == 0) return ItemStack.EMPTY;
            validateSlotIndex(slot);
            return inventory[slot].extractItem(0, amount, simulate);
        }

        @Override
        protected void validateSlotIndex(int slot) {
            if (slot < 0 || slot >= getSlots())
                throw new RuntimeException(
                        "Slot " + slot + " not in valid range - [0," + getSlots() + ")");
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStackTransfer copy() {
            // because recipe testing uses copy transfer instead of simulated operations
            return new ItemStackTransferDelegate(inventory) {

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                    return super.extractItem(slot, amount, true, notifyChanges);
                }
            };
        }
    }
}
