package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;

import net.minecraft.server.level.ServerPlayer;

public final class GTMachineUI {

    private GTMachineUI() {}

    public static boolean open(MetaMachine machine, ServerPlayer player) {
        return BlockUIMenuType.openUI(player, machine.getBlockPos());
    }
}
