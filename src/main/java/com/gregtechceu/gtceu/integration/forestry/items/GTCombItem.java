package com.gregtechceu.gtceu.integration.forestry.items;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.world.item.ItemStack;

public class GTCombItem extends ItemForestry implements IColoredItem {
    private final GTCombType type;


    public GTCombItem(GTCombType type) {
        this.type = type;
    }

    @Override
    public int getColorFromItemStack(ItemStack itemstack, int tintIndex) {
        GTCombType honeyComb = this.type;

        if (tintIndex == 1) {
            return honeyComb.primaryColor;
        } else {
            return honeyComb.secondaryColor;
        }
    }
}
