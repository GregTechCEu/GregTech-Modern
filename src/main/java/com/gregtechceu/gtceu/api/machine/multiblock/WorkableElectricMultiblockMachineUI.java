package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.feature.IVoidableUI;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachineUI;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.List;

final class WorkableElectricMultiblockMachineUI {

    private WorkableElectricMultiblockMachineUI() {}

    static Object createUIWidget(WorkableElectricMultiblockMachine machine) {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(IDisplayUIMachineUI.screenTexture(machine.getScreenTexture()))
                .addWidget(new LabelWidget(4, 5, machine.self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, machine::addDisplayText)
                        .textSupplier(machine.getLevel().isClientSide() ? null : machine::addDisplayText)
                        .setMaxWidthLimit(200)
                        .clickHandler(machine::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    static ModularUI createUI(WorkableElectricMultiblockMachine machine, Player entityPlayer) {
        return new ModularUI(198, 208, machine, entityPlayer).widget(new FancyMachineUIWidget(machine, 198, 208));
    }

    static void attachConfigurators(WorkableElectricMultiblockMachine machine, Object configuratorPanelObject) {
        if (!(configuratorPanelObject instanceof ConfiguratorPanel configuratorPanel)) {
            return;
        }
        IVoidableUI.attachConfigurators(configuratorPanel, machine);
        if (machine.getDefinition().getRecipeModifier() instanceof RecipeModifierList list &&
                Arrays.stream(list.getModifiers()).anyMatch(modifier -> modifier == GTRecipeModifiers.BATCH_MODE)) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_BATCH.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_BATCH.getSubTexture(0, 0.5, 1, 0.5),
                    machine::isBatchEnabled,
                    (cd, p) -> machine.setBatchEnabled(p))
                    .setTooltipsSupplier(
                            p -> List.of(
                                    Component.translatable("gtceu.machine.batch_" + (p ? "enabled" : "disabled")))));
        }
    }

    static void attachTooltips(WorkableElectricMultiblockMachine machine, Object tooltipsPanelObject) {
        if (!(tooltipsPanelObject instanceof TooltipsPanel tooltipsPanel)) {
            return;
        }
        for (IMultiPart part : machine.getParts()) {
            part.attachFancyTooltipsToController(machine, tooltipsPanel);
        }
    }
}
