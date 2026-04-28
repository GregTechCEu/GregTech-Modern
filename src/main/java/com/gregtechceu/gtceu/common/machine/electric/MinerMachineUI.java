package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachineUI;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

final class MinerMachineUI {

    private MinerMachineUI() {}

    static Object createEditableUI(Identifier path, int inventorySize) {
        return new EditableMachineUI("misc", path, () -> {
            WidgetGroup template = createTemplate(inventorySize).createDefault();
            SlotWidget batterySlot = createBatterySlot().createDefault();
            batterySlot.setSelfPosition(new Position(100, 10));
            WidgetGroup group = new WidgetGroup(0, 0, Math.max(template.getSize().width + 12, 172),
                    template.getSize().height + 8);
            Size size = group.getSize();

            template.setSelfPosition(new Position(
                    (size.width - 4 - template.getSize().width) / 2 + 4,
                    (size.height - template.getSize().height) / 2));

            group.addWidget(template);
            group.addWidget(batterySlot);
            return group;
        }, (template, machine) -> {
            if (machine instanceof MinerMachine minerMachine) {
                createTemplate(inventorySize).setupUI(template, minerMachine);
                TieredEnergyMachineUI.createEnergyBar().setupUI(template, minerMachine);
                createBatterySlot().setupUI(template, minerMachine);
            }
        });
    }

    private static EditableUI<WidgetGroup, MinerMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("miner", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            int width = rowSize * 18 + 120;
            int height = Math.max(rowSize * 18, 80);
            WidgetGroup group = new WidgetGroup(0, 0, width, height);

            WidgetGroup slots = new WidgetGroup(120, (height - rowSize * 18) / 2, rowSize * 18, rowSize * 18);
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    var slot = new SlotWidget();
                    slot.initTemplate();
                    slot.setSelfPosition(new Position(x * 18, y * 18));
                    slot.setBackground(GuiTextures.SLOT);
                    slot.setId("slot_" + index);
                    slots.addWidget(slot);
                }
            }

            var componentPanel = new ComponentPanelWidget(4, 5, list -> {});
            componentPanel.setMaxWidthLimit(110);
            componentPanel.setId("component_panel");

            var container = new WidgetGroup(0, 0, 117, height);
            container.addWidget(new DraggableScrollableWidgetGroup(4, 4, container.getSize().width - 8,
                    container.getSize().height - 8)
                    .setBackground(GuiTextures.DISPLAY)
                    .addWidget(componentPanel));
            container.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(container);
            group.addWidget(slots);
            return group;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < machine.exportItems.getSlots()) {
                    slot.setHandlerSlot(machine.exportItems, index);
                    slot.setCanTakeItems(true);
                    slot.setCanPutItems(false);
                }
            });
            WidgetUtils.widgetByIdForEach(group, "^component_panel$", ComponentPanelWidget.class,
                    panel -> panel.textSupplier(machine::addDisplayText));
        });
    }

    /**
     * Create an energy bar widget.
     */
    private static EditableUI<SlotWidget, MinerMachine> createBatterySlot() {
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
}
