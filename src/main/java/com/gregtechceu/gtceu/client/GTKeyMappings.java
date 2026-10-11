package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;

public final class GTKeyMappings {

    public static final KeyMapping JETPACK_ENABLE = new KeyMapping("gtceu.key.enable_jetpack",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, GTCEu.NAME);
    public static final KeyMapping TOGGLE_HOVER_MODE = new KeyMapping("gtceu.key.armor_hover",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_H, GTCEu.NAME);

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(JETPACK_ENABLE);
        event.register(TOGGLE_HOVER_MODE);
    }

    private GTKeyMappings() {}
}
