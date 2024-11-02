package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.OreDictExprFilter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote TagItemFilter
 */
public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    protected TagItemFilter(String tag) {
        oreDictFilterExpression = tag;
        OreDictExprFilter.parseExpression(matchRules, oreDictFilterExpression);
    }

    public static TagItemFilter loadFilter(ItemStack itemStack) {
        var expr = itemStack.getOrDefault(GTDataComponents.TAG_FILTER_EXPRESSION, "");
        var handler = new TagItemFilter(expr);
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                ((TagItemFilter) filter).oreDictFilterExpression);
        return handler;
    }

    public void setOreDict(String oreDict) {
        cache.clear();
        super.setOreDict(oreDict);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (oreDictFilterExpression.isEmpty()) return true;
        if (cache.containsKey(itemStack.getItem())) return cache.getOrDefault(itemStack.getItem(), false);
        if (OreDictExprFilter.matchesOreDict(matchRules, itemStack)) {
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
