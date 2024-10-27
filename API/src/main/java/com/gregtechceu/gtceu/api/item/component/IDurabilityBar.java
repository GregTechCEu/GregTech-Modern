package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IDurabilityBar
 */
public interface IDurabilityBar extends IItemComponent {

    default int getBarWidth(ItemStack stack) {
        return Math.round(getDurabilityForDisplay(stack) * 13);
    }

    default float getDurabilityForDisplay(ItemStack stack) {
        return (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
    }

    default int getMaxDurability(ItemStack stack) {
        return stack.getDamageValue();
    }

    default boolean isBarVisible(ItemStack stack) {
        return true;
    }

    default int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, getDurabilityForDisplay(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    /** The first and last colors of a gradient. Default to Green durability gradient (null Pair). */
    @Nullable
    default Pair<Integer, Integer> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return null;
    }

    /** Whether to show the durability as red when at the last 1/4th durability. Default true */
    default boolean doDamagedStateColors(ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IDurabilityBar#getDurabilityForDisplay(ItemStack)} is 0.
     * Default true
     */
    default boolean showEmptyBar(ItemStack itemStack) {
        return true;
    }

    /**
     * Whether to show the durability bar when {@link IDurabilityBar#getDurabilityForDisplay(ItemStack)} is 1.
     * Default true
     */
    default boolean showFullBar(ItemStack itemStack) {
        return true;
    }
}
