package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorTextRenderer;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.CodeEditorWidget;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TextModuleBehaviour implements IMonitorModuleItem, IItemUIFactory {

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack) {
        return new MonitorTextRenderer(() -> List.of(stack.getDisplayName()));
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        ModularUI ui = new ModularUI(holder, entityPlayer);
        return ui.widget(new CodeEditorWidget(50, 50, 100, 200));
    }
}
