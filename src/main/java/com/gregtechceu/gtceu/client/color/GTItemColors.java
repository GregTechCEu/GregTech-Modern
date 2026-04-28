package com.gregtechceu.gtceu.client.color;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class GTItemColors {

    private static final Map<Item, ItemColor> COLORS = new ConcurrentHashMap<>();

    private GTItemColors() {}

    public static void register(Item item, Object value) {
        COLORS.put(item, adapt(value));
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        ItemColor color = COLORS.get(stack.getItem());
        return color == null ? -1 : color.getColor(stack, tintIndex);
    }

    public static ItemColor adapt(Object value) {
        if (value instanceof Supplier<?> supplier) {
            return adapt(supplier.get());
        }
        if (value instanceof ItemColor itemColor) {
            return itemColor;
        }
        throw new IllegalArgumentException("Unsupported item color: " + value);
    }
}
