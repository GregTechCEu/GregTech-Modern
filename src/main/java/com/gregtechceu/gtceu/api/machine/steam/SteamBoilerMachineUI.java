package com.gregtechceu.gtceu.api.machine.steam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

public final class SteamBoilerMachineUI {

    private SteamBoilerMachineUI() {}

    public static ModularUI create(SteamBoilerMachine machine, Player entityPlayer) {
        return new ModularUI(176, 166, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(machine.isHighPressure))
                .widget(new LabelWidget(6, 6, machine.getBlockState().getBlock().getDescriptionId()))
                .widget(new ProgressWidget(machine::getTemperaturePercent, 96, 26, 10, 54)
                        .setProgressTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(machine.isHighPressure),
                                GuiTextures.PROGRESS_BAR_BOILER_HEAT)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setDynamicHoverTips(pct -> I18n.get("gtceu.multiblock.large_boiler.temperature",
                                machine.getCurrentTemperature() + 274, machine.getMaxTemperature() + 274)))
                .widget(new TankWidget(machine.waterTank.getStorages()[0], 83, 26, 10, 54, false, true)
                        .setShowAmount(false)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setBackground(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(machine.isHighPressure)))
                .widget(new TankWidget(machine.steamTank.getStorages()[0], 70, 26, 10, 54, true, false)
                        .setShowAmount(false)
                        .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
                        .setBackground(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(machine.isHighPressure)))
                .widget(new ImageWidget(43, 44, 18, 18, GuiTextures.CANISTER_OVERLAY_STEAM.get(machine.isHighPressure)))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(machine.isHighPressure), 7, 84, true));
    }
}
