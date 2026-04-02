package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.value.sync.PanelSyncManager;

public interface IMonitorModuleItem extends IItemComponent {

    default void tick(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {}

    default void tickInPlaceholder(ItemStack stack, PlaceholderContext context) {}

    IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group);

    IPanelHandler createModularPanel(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group,
                                     PanelSyncManager syncManager);

    default String getType() {
        return "unknown";
    }
}
