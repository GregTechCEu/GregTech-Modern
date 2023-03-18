package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote IDurabilityBar
 */
public interface IDurabilityBar extends IItemComponent{

    default int getBarWidth(ItemStack stack) {
        return Math.round(getDurabilityForDisplay(stack) * 13);
    }

    default float getDurabilityForDisplay(ItemStack stack) {
        return (stack.getMaxDamage() - (float)stack.getDamageValue()) / (float)stack.getMaxDamage();
    }

    default boolean isBarVisible(ItemStack stack) {
        return true;
    }

    default int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, getDurabilityForDisplay(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

}
