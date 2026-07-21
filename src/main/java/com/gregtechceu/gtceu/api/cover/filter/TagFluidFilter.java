package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {

    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    protected TagFluidFilter(String filterExpr) {
        this.setFilterString(filterExpr);
    }

    public static TagFluidFilter loadFilter(ItemStack itemStack) {
        var expr = itemStack.getOrDefault(GTDataComponents.TAG_FILTER_EXPRESSION, "");
        var handler = new TagFluidFilter(expr);
        handler.itemWriter = filter -> itemStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                ((TagFluidFilter) filter).filterString);
        return handler;
    }

    public void setFilterString(String oreDict) {
        cache.clear();
        super.setFilterString(oreDict);
    }

    @Override
    public ItemStack getFilterItem() {
        return GTItems.TAG_FLUID_FILTER.asStack();
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        if (filterString.isEmpty()) return false;
        if (cache.containsKey(fluidStack.getFluid())) return cache.getOrDefault(fluidStack.getFluid(), false);
        if (TagExprFilter.tagsMatch(matchExpr, fluidStack)) {
            cache.put(fluidStack.getFluid(), true);
            return true;
        }
        cache.put(fluidStack.getFluid(), false);
        return false;
    }

    @Override
    public int testFluidAmount(FluidStack fluidStack) {
        return test(fluidStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
