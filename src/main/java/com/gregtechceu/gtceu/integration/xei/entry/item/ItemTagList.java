package com.gregtechceu.gtceu.integration.xei.entry.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemTagList implements ItemEntryList {

    @Getter
    private final List<ItemTagEntry> entries = new ArrayList<>();

    public static ItemTagList of(@NotNull TagKey<Item> tag, int amount, @NotNull DataComponentPatch componentPatch) {
        var list = new ItemTagList();
        list.add(tag, amount, componentPatch);
        return list;
    }

    public void add(ItemTagEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull TagKey<Item> tag, int amount, @NotNull DataComponentPatch componentPatch) {
        add(new ItemTagEntry(tag, amount, componentPatch));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<ItemStack> getStacks() {
        return entries.stream()
                .flatMap(ItemTagEntry::stacks)
                .toList();
    }

    public record ItemTagEntry(@NotNull TagKey<Item> tag, int amount, @NotNull DataComponentPatch componentPatch) {

        public Stream<ItemStack> stacks() {
            return BuiltInRegistries.ITEM.getOrThrow(tag).stream()
                    .map(holder -> new ItemStack(holder, amount, componentPatch));
        }
    }
}
