package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IItemLifeCycle extends IItemComponent {

    void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected);
}
