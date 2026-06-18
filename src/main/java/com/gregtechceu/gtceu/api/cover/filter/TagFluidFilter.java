package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.factory.GuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {

    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    public TagFluidFilter(ItemStack stack) {
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
    protected ItemStack getFilterItem() {
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
    public Flow getFilterUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        return super.getFilterUI(data, syncManager, settings);
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
