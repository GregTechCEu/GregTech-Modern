package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface IDisplayUIMachine extends IUIMachine {

    default void addDisplayText(List<Component> textList) {
        for (var part : self().getParts()) {
            part.addMultiText(textList);
        }
    }

    default void handleDisplayClick(String componentData, Object clickData) {}

    default Object getScreenTexture() {
        return IDisplayUIMachineUI.defaultScreenTexture();
    }

    @Override
    default ModularUI createUI(Player entityPlayer) {
        return IDisplayUIMachineUI.createUI(this, entityPlayer);
    }

    @Override
    default MultiblockControllerMachine self() {
        return (MultiblockControllerMachine) this;
    }
}
