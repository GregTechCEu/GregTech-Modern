package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.world.entity.player.Player;

public interface IUICover extends IUIHolder {

    default CoverBehavior self() {
        return (CoverBehavior) this;
    }

    @Override
    default boolean isInvalid() {
        return self().coverHolder.isInValid() || self().coverHolder.getCoverAtSide(self().attachedSide) != self();
    }

    @Override
    default boolean isRemote() {
        return self().coverHolder.isRemote();
    }

    @Override
    default void markAsDirty() {
        self().coverHolder.markDirty();
    }

    @Override
    default ModularUI createUI(Player entityPlayer) {
        var widget = createUIWidget();
        var size = widget.getSize();
        widget.setSelfPosition(new Position((176 - size.width) / 2, 0));
        var modularUI = new ModularUI(176, size.height + 82, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(widget)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, size.height,
                        true));
        modularUI.registerCloseListener(this::onUIClosed);
        return modularUI;
    }

    default void onUIClosed() {}

    Widget createUIWidget();
}
