package com.gregtechceu.gtceu.api.cover.filter;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/14
 * @implNote FluidFilter
 */
public interface FluidFilter extends Filter<FluidStack, FluidFilter> {

    Map<Item, Function<ItemStack, FluidFilter>> FILTERS = new HashMap<>();

    static FluidFilter loadFilter(ItemStack itemStack) {
        return FILTERS.get(itemStack.getItem()).apply(itemStack);
    }

}
