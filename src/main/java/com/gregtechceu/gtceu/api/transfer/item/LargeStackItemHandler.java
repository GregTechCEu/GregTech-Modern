package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/** Item handler whose per-slot capacity may exceed an item's normal stack size. */
public class LargeStackItemHandler extends CustomItemStackHandler {

    private final int multiplier;

    public LargeStackItemHandler(int size, int multiplier) {
        super(size);
        this.multiplier = multiplier;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;

        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int extracted = Math.min(amount, getStackLimit(slot, existing));
        if (existing.getCount() <= extracted) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing.copy();
        }

        if (!simulate) {
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - extracted));
            onContentsChanged(slot);
        }
        return ItemHandlerHelper.copyStackWithSize(existing, extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return multiplier == Integer.MAX_VALUE ? Integer.MAX_VALUE : 64 * multiplier;
    }

    @Override
    public int getStackLimit(int slot, @NotNull ItemStack stack) {
        return multiplier == Integer.MAX_VALUE ? Integer.MAX_VALUE :
                Math.min(getSlotLimit(slot), stack.getMaxStackSize() * multiplier);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag entries = new ListTag();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) continue;

            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.put("Item", stack.save(new CompoundTag()));
            entry.putInt("Count", stack.getCount());
            entries.add(entry);
        }

        CompoundTag tag = new CompoundTag();
        tag.put("Items", entries);
        tag.putInt("Size", stacks.size());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setSize(tag.contains("Size", CompoundTag.TAG_INT) ? tag.getInt("Size") : stacks.size());
        Collections.fill(stacks, ItemStack.EMPTY);

        ListTag entries = tag.getList("Items", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot < 0 || slot >= stacks.size()) continue;

            ItemStack stack = ItemStack.of(entry.getCompound("Item"));
            if (entry.contains("Count", CompoundTag.TAG_INT)) {
                stack.setCount(entry.getInt("Count"));
            }
            stacks.set(slot, stack);
        }
        onLoad();
    }
}
