package brachy.modularui.integration.recipeviewer.entry.item;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public record ItemStackList(List<ItemStack> stacks) implements ItemEntryList {

    public ItemStackList() {
        this(new ArrayList<>());
    }

    public ItemStackList(List<ItemStack> stacks) {
        this.stacks = new ArrayList<>(stacks);
    }

    public static ItemStackList of(ItemStack stack) {
        var list = new ItemStackList();
        list.add(stack);
        return list;
    }

    public static ItemStackList of(Collection<ItemStack> coll) {
        var list = new ItemStackList();
        list.addAll(coll);
        return list;
    }

    public void add(ItemStack stack) {
        stacks.add(stack);
    }

    public void addAll(Collection<ItemStack> list) {
        stacks.addAll(list);
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public List<ItemStack> getStacks() {
        return stacks;
    }

    public Stream<ItemStack> stream() {
        return stacks.stream();
    }
}
