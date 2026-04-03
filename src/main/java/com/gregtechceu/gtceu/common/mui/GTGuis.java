package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import brachy.modularui.screen.ModularPanel;

public class GTGuis {

    public static ModularPanel<?> createPanel(MetaMachine machine, int width, int height) {
        return ModularPanel.defaultPanel(machine.getDefinition().getId().getPath(), width, height);
    }

}
