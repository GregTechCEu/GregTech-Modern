package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

public class SyncedKeyMappings {

    public static final SyncedKeyMapping VANILLA_JUMP = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyJump);
    public static final SyncedKeyMapping VANILLA_SNEAK = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyShift);
    public static final SyncedKeyMapping VANILLA_FORWARD = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyUp);
    public static final SyncedKeyMapping VANILLA_BACKWARD = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyDown);
    public static final SyncedKeyMapping VANILLA_LEFT = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyLeft);
    public static final SyncedKeyMapping VANILLA_RIGHT = SyncedKeyMapping
            .createFromMC(() -> () -> Minecraft.getInstance().options.keyRight);

    public static void init() {
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
        ModLoader.get().postEvent(new SyncedKeyMappingEvent());
    }
}
