package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorGuiRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class GuiModuleBehaviour implements IMonitorModuleItem {

    private final Map<Pair<Level, BlockPos>, MonitorGuiRenderer> renderers = new HashMap<>();

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return renderers.computeIfAbsent(
                Pair.of(group.getTargetLevel(machine.getLevel()), group.getTarget(machine.getLevel())),
                MonitorGuiRenderer::new);
    }

    @Override
    public IPanelHandler createModularPanel(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group,
                                            PanelSyncManager syncManager) {
        return syncManager.syncedPanel("gui_module_" + group.getName(), true,
                (psm, handler) -> new ModularPanel("gui_module_info")
                        .coverChildren()
                        .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.gui_module_info"))
                                .height(50)
                                .width(200)));
    }
}
