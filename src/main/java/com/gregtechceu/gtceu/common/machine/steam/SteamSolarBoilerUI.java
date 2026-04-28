package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachineUI;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.world.entity.player.Player;

import java.util.Objects;

final class SteamSolarBoilerUI {

    private SteamSolarBoilerUI() {}

    static ModularUI create(SteamSolarBoiler machine, Player entityPlayer) {
        return SteamBoilerMachineUI.create(machine, entityPlayer)
                .widget(new ProgressWidget(
                        () -> GTUtil.canSeeSunClearly(Objects.requireNonNull(machine.getLevel()),
                                machine.getBlockPos()) ? 1.0 : 0.0,
                        114,
                        44, 20,
                        20)
                        .setProgressTexture(
                                GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(machine.isHighPressure)
                                        .getSubTexture(0, 0, 1, 0.5),
                                GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(machine.isHighPressure)
                                        .getSubTexture(0, 0.5, 1, 0.5)));
    }
}
