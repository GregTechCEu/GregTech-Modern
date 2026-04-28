package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.network.chat.Component;

final class ItemBusPartMachineUI {

    private ItemBusPartMachineUI() {}

    static void attachConfigurators(ItemBusPartMachine machine, Object configuratorPanelObject) {
        if (machine.getIo().support(IO.OUT)) {
            ((IDistinctPart) machine).superAttachConfigurators(configuratorPanelObject);
        } else if (machine.getIo().support(IO.IN)) {
            ((IDistinctPart) machine).attachConfigurators(configuratorPanelObject);
            if (machine.hasCircuitSlot && machine.isCircuitSlotEnabled() &&
                    configuratorPanelObject instanceof ConfiguratorPanel configuratorPanel) {
                configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(machine.circuitInventory.storage));
            }
        }
    }

    static Object createUIWidget(ItemBusPartMachine machine) {
        int rowSize = (int) Math.sqrt(machine.getInventorySize());
        int colSize = rowSize;
        if (machine.getInventorySize() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        if (machine.getIo() == IO.OUT) {
            group.addWidget(((Widget) machine.filterHandler
                    .createFilterSlotUI(71 + (18 * rowSize) / 2, 35 + 9 * rowSize))
                    .setHoverTooltips(Component.translatable("cover.item_filter.title")));
        }
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new SlotWidget(machine.getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true,
                                machine.getIo().support(IO.IN))
                                .setBackgroundTexture(GuiTextures.SLOT)
                                .setIngredientIO(machine.getIo().support(IO.IN) ? IngredientIO.INPUT :
                                        IngredientIO.OUTPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
