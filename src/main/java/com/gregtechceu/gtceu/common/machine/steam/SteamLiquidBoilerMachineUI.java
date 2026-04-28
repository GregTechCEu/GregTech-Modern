package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachineUI;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

import net.minecraft.world.entity.player.Player;

final class SteamLiquidBoilerMachineUI {

    private SteamLiquidBoilerMachineUI() {}

    static ModularUI create(SteamLiquidBoilerMachine machine, Player entityPlayer) {
        return SteamBoilerMachineUI.create(machine, entityPlayer)
                .widget(new TankWidget(machine.fuelTank.getStorages()[0], 119, 26, 10, 54, true, true)
                        .setShowAmount(false)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setBackground(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(machine.isHighPressure)));
    }
}
