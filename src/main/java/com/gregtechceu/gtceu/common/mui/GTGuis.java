package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gregtechceu.gtceu.common.mui.widgets.PopupPanel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.screen.ModularPanel;

public class GTGuis {

    public static final int DEFAULT_WIDTH = 176, DEFAULT_HEIGHT = 166;

    public static ModularPanel<?> createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    public static ModularPanel<?> createPanel(MetaMachine machine, int width, int height) {
        return createPanel(machine.getDefinition().getId().getPath(), width, height);
    }

    public static ModularPanel<?> createPanel(CoverBehavior cover, int width, int height) {
        return createPanel(cover.coverDefinition.getId().getPath(), width, height);
    }

    public static ModularPanel<?> createPanel(ItemStack stack, int width, int height) {
        return createPanel(stack.getDescriptionId(), width, height);
    }

    public static ModularPanel<?> createPanel(String name) {
        return ModularPanel.defaultPanel(name, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static ModularPanel<?> defaultPanel(MetaMachine machine) {
        return createPanel(machine.getDefinition().getId().getPath());
    }

    public static ModularPanel<?> defaultPanel(CoverBehavior cover) {
        return createPanel(cover.coverDefinition.getId().getPath());
    }

    public static ModularPanel<?> defaultPanel(ItemStack stack) {
        return createPanel(stack, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static ModularPanel<?> defaultPanel(Item item) {
        return createPanel(item.getDescriptionId());
    }

    public static PopupPanel createPopupPanel(String name, int width, int height) {
        return defaultPopupPanel(name)
                .size(width, height);
    }

    public static PopupPanel defaultPopupPanel(String name) {
        return PopupPanel.defaultPopupPanel(name)
                .size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

}
