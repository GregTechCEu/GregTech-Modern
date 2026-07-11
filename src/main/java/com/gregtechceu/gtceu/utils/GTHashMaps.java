package com.gregtechceu.gtceu.utils;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public final class GTHashMaps {

    private GTHashMaps() {}

    /**
     * Maps all items in the {@link IItemHandler} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @NotNull
    public static Object2IntMap<ItemStack> fromItemHandler(@NotNull IItemHandler inputs) {
        return fromItemHandler(inputs, false);
    }

    /**
     * Maps all items in the {@link IItemHandler} into a {@link ItemStack}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @param linked If the Map should be a Linked Map to preserve insertion order
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @NotNull
    public static Object2IntMap<ItemStack> fromItemHandler(@NotNull IItemHandler inputs, boolean linked) {
        final Object2IntMap<ItemStack> map = createItemStackMap(linked);

        // Create a single stack of the combined count for each item

        for (int i = 0; i < inputs.getSlots(); i++) {
            ItemStack stack = inputs.getStackInSlot(i);
            if (!stack.isEmpty()) {
                map.put(stack.copy(), map.getInt(stack) + stack.getCount());
            }
        }

        return map;
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStack}, {@link Integer} value as
     * amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @NotNull
    public static Object2IntMap<ItemStack> fromItemStackCollection(@NotNull Iterable<ItemStack> inputs) {
        return fromItemStackCollection(inputs, false);
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStack}, {@link Integer} value as
     * amount
     *
     * @param inputs The inventory handler of the inventory
     * @param linked If the Map should be a Linked Map to preserve insertion order
     * @return a {@link Map} of {@link ItemStack} and {@link Integer} as amount on the inventory
     */
    @NotNull
    public static Object2IntMap<ItemStack> fromItemStackCollection(@NotNull Iterable<ItemStack> inputs,
                                                                   boolean linked) {
        final Object2IntMap<ItemStack> map = createItemStackMap(linked);

        // Create a single stack of the combined count for each item

        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                map.put(stack.copy(), map.getInt(stack) + stack.getCount());
            }
        }

        return map;
    }

    @NotNull
    public static Object2IntMap<ItemStack> createItemStackMap(boolean linked) {
        ItemStackHashStrategy strategy = ItemStackHashStrategy.comparingAllButCount();
        return linked ? new Object2IntLinkedOpenCustomHashMap<>(strategy) : new Object2IntOpenCustomHashMap<>(strategy);
    }
}
