package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;

import net.minecraft.server.level.ServerPlayer;

public final class GTCoverUI {

    private GTCoverUI() {}

    public static void open(CoverBehavior cover, ServerPlayer player) {
        CoverUIFactory.INSTANCE.openUI(cover, player);
    }
}
