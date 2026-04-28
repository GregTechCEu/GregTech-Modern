package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import net.minecraft.world.entity.player.Player;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

final class CreativeEnergyContainerMachineUI {

    private CreativeEnergyContainerMachineUI() {}

    static ModularUI create(CreativeEnergyContainerMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(machine.voltage),
                        value -> {
                            machine.voltage = Long.parseLong(value);
                            machine.setTier = GTUtil.getTierByVoltage(machine.voltage);
                        }).setNumbersOnly(0L, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
                .widget(new ButtonWidget(7, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
                        cd -> machine.amps = --machine.amps == -1 ? 0 : machine.amps))
                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(machine.amps),
                        value -> machine.amps = Integer.parseInt(value)).setNumbersOnly(0, Integer.MAX_VALUE))
                .widget(new ButtonWidget(149, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
                        cd -> {
                            if (machine.amps < Integer.MAX_VALUE) {
                                machine.amps++;
                            }
                        }))
                .widget(new LabelWidget(7, 110,
                        () -> "Average Energy I/O per tick: " + machine.lastAverageEnergyIOPerTick))
                .widget(new SwitchWidget(7, 139, 77, 20, (clickData, value) -> machine.active = value)
                        .setTexture(
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                        new TextTexture("gtceu.creative.activity.off")),
                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                        new TextTexture("gtceu.creative.activity.on")))
                        .setPressed(machine.active))
                .widget(new SwitchWidget(85, 139, 77, 20, (clickData, value) -> {
                    machine.source = value;
                    if (machine.source) {
                        machine.voltage = 0;
                        machine.amps = 0;
                        machine.setTier = 0;
                    } else {
                        machine.voltage = GTValues.V[14];
                        machine.amps = Integer.MAX_VALUE;
                        machine.setTier = 14;
                    }
                }).setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.energy.sink")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.energy.source")))
                        .setPressed(machine.source))
                .widget(new SelectorWidget(7, 7, 50, 20, Arrays.stream(GTValues.VNF).toList(), -1)
                        .setOnChanged(tier -> {
                            machine.setTier = ArrayUtils.indexOf(GTValues.VNF, tier);
                            machine.voltage = GTValues.VEX[machine.setTier];
                        })
                        .setSupplier(() -> GTValues.VNF[machine.setTier])
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(ColorPattern.BLACK.rectTexture())
                        .setValue(GTValues.VNF[machine.setTier]));
    }
}
