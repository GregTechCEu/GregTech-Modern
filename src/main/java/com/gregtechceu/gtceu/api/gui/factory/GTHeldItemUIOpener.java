package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class GTHeldItemUIOpener {

    private GTHeldItemUIOpener() {}

    public static boolean openUI(ServerPlayer player, InteractionHand hand) {
        if (GTCEu.isDataGen()) {
            return false;
        }
        return GTHeldItemUIFactory.INSTANCE.openUI(player, hand);
    }
}
