package brachy.modularui.integration.recipeviewer.entry.item;

import net.minecraft.core.HolderSet;
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

    public static ItemHolderSetList of(@NotNull HolderSet<Item> set, int amount, @NotNull DataComponentPatch componentPatch) {
        var list = new ItemHolderSetList();
        list.add(set, amount, componentPatch);
        return list;
    }

    public void add(ItemHolderSetEntry entry) {
        entries.add(entry);
    }

    public void add(@NotNull HolderSet<Item> set, int amount, @NotNull DataComponentPatch componentPatch) {
        add(new ItemHolderSetEntry(set, amount, componentPatch));
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

    public record ItemHolderSetEntry(@NotNull HolderSet<Item> set, int amount, @NotNull DataComponentPatch componentPatch) {

        public Stream<ItemStack> stacks() {
            return set.stream().map(holder -> ItemTagList.stackWithComponents(holder, amount, componentPatch));
        }
    }
}
