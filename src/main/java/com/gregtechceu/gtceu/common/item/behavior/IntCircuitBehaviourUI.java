package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.world.entity.player.Player;

final class IntCircuitBehaviourUI {

    private IntCircuitBehaviourUI() {}

    static Object create(GTHeldItemUIHolder holder, Player entityPlayer) {
        LabelWidget label = new LabelWidget(9, 8, "Programmed Circuit Configuration");
        label.setDropShadow(false);
        label.setTextColor(0x404040);
        var modular = new ModularUI(184, 132, holder, entityPlayer)
                .widget(label);
        SlotWidget slotwidget = new SlotWidget(new CustomItemStackHandler(
                IntCircuitBehaviour.stack(IntCircuitBehaviour.getCircuitConfiguration(holder.getHeld()))), 0, 82, 20,
                false, false);
        slotwidget.setBackground(GuiTextures.SLOT);
        modular.widget(slotwidget);
        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                modular.widget(new ButtonWidget(10 + (18 * y), 48 + (18 * x), 18, 18,
                        new GuiTextureGroup(GuiTextures.SLOT,
                                new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                        data -> {
                            IntCircuitBehaviour.setCircuitConfiguration(holder, finalIdx);
                            slotwidget.setHandlerSlot(
                                    new CustomItemStackHandler(IntCircuitBehaviour.stack(finalIdx)), 0);
                        }));
                idx++;
            }
        }
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            modular.widget(new ButtonWidget(10 + (18 * x), 102, 18, 18,
                    new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                    data -> {
                        IntCircuitBehaviour.setCircuitConfiguration(holder, finalIdx);
                        slotwidget.setHandlerSlot(new CustomItemStackHandler(IntCircuitBehaviour.stack(finalIdx)), 0);
                    }));
        }
        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}
