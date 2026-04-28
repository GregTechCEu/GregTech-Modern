package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.gui.widget.ProspectingMapWidget;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;

import net.minecraft.world.entity.player.Player;

final class ProspectorScannerBehaviorUI {

    private ProspectorScannerBehaviorUI() {}

    static Object create(GTHeldItemUIHolder holder, Player entityPlayer, int radius, ProspectorMode<?> mode) {
        var map = new ProspectingMapWidget(4, 4, 332 - 8, 200 - 8, radius, mode, 1);
        return new ModularUI(332, 200, holder, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(map)
                .widget(new SwitchWidget(-20, 4, 18, 18, (cd, pressed) -> map.setDarkMode(pressed))
                        .setSupplier(map::isDarkMode)
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy()
                                                .getSubTexture(0, 0.5, 1, 0.5).scale(0.8f)),
                                new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true)
                                        .copy().getSubTexture(0, 0, 1, 0.5).scale(0.8f))));
    }
}
