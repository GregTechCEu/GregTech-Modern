package com.gregtechceu.gtceu.integration.xei.entry.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemHolderSetList implements ItemEntryList {

    @Getter
    private final List<ItemHolderSetEntry> entries = new ArrayList<>();

    public static ItemHolderSetList of(@NotNull Holder<Item> set, int amount, @NotNull DataComponentPatch patch) {
        var list = new ItemHolderSetList();
        list.add(set, amount, patch);
        return list;
    }

    public void add(ItemHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull Holder<Item> set, int amount, @NotNull DataComponentPatch patch) {
        add(new ItemHolderSetEntry(set, amount, patch));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<ItemStack> getStacks() {
        return entries.stream()
                .flatMap(ItemHolderSetEntry::stacks)
                .toList();
    }

    public record ItemHolderSetEntry(@NotNull Holder<Item> set, int amount, @NotNull DataComponentPatch patch) {

        public Stream<ItemStack> stacks() {
            // return set.map(holder -> );
            return Stream.of(new ItemStack(set, amount, patch));
        }
    }
}
