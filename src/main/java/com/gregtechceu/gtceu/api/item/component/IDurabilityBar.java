package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public interface IDurabilityBar extends IItemDecoratorComponent {

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
    default IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
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

    @Override
    default boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        return ToolChargeBarRenderer.renderDurabilityBar(guiGraphics, stack, this, xOffset, yOffset);
    }
}
