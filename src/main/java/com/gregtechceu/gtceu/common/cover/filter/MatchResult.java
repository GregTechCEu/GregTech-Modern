package com.gregtechceu.gtceu.common.cover.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MatchResult {

    public static final MatchResult NONE = new MatchResult(false, null, -1);
    public static final MatchResult ANY = new MatchResult(true, null, -1);
    private final boolean matched;
    private final Object matchedObject;
    private final int filterIndex;

    private MatchResult(boolean matched, Object matchedObject, int filterIndex) {
        this.matched = matched;
        this.matchedObject = matchedObject;
        this.filterIndex = filterIndex;
    }

    public boolean isMatched() {
        return matched;
    }

    public Object getMatchedObject() {
        return matchedObject;
    }

    public @NotNull ItemStack getItemStack() {
        return matchedObject instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    public @Nullable FluidStack getFluidStack() {
        return matchedObject instanceof FluidStack stack ? stack : null;
    }

    public int getFilterIndex() {
        return filterIndex;
    }

    public static MatchResult create(boolean matched, Object matchedStack, int filterIndex) {
        return new MatchResult(matched, matchedStack, filterIndex);
    }
}
