package com.gregtechceu.gtceu.api.mui.factory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityGuiData extends GuiData {

    private final Entity guiHolder;

    public EntityGuiData(Player player, Entity guiHolder) {
        super(player);
        this.guiHolder = guiHolder;
    }

    public Entity getGuiHolder() {
        return guiHolder;
    }
}
