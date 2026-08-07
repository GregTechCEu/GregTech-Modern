package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    protected TagItemFilter(String filterExpr) {
        setFilterString(filterExpr);
    }

    public static TagItemFilter loadFilter(ItemStack itemStack) {
        var expr = itemStack.getOrDefault(GTDataComponents.TAG_FILTER_EXPRESSION, "");
        var handler = new TagItemFilter(expr);
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                ((TagItemFilter) filter).filterString);
        return handler;
    }

    public void setFilterString(String oreDict) {
        cache.clear();
        super.setFilterString(oreDict);
    }

    @Override
    public ItemStack getFilterItem() {
        return GTItems.TAG_FILTER.asStack();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (filterString.isEmpty()) return false;
        if (cache.containsKey(itemStack.getItem())) return cache.getOrDefault(itemStack.getItem(), false);
        if (TagExprFilter.tagsMatch(matchExpr, itemStack)) {
            cache.put(itemStack.getItem(), true);
            return true;
        }
        cache.put(itemStack.getItem(), false);
        return false;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        return test(itemStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
