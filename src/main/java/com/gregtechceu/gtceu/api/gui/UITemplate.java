package com.gregtechceu.gtceu.api.gui;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.entity.player.Inventory;

public class UITemplate {

    public static WidgetGroup bindPlayerInventory(Inventory inventoryPlayer, IGuiTexture imageLocation, int x, int y,
                                                  boolean addHotbar) {
        WidgetGroup group = new WidgetGroup(x, y, 162, 54 + (addHotbar ? 22 : 0));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                group.addWidget(new SlotWidget(inventoryPlayer, col + (row + 1) * 9, col * 18, row * 18)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, false));
            }
        }
        if (addHotbar) {
            for (int slot = 0; slot < 9; slot++) {
                group.addWidget(new SlotWidget(inventoryPlayer, slot, slot * 18, 58)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, true));
            }
        }
        return group;
    }
}
