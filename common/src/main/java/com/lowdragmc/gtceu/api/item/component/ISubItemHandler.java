package com.lowdragmc.gtceu.api.item.component;

import com.lowdragmc.gtceu.api.item.ComponentItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ISubItemHandler
 */
public interface ISubItemHandler extends IItemComponent {
    default void fillItemCategory(ComponentItem item, CreativeModeTab category, NonNullList<ItemStack> items) {
        if (item.allowedIn(category)) {
            items.add(new ItemStack(item));
        }
    }
}
