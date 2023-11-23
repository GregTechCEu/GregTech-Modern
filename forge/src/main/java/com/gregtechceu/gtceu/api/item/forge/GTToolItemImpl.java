package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote GTToolItemImpl
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTToolItemImpl extends GTToolItem {

    protected GTToolItemImpl(GTToolType toolType, MaterialToolTier tier, int electricTier, Properties properties) {
        super(toolType, tier, electricTier, properties);
    }

    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, int electricTier, Item.Properties properties) {
        return new GTToolItemImpl(toolType, tier, electricTier, properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (itemStack.getMaxDamage() >= itemStack.getDamageValue()) {
            itemStack = itemStack.copy();
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }
}
