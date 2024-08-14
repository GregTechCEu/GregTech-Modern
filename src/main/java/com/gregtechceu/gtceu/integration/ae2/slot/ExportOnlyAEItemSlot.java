package com.gregtechceu.gtceu.integration.ae2.slot;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

public class ExportOnlyAEItemSlot extends ExportOnlyAESlot implements IItemTransfer {

    public ExportOnlyAEItemSlot() {
        super();
    }

    public ExportOnlyAEItemSlot(GenericStack config, GenericStack stock) {
        super(config, stock);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        // NO-OP
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        return stack;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0 && this.stock != null) {
            return this.stock.what() instanceof AEItemKey itemKey ? itemKey.toStack((int) this.stock.amount()) :
                    ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (slot == 0 && this.stock != null) {
            int extracted = (int) Math.min(this.stock.amount(), amount);
            ItemStack result = this.stock.what() instanceof AEItemKey itemKey ?
                    itemKey.toStack((int) this.stock.amount()) : ItemStack.EMPTY.copy();
            result.setCount(extracted);
            if (!simulate) {
                this.stock = ExportOnlyAESlot.copy(this.stock, this.stock.amount() - extracted);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
            }
            if (notifyChanges) {
                onContentsChanged();
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onContentsChanged() {
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    @Override
    public void addStack(GenericStack stack) {
        if (this.stock == null) {
            this.stock = stack;
        } else {
            this.stock = GenericStack.sum(this.stock, stack);
        }
        onContentsChanged();
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public ExportOnlyAEItemSlot copy() {
        return new ExportOnlyAEItemSlot(
                this.config == null ? null : copy(this.config),
                this.stock == null ? null : copy(this.stock));
    }

    @Deprecated
    @NotNull
    @Override
    public Object createSnapshot() {
        return Pair.of(this.config, this.stock);
    }

    @Deprecated
    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Pair<?, ?> pair) {
            this.config = (GenericStack) pair.getFirst();
            this.stock = (GenericStack) pair.getSecond();
        }
    }
}
