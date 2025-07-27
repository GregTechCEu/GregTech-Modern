package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

import com.mojang.blaze3d.platform.InputConstants;

public class SyncedKeyMappings {

    public static final SyncedKeyMapping ARMOR_MODE_SWITCH = SyncedKeyMapping
            .createConfigurable("gtceu.key.armor_mode_switch_synctest", KeyConflictContext.IN_GAME,
                    InputConstants.KEY_M);

    public static void init() {
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
        ModLoader.get().postEvent(new SyncedKeyMappingEvent());
    }
}
