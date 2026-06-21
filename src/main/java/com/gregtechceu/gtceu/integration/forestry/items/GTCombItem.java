package com.gregtechceu.gtceu.integration.forestry.items;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.world.item.ItemStack;



//todo Make this class implement getDescriptionId(), getDescriptionId(ItemStack), getDescription()
// and getName() in the same way TagPrefixItem does to avoid duplicating the language keys for combs.
// Basically, make it a special kind of tag prefix item.
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
