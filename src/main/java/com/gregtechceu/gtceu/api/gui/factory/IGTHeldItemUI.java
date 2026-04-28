package com.gregtechceu.gtceu.api.gui.factory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface IGTHeldItemUI {

    ModularUI createUI(Player player, GTHeldItemUIHolder holder);

    default GTHeldItemUIHolder createUIHolder(Player player, InteractionHand hand) {
        return new GTHeldItemUIHolder(this, player, hand);
    }
}
