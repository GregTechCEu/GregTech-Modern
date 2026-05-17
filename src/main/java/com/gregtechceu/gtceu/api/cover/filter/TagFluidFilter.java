package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.TagExprFilter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Objects;
import java.util.function.Consumer;

public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {

    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    protected TagFluidFilter() {}

    public static TagFluidFilter loadFilter(ItemStack itemStack) {
        return loadFilter(Objects.requireNonNullElseGet(itemStack.getTag(), CompoundTag::new),
                filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static TagFluidFilter loadFilter(CompoundTag tag, Consumer<FluidFilter> itemWriter) {
        var handler = new TagFluidFilter();
        handler.itemWriter = itemWriter;
        handler.filterString = tag.getString("oreDict");
        handler.matchExpr = null;
        handler.cache.clear();
        handler.matchExpr = TagExprFilter.parseExpression(handler.filterString);
        return handler;
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
    public int testFluidAmount(FluidStack fluidStack) {
        return test(fluidStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
