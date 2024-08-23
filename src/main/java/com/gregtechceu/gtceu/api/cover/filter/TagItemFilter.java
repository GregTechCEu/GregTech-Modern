package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.cover.filter.MatchResult;
import com.gregtechceu.gtceu.utils.OreDictExprFilter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote TagItemFilter
 */
public class TagItemFilter extends TagFilter<ItemStack, ItemFilter> implements ItemFilter {

    private final Object2BooleanMap<Item> cache = new Object2BooleanOpenHashMap<>();

    @Getter
    protected int maxStackSize = 1;

    protected TagItemFilter() {}

    public static TagItemFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static TagItemFilter loadFilter(CompoundTag tag, Consumer<ItemFilter> itemWriter) {
        var handler = new TagItemFilter();
        handler.itemWriter = itemWriter;
        handler.oreDictFilterExpression = tag.getString("oreDict");
        handler.matchRules.clear();
        handler.cache.clear();
        OreDictExprFilter.parseExpression(handler.matchRules, handler.oreDictFilterExpression);
        return handler;
    }

    @Override
    public CompoundTag saveFilter() {
        CompoundTag tag = super.saveFilter();
        tag.putString("type", FilterType.FLUID_TAG.getSerializedName());
        return tag;
    }

    @Override
    public int getMaxTransferSize() {
        return maxStackSize;
    }

    @Override
    public void setMaxTransferSize(int transferRate) {
        transferRate = Mth.clamp(transferRate, 1, Integer.MAX_VALUE);
        if (this.maxStackSize != transferRate) {
            this.maxStackSize = transferRate;
            onUpdated.accept(this);
        }
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

    @Override
    public MatchResult apply(ItemStack itemStack) {
        var match = OreDictExprFilter.matchesOreDict(matchRules, itemStack);
        return MatchResult.create(match != isBlackList(), match ? itemStack.copy() : ItemStack.EMPTY, -1);
    }
}
