package com.gregtechceu.gtceu.utils.input;

import net.minecraft.server.level.ServerPlayer;

public interface IKeyPressedListener {
    void onKeyPressed(ServerPlayer player, KeyBind keyPressed);
}
