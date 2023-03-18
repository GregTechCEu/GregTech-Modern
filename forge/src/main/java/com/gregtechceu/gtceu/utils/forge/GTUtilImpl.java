package com.gregtechceu.gtceu.utils.forge;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote GTUtilImpl
 */
public class GTUtilImpl {
    public static int getItemBurnTime(Item item) {
        return ForgeHooks.getBurnTime(item.getDefaultInstance(), RecipeType.SMELTING);
    }
}
