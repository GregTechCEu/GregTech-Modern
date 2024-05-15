package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote IOItemTransferList
 */
public class IOItemTransferList extends ItemTransferList {

    @Getter
    private final IO io;

    public IOItemTransferList(List<IItemHandlerModifiable> transfers, IO io, Predicate<ItemStack> filter) {
        super(transfers);
        this.io = io;
        setFilter(filter);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (io != IO.IN && io != IO.BOTH) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (io != IO.OUT && io != IO.BOTH) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }
}
