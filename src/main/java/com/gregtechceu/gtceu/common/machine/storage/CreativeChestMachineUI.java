package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class CreativeChestMachineUI {

    private CreativeChestMachineUI() {}

    static Object createUIWidget(CreativeChestMachine machine) {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.addWidget(new PhantomSlotWidget(machine.cache, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setMaxStackSize(1)
                .setBackgroundTexture(GuiTextures.SLOT));
        group.addWidget(new LabelWidget(7, 9, "gtceu.creative.chest.item"));
        group.addWidget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 50, 152, 10, () -> String.valueOf(machine.itemsPerCycle),
                machine::setItemsPerCycle)
                .setMaxStringLength(11)
                .setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 28, "gtceu.creative.chest.ipc"));
        group.addWidget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 87, 152, 10, () -> String.valueOf(machine.ticksPerCycle),
                machine::setTicksPerCycle)
                .setMaxStringLength(11)
                .setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 65, "gtceu.creative.chest.tpc"));
        group.addWidget(new SwitchWidget(7, 101, 162, 20, (clickData, value) -> machine.setWorkingEnabled(value))
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.off")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.on")))
                .setPressed(machine.isWorkingEnabled()));

        return group;
    }
}
