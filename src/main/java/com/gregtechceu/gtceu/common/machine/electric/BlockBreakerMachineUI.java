package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachineUI;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

final class BlockBreakerMachineUI {

    private BlockBreakerMachineUI() {}

    static Object createEditableUI(Identifier path, int inventorySize) {
        return new EditableMachineUI("misc", path, () -> {
            var template = createTemplate(inventorySize).createDefault();
            var energyBar = TieredEnergyMachineUI.createEnergyBar().createDefault();
            var batterySlot = createBatterySlot().createDefault();
            var energyGroup = new WidgetGroup(0, 0, energyBar.getSize().width, energyBar.getSize().height + 20);
            batterySlot.setSelfPosition(
                    new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
            energyGroup.addWidget(energyBar);
            energyGroup.addWidget(batterySlot);
            var group = new WidgetGroup(0, 0,
                    Math.max(energyGroup.getSize().width + template.getSize().width + 4 + 8, 172),
                    Math.max(template.getSize().height + 8, energyGroup.getSize().height + 8));
            var size = group.getSize();
            energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));

            template.setSelfPosition(new Position(
                    (size.width - 4 - template.getSize().width) / 2 + 4,
                    (size.height - template.getSize().height) / 2));

            group.addWidget(energyGroup);
            group.addWidget(template);
            return group;
        }, (template, machine) -> {
            if (machine instanceof BlockBreakerMachine blockBreakerMachine) {
                createTemplate(inventorySize).setupUI(template, blockBreakerMachine);
                TieredEnergyMachineUI.createEnergyBar().setupUI(template, blockBreakerMachine);
                createBatterySlot().setupUI(template, blockBreakerMachine);
            }
        });
    }

    private static EditableUI<SlotWidget, BlockBreakerMachine> createBatterySlot() {
        return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
            var slotWidget = new SlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setHandlerSlot(machine.chargerInventory, 0);
            slotWidget.setCanPutItems(true);
            slotWidget.setCanTakeItems(true);
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
                    GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
        });
    }

    private static EditableUI<WidgetGroup, BlockBreakerMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            WidgetGroup main = new WidgetGroup(0, 0, rowSize * 18 + 8, rowSize * 18 + 8);
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    SlotWidget slotWidget = new SlotWidget();
                    slotWidget.initTemplate();
                    slotWidget.setSelfPosition(new Position(4 + x * 18, 4 + y * 18));
                    slotWidget.setBackground(GuiTextures.SLOT);
                    slotWidget.setId("slot_" + index);
                    main.addWidget(slotWidget);
                }
            }
            main.setBackground(GuiTextures.BACKGROUND_INVERSE);
            return main;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < machine.cache.getSlots()) {
                    slot.setHandlerSlot(machine.cache, index);
                    slot.setCanTakeItems(true);
                    slot.setCanPutItems(false);
                }
            });
        });
    }
}
