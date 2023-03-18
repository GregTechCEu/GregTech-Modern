package com.gregtechceu.gtceu.utils.fabric;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.Item;

/**
 * @author KilaBash
 * @date 2023/3/17
 * @implNote GTUtilImpl
 */
public class GTUtilImpl {
    public static int getItemBurnTime(Item item) {
        var burnTime = FuelRegistry.INSTANCE.get(item);
        return burnTime == null ? -1 : burnTime;
    }
}
