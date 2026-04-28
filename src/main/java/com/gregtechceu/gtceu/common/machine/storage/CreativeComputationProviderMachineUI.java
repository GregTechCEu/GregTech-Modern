package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import net.minecraft.world.entity.player.Player;

final class CreativeComputationProviderMachineUI {

    private CreativeComputationProviderMachineUI() {}

    static ModularUI create(CreativeComputationProviderMachine machine, Player entityPlayer) {
        return new ModularUI(140, 95, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 7, "CWUt"))
                .widget(new TextFieldWidget(9, 20, 122, 16, () -> String.valueOf(machine.maxCWUt),
                        value -> machine.maxCWUt = Integer.parseInt(value)).setNumbersOnly(0, Integer.MAX_VALUE))
                .widget(new LabelWidget(7, 42, "gtceu.creative.computation.average"))
                .widget(new LabelWidget(7, 54, () -> String.valueOf(machine.lastRequestedCWUt)))
                .widget(new SwitchWidget(9, 66, 122, 20, (clickData, value) -> machine.setActive(value))
                        .setSupplier(machine::isActive)
                        .setTexture(new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.off")),
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                        new TextTexture("gtceu.creative.activity.on"))));
    }
}
