package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

import com.mojang.blaze3d.platform.InputConstants;

public final class SyncedKeyMappings {

    // MC keymappings
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

    // GT keymappings
    public static final SyncedKeyMapping ARMOR_MODE_SWITCH = SyncedKeyMapping.createConfigurable(
            "gtceu.key.armor_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_M);
    public static final SyncedKeyMapping ARMOR_HOVER = SyncedKeyMapping.createConfigurable(
            "gtceu.key.armor_hover", KeyConflictContext.IN_GAME, InputConstants.KEY_H);
    public static final SyncedKeyMapping JETPACK_ENABLE = SyncedKeyMapping.createConfigurable(
            "gtceu.key.enable_jetpack", KeyConflictContext.IN_GAME, InputConstants.KEY_G);
    public static final SyncedKeyMapping BOOTS_ENABLE = SyncedKeyMapping.createConfigurable(
            "gtceu.key.enable_boots", KeyConflictContext.IN_GAME, InputConstants.KEY_PERIOD);
    public static final SyncedKeyMapping ARMOR_CHARGING = SyncedKeyMapping.createConfigurable(
            "gtceu.key.armor_charging", KeyConflictContext.IN_GAME, InputConstants.KEY_N);
    public static final SyncedKeyMapping TOOL_AOE_CHANGE = SyncedKeyMapping.createConfigurable(
            "gtceu.key.tool_aoe_change", KeyConflictContext.IN_GAME, InputConstants.KEY_V);

    public static void init() {
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
        ModLoader.get().postEvent(new SyncedKeyMappingEvent());
    }

    private SyncedKeyMappings() {}
}
