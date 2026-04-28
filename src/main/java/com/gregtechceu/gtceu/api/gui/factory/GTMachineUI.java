package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.server.level.ServerPlayer;

public final class GTMachineUI {

    private GTMachineUI() {}

    public static void open(MetaMachine machine, ServerPlayer player) {
        MachineUIFactory.INSTANCE.openUI(machine, player);
    }
}
