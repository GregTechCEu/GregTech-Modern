package com.gregtechceu.gtceu.integration.xei.entry.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ItemTagList implements ItemEntryList {

    @Getter
    private final List<ItemTagEntry> entries = new ArrayList<>();

    public static ItemTagList of(@NotNull TagKey<Item> tag, int amount, @Nullable CompoundTag nbt) {
        var list = new ItemTagList();
        list.add(tag, amount, nbt);
        return list;
    }

    public void add(ItemTagEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull TagKey<Item> tag, int amount, @Nullable CompoundTag nbt) {
        add(new ItemTagEntry(tag, amount, nbt));
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

    public record ItemTagEntry(@NotNull TagKey<Item> tag, int amount, @Nullable CompoundTag nbt) {

        public Stream<ItemStack> stacks() {
            return BuiltInRegistries.ITEM.getTag(tag).map(HolderSet.ListBacked::stream).orElseGet(Stream::empty)
                    .map(holder -> stackWithTag(holder, amount, nbt));
        }
    }

    static ItemStack stackWithTag(Holder<Item> holder, int amount, @Nullable CompoundTag nbt) {
        ItemStack stack = new ItemStack(holder.value(), amount);
        stack.setTag(nbt);
        return stack;
    }
}
