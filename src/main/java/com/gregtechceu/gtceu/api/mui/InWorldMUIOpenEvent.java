package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InWorldMUIOpenEvent extends Event {

    @Getter
    private final GuiData guiData;

    @Getter
    private final Screen vanillaScreen;

    @Getter
    private final ModularScreen screen;

    @Getter
    private final ModularContainerMenu menu;
}
