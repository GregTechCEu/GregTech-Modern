package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.ModularItemManagerUI;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.AbstractUIFactory;
import brachy.modularui.factory.GuiData;

public class ModularItemUIFactory extends AbstractUIFactory<GuiData> {

    public static final ModularItemUIFactory INSTANCE = new ModularItemUIFactory();

    protected ModularItemUIFactory() {
        super(GTCEu.id("modular_item"));
    }

    @Override
    public IUIHolder<GuiData> getGuiHolder(GuiData data) {
        return new ModularItemManagerUI(data.getPlayer());
    }

    @Override
    public void writeGuiData(GuiData data, FriendlyByteBuf buf) {}

    @Override
    public GuiData readGuiData(Player player, FriendlyByteBuf buf) {
        return new GuiData(player);
    }
}
