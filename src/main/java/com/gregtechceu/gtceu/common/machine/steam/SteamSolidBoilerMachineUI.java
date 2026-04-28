package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachineUI;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.world.entity.player.Player;

final class SteamSolidBoilerMachineUI {

    private SteamSolidBoilerMachineUI() {}

    static ModularUI create(SteamSolidBoilerMachine machine, Player entityPlayer) {
        return SteamBoilerMachineUI.create(machine, entityPlayer)
                .widget(new SlotWidget(machine.fuelHandler.storage, 0, 115, 62)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT_STEAM.get(machine.isHighPressure),
                                GuiTextures.COAL_OVERLAY_STEAM.get(machine.isHighPressure))))
                .widget(new SlotWidget(machine.ashHandler.storage, 0, 115, 26, true, false)
                        .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT_STEAM.get(machine.isHighPressure),
                                GuiTextures.DUST_OVERLAY_STEAM.get(machine.isHighPressure))))
                .widget(new ProgressWidget(machine.recipeLogic::getProgressPercent, 115, 44, 18, 18)
                        .setProgressTexture(
                                GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(machine.isHighPressure)
                                        .getSubTexture(0, 0, 1, 0.5),
                                GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(machine.isHighPressure)
                                        .getSubTexture(0, 0.5, 1, 0.5))
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP));
    }
}
