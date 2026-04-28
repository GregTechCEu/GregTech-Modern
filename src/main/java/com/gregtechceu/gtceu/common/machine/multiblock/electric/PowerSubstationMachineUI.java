package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.entity.player.Player;

final class PowerSubstationMachineUI {

    private PowerSubstationMachineUI() {}

    static Object createUIWidget(PowerSubstationMachine machine) {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(IDisplayUIMachineUI.screenTexture(machine.getScreenTexture()))
                .addWidget(new LabelWidget(4, 5, machine.self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, machine::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(machine::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    static ModularUI createUI(PowerSubstationMachine machine, Player entityPlayer) {
        return new ModularUI(198, 208, machine, entityPlayer).widget(new FancyMachineUIWidget(machine, 198, 208));
    }

    static void attachTooltips(PowerSubstationMachine machine, Object tooltipsPanelObject) {
        if (!(tooltipsPanelObject instanceof TooltipsPanel tooltipsPanel)) {
            return;
        }
        for (IMultiPart part : machine.getParts()) {
            part.attachFancyTooltipsToController(machine, tooltipsPanel);
        }
    }
}
