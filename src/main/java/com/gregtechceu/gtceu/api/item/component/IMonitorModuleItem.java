package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;

import net.minecraft.world.item.ItemStack;

public interface IMonitorModuleItem extends IItemComponent {

    IMonitorRenderer getRenderer(ItemStack stack);
}
