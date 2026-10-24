package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.modular_item.ModularArmorUIFactory;

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
    public static final SyncedKeyMapping NIGHT_VISION_ENABLE = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.enable_night_vision", KeyConflictContext.IN_GAME, InputConstants.KEY_N);
    public static final SyncedKeyMapping ARMOR_HOVER = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.armor_hover", KeyConflictContext.IN_GAME, InputConstants.KEY_H);
    public static final SyncedKeyMapping JETPACK_ENABLE = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.enable_jetpack", KeyConflictContext.IN_GAME, InputConstants.KEY_G);
    public static final SyncedKeyMapping TOOL_AOE_CHANGE = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.tool_aoe_change", KeyConflictContext.IN_GAME, InputConstants.KEY_V);
    public static final SyncedKeyMapping MODULAR_ITEM_GUI = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.modular_item_gui", KeyConflictContext.IN_GAME,
            InputConstants.KEY_V).registerGlobalListener((player, key, isDown) -> {
                if (isDown && player.containerMenu == player.inventoryMenu) {
                    ModularArmorUIFactory.INSTANCE.open(player);
                }
            });
    public static final SyncedKeyMapping STEP_ASSIST_ENABLE = SyncedKeyMapping.createConfigurable(
            "keybind.gtceu.enable_step_assist", KeyConflictContext.IN_GAME,
            InputConstants.KEY_APOSTROPHE);

    public static void init() {
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
        ModLoader.get().postEvent(new SyncedKeyMappingEvent());
    }

    private SyncedKeyMappings() {}
}
