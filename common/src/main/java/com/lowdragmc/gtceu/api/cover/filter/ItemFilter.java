package com.lowdragmc.gtceu.api.cover.filter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilter
 */
public interface ItemFilter extends Filter<ItemStack, ItemFilter> {

    Map<Item, Function<ItemStack, ItemFilter>> FILTERS = new HashMap<>();

    static ItemFilter loadFilter(ItemStack itemStack) {
        return FILTERS.get(itemStack.getItem()).apply(itemStack);
    }

}
