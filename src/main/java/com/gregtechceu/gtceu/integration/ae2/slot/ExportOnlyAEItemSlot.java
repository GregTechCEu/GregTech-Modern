package com.gregtechceu.gtceu.integration.ae2.slot;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExportOnlyAEItemSlot extends ExportOnlyAESlot implements IItemHandlerModifiable {

    public ExportOnlyAEItemSlot() {
        super();
    }

    public ExportOnlyAEItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
        super(config, stock);
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
    public void setStock(@Nullable GenericStack stack) {
        if (this.stock == null && stack == null) {
            return;
        } else if (stack == null) {
            this.stock = null;
        } else {
            if (stack.equals(stock)) return;
            this.stock = stack;
        }
        onContentsChanged();
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        // NO-OP
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0 && this.stock != null) {
            return this.stock.what() instanceof AEItemKey itemKey ?
                    itemKey.toStack(GTMath.saturatedCast(this.stock.amount())) :
                    ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
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
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0 && this.stock != null) {
            int extracted = (int) Math.min(this.stock.amount(), amount);
            if (!(this.stock.what() instanceof AEItemKey itemKey)) return ItemStack.EMPTY;
            ItemStack result = itemKey.toStack(extracted);
            if (!simulate) {
                this.stock = ExportOnlyAESlot.copy(this.stock, this.stock.amount() - extracted);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
            }
            onContentsChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    public void onContentsChanged() {
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    @Override
    public ExportOnlyAEItemSlot copy() {
        return new ExportOnlyAEItemSlot(
                this.config == null ? null : copy(this.config),
                this.stock == null ? null : copy(this.stock));
    }
}
