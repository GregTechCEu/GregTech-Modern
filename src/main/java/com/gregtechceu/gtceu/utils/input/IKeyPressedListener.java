package com.gregtechceu.gtceu.utils.input;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface IKeyPressedListener {

    /**
     * Called <strong>server-side only</strong> when a player presses a specified keybinding.
     *
     * @param player     The player who pressed the key.
     * @param keyPressed The key the player pressed.
     */
    void onKeyPressed(ServerPlayer player, SyncedKeyMapping keyPressed, boolean isDown);
}
