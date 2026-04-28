package com.lowdragmc.lowdraglib.gui.widget.custom;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.entity.player.Player;

public class PlayerInventoryWidget extends WidgetGroup {

    private IGuiTexture slotBackground = IGuiTexture.EMPTY;

    public PlayerInventoryWidget() {
        super(0, 0, 176, 82);
    }

    public void initTemplate() {}

    public void setPlayer(Player player) {}

    public void setSlotBackground(IGuiTexture slotBackground) {
        this.slotBackground = slotBackground;
    }

    public IGuiTexture getSlotBackground() {
        return slotBackground;
    }
}
