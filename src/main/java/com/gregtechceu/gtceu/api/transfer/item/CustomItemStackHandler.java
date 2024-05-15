package com.gregtechceu.gtceu.api.transfer.item;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CustomItemStackHandler extends ItemStackHandler implements IContentChangeAware {

    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};
    @Getter
    @Setter
    protected Predicate<ItemStack> filter = stack -> true;

    public CustomItemStackHandler() {}

    public CustomItemStackHandler(ItemStack stack) {
        this(NonNullList.of(ItemStack.EMPTY, stack));
    }

    public CustomItemStackHandler(int size) {
        super(size);
    }

    public CustomItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return filter.test(stack);
    }

    public void onContentsChanged(int slot) {
        onContentsChanged.run();
    }

    public CustomItemStackHandler copy() {
        NonNullList<ItemStack> copiedStacks = NonNullList.withSize(this.stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            copiedStacks.set(i, stacks.get(i).copy());
        }
        CustomItemStackHandler copied = new CustomItemStackHandler(copiedStacks);
        copied.setFilter(this.filter);
        return copied;
    }
}
