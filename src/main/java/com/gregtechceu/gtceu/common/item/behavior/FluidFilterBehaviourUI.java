package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.world.entity.player.Player;

final class FluidFilterBehaviourUI {

    private FluidFilterBehaviourUI() {}

    static Object create(GTHeldItemUIHolder holder, Player entityPlayer) {
        var held = holder.getHeld();
        return new ModularUI(176, 157, holder, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, held.getItem().getDescriptionId()))
                .widget((Widget) FluidFilter.loadFilter(held).openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 75, true));
    }
}
