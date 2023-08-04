package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.utils.OreDictExprFilter;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote TagFluidFilter
 */
public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {
    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    protected TagFluidFilter() {
    }

    public static TagFluidFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static TagFluidFilter loadFilter(CompoundTag tag, Consumer<FluidFilter> itemWriter) {
        var handler = new TagFluidFilter();
        handler.itemWriter = itemWriter;
        handler.oreDictFilterExpression = tag.getString("oreDict");
        handler.matchRules.clear();
        handler.cache.clear();
        OreDictExprFilter.parseExpression(handler.matchRules, handler.oreDictFilterExpression);
        return handler;
    }

    public void setOreDict(String oreDict) {
        cache.clear();
        super.setOreDict(oreDict);
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        if (oreDictFilterExpression.isEmpty()) return true;
        if (cache.containsKey(fluidStack.getFluid())) return cache.getOrDefault(fluidStack.getFluid(), false);
        if (OreDictExprFilter.matchesOreDict(matchRules, fluidStack)) {
            cache.put(fluidStack.getFluid(), true);
            return true;
        }
        cache.put(fluidStack.getFluid(), false);
        return false;
    }

    @Override
    public long testFluidAmount(FluidStack fluidStack) {
        return test(fluidStack) ? Long.MAX_VALUE : 0;
    }
}
