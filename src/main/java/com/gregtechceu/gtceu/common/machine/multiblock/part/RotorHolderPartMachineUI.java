package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

final class RotorHolderPartMachineUI {

    private RotorHolderPartMachineUI() {}

    static Object createUIWidget(RotorHolderPartMachine machine) {
        var group = new WidgetGroup(0, 0, 18 + 16, 18 + 16);
        var container = new WidgetGroup(4, 4, 18 + 8, 18 + 8);
        container.addWidget(new BlockableSlotWidget(machine.inventory.storage, 0, 4, 4)
                .setIsBlocked(() -> machine.rotorSpeed != 0)
                .setBackground(GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

    static void attachTooltips(RotorHolderPartMachine machine, Object tooltipsPanelObject) {
        if (!(tooltipsPanelObject instanceof TooltipsPanel tooltipsPanel)) {
            return;
        }
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.universal.rotor_obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                () -> !machine.isFrontFaceFree(),
                () -> null));
    }
}
