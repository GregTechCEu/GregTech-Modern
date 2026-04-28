package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class IDisplayUIMachineUI {

    private IDisplayUIMachineUI() {}

    static ModularUI createUI(IDisplayUIMachine machine, Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121)
                .setBackground(screenTexture(machine.getScreenTexture()));
        screen.addWidget(new LabelWidget(4, 5, machine.self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, machine::addDisplayText)
                .textSupplier(machine.self().getLevel().isClientSide() ? null : machine::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(machine::handleDisplayClick)).setSizeHeight(8);
        return new ModularUI(176, 216, machine, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 134, true));
    }

    public static IGuiTexture defaultScreenTexture() {
        return GuiTextures.DISPLAY;
    }

    public static IGuiTexture screenTexture(Object screenTexture) {
        if (screenTexture instanceof IGuiTexture guiTexture) {
            return guiTexture;
        }
        return GuiTextures.DISPLAY;
    }

    public static IGuiTexture steamScreenTexture(boolean highPressure) {
        return GuiTextures.DISPLAY_STEAM.get(highPressure);
    }

    public static Component withButton(Component component, String data) {
        return ComponentPanelWidget.withButton(component, data);
    }

    public static boolean isRemoteClick(Object clickData) {
        return clickData instanceof ClickData data && data.isRemote;
    }
}
