package com.gregtechceu.gtceu.api.transfer.item;

import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class BigItemStackTransfer extends ItemStackTransfer {

    @Getter
    private final boolean acceptTag;

    private final int slotLimit;

    public BigItemStackTransfer(boolean acceptTag, int slotLimit) {
        this.acceptTag = acceptTag;
        this.slotLimit = slotLimit;
    }

    public BigItemStackTransfer(int size, boolean acceptTag, int slotLimit) {
        super(size);
        this.acceptTag = acceptTag;
        this.slotLimit = slotLimit;
    }

    public BigItemStackTransfer(NonNullList<ItemStack> stacks, boolean acceptTag, int slotLimit) {
        super(stacks);
        this.acceptTag = acceptTag;
        this.slotLimit = slotLimit;
    }

    public BigItemStackTransfer(ItemStack stack, boolean acceptTag, int slotLimit) {
        super(stack);
        this.acceptTag = acceptTag;
        this.slotLimit = slotLimit;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (amount == 0) return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, getSlotLimit(slot));

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                if (notifyChanges) {
                    onContentsChanged(slot);
                }
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(
                        slot, ItemTransferHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                if (notifyChanges) {
                    onContentsChanged(slot);
                }
            }

            return ItemTransferHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        if (!acceptTag && stack.hasTag()) return 0;
        return stack.isStackable() ? getSlotLimit(slot) : 1;
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                GTUtil.saveItemStack(stacks.get(i), itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, GTUtil.loadItemStack(itemTags));
            }
        }
        onLoad();
    }

    @Override
    public BigItemStackTransfer copy() {
        var copiedStack = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            copiedStack.set(i, stacks.get(i).copy());
        }
        var copied = new BigItemStackTransfer(copiedStack, acceptTag, slotLimit);
        // copied.setFilter(((ItemStackTransferAccessor) this).getFilter()); //TODO Why not modify the ldlib directly?
        return copied;
    }
}
