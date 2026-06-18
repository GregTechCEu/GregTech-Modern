package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    public TagItemFilter(ItemStack stack) {
        itemWriter = filter -> stack.setTag(filter.saveFilter());
        var tag = stack.getOrCreateTag();

        filterString = tag.getString("oreDict");
        matchExpr = null;
        cache.clear();
        matchExpr = TagExprFilter.parseExpression(filterString);
    }

    public void setFilterString(String oreDict) {
        cache.clear();
        super.setFilterString(oreDict);
    }

    @Override
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        return super.getFilterUI(data, syncManager, settings);
    }

    @Override
    protected ItemStack getFilterItem() {
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
