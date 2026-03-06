package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    protected TagItemFilter(String filterExpr) {
        setFilterExpr(filterExpr);
    }

    public static TagItemFilter loadFilter(ItemStack itemStack) {
        var expr = itemStack.getOrDefault(GTDataComponents.TAG_FILTER_EXPRESSION, "");
        var handler = new TagItemFilter(expr);
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                ((TagItemFilter) filter).tagFilterExpression);
        return handler;
    }

    public void setFilterExpr(String filterExpr) {
        cache.clear();
        super.setFilterExpr(filterExpr);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (tagFilterExpression.isEmpty()) return false;
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
