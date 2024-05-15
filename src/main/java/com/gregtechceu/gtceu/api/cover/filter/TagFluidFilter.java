package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.OreDictExprFilter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote TagFluidFilter
 */
public class TagFluidFilter extends TagFilter<FluidStack, FluidFilter> implements FluidFilter {

    public static final Codec<TagFluidFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tag").forGetter(val -> val.oreDictFilterExpression))
            .apply(instance, TagFluidFilter::loadFilter));
    private final Object2BooleanMap<Fluid> cache = new Object2BooleanOpenHashMap<>();

    protected TagFluidFilter() {}

    public static TagFluidFilter loadFilter(ItemStack itemStack) {
        return itemStack.get(GTDataComponents.TAG_FLUID_FILTER);
    }

    private static TagFluidFilter loadFilter(String oreDict) {
        var handler = new TagFluidFilter();
        // handler.itemWriter = itemWriter; // TODO fix
        handler.oreDictFilterExpression = oreDict;
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
    public int testFluidAmount(FluidStack fluidStack) {
        return test(fluidStack) ? Integer.MAX_VALUE : 0;
    }

    @Override
    public boolean supportsAmounts() {
        return false;
    }
}
