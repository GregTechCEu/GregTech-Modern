package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * An extension of {@link CombinedInvWrapper} that inserts only in the given
 * IO direction, testing any item transfers through the given filters before execution
 */
public class IOFilteredInvWrapper extends CombinedInvWrapper {

    private final IO io;
    private final Predicate<ItemStack> inFilter;
    private final Predicate<ItemStack> outFilter;

    public IOFilteredInvWrapper(List<IItemHandlerModifiable> handlers, IO io, Predicate<ItemStack> inFilter,
                                Predicate<ItemStack> outFilter) {
        super(handlers.toArray(IItemHandlerModifiable[]::new));
        this.io = io;
        this.inFilter = inFilter;
        this.outFilter = outFilter;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!io.support(IO.IN) || !inFilter.test(stack)) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!io.support(IO.OUT)) return ItemStack.EMPTY;
        var stack = super.extractItem(slot, amount, true);
        if (stack.isEmpty() || !outFilter.test(stack)) return ItemStack.EMPTY;
        return simulate ? stack : super.extractItem(slot, amount, false);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        Predicate<ItemStack> filter = s -> true;
        if (io.support(IO.IN)) filter = inFilter.and(filter);
        if (io.support(IO.OUT)) filter = outFilter.and(filter);
        return filter.test(stack) && super.isItemValid(slot, stack);
    }
}
