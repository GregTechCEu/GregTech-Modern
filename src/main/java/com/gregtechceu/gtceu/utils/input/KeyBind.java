package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link SyncedKeyMappings} instead
 */
@ApiStatus.ScheduledForRemoval(inVersion = "8.0.0")
@Deprecated(forRemoval = true, since = "7.2.1")
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public enum KeyBind {

    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_JUMP}
     */
    @Deprecated
    VANILLA_JUMP,
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_SNEAK}
     */
    @Deprecated
    VANILLA_SNEAK,
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_FORWARD}
     */
    @Deprecated
    VANILLA_FORWARD,
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_BACKWARD}
     */
    @Deprecated
    VANILLA_BACKWARD,
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_LEFT}
     */
    @Deprecated
    VANILLA_LEFT,
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_RIGHT}
     */
    @Deprecated
    VANILLA_RIGHT,
    /**
     * @deprecated {@link SyncedKeyMappings#ARMOR_MODE_SWITCH}
     */
    @Deprecated
    ARMOR_MODE_SWITCH("gtceu.key.armor_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_M),
    /**
     * @deprecated {@link SyncedKeyMappings#ARMOR_HOVER}
     */
    @Deprecated
    ARMOR_HOVER("gtceu.key.armor_hover", KeyConflictContext.IN_GAME, InputConstants.KEY_H),
    /**
     * @deprecated {@link SyncedKeyMappings#JETPACK_ENABLE}
     */
    @Deprecated
    JETPACK_ENABLE("gtceu.key.enable_jetpack", KeyConflictContext.IN_GAME, InputConstants.KEY_G),
    /**
     * @deprecated {@link SyncedKeyMappings#BOOTS_ENABLE}
     */
    @Deprecated
    BOOTS_ENABLE("gtceu.key.enable_boots", KeyConflictContext.IN_GAME, InputConstants.KEY_PERIOD),
    /**
     * @deprecated {@link SyncedKeyMappings#ARMOR_CHARGING}
     */
    @Deprecated
    ARMOR_CHARGING("gtceu.key.armor_charging", KeyConflictContext.IN_GAME, InputConstants.KEY_N),
    /**
     * @deprecated {@link SyncedKeyMappings#TOOL_AOE_CHANGE}
     */
    @Deprecated
    TOOL_AOE_CHANGE("gtceu.key.tool_aoe_change", KeyConflictContext.IN_GAME, InputConstants.KEY_V),
    /**
     * @deprecated removed
     */
    @Deprecated(forRemoval = true)
    ACTION("gtceu.key.action", KeyConflictContext.GUI, InputConstants.KEY_DELETE),
    ;

    /**
     * @deprecated removed
     */
    @Deprecated(forRemoval = true)
    public static final KeyBind[] VALUES = values();

    private static double mouseDelta = 0.0;

    /**
     * @deprecated removed
     */
    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public static void init() {
        GTCEu.LOGGER.info("Registering KeyBinds");
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(KeyBind.class);
        }
    }

    @ApiStatus.Internal
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        mouseDelta = event.getScrollDelta();
    }

    /**
     * @deprecated {@link InputEvent.MouseScrollingEvent#getScrollDelta()}
     */
    @Deprecated(forRemoval = true)
    @OnlyIn(Dist.CLIENT)
    public static boolean scrollingUp() {
        return mouseDelta > 0;
    }

    /**
     * @deprecated {@link InputEvent.MouseScrollingEvent#getScrollDelta()}
     */
    @Deprecated(forRemoval = true)
    @OnlyIn(Dist.CLIENT)
    public static boolean notScrolling() {
        return mouseDelta == 0;
    }

    /**
     * @deprecated {@link InputEvent.MouseScrollingEvent#getScrollDelta()}
     */
    @Deprecated(forRemoval = true)
    @OnlyIn(Dist.CLIENT)
    public static boolean scrollingDown() {
        return mouseDelta < 0;
    }

    @OnlyIn(Dist.CLIENT)
    private KeyMapping keybinding;

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind() {}

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (GTCEu.isClientSide()) {
            this.keybinding = new KeyMapping(langKey, ctx, InputConstants.Type.KEYSYM, button, GTCEu.NAME);
        }
    }

    /**
     * @deprecated removed
     */
    @Deprecated(forRemoval = true)
    @OnlyIn(Dist.CLIENT)
    public KeyMapping toMinecraft() {
        return switch (this) {
            case VANILLA_JUMP -> Minecraft.getInstance().options.keyJump;
            case VANILLA_SNEAK -> Minecraft.getInstance().options.keyShift;
            case VANILLA_FORWARD -> Minecraft.getInstance().options.keyUp;
            case VANILLA_BACKWARD -> Minecraft.getInstance().options.keyDown;
            case VANILLA_LEFT -> Minecraft.getInstance().options.keyLeft;
            case VANILLA_RIGHT -> Minecraft.getInstance().options.keyRight;
            default -> this.keybinding;
        };
    }

    /**
     * @deprecated removed
     */
    @Deprecated(forRemoval = true)
    @OnlyIn(Dist.CLIENT)
    public boolean isPressed() {
        return isKeyDown();
    }

    /**
     * @deprecated {@link SyncedKeyMapping#isKeyDown()}
     */
    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public boolean isKeyDown() {
        SyncedKeyMapping keyMapping = syncedKeyMapping();
        return keyMapping != null && keyMapping.isKeyDown();
    }

    /**
     * @deprecated removed
     */
    @Deprecated(forRemoval = true)
    public boolean isPressed(Player player) {
        return isKeyDown(player);
    }

    /**
     * @deprecated {@link SyncedKeyMapping#isKeyDown(Player)}
     */
    @Deprecated
    public boolean isKeyDown(Player player) {
        SyncedKeyMapping keyMapping = syncedKeyMapping();
        return keyMapping != null && keyMapping.isKeyDown(player);
    }

    private @Nullable SyncedKeyMapping syncedKeyMapping() {
        return switch (this) {
            case VANILLA_JUMP -> SyncedKeyMappings.VANILLA_JUMP;
            case VANILLA_SNEAK -> SyncedKeyMappings.VANILLA_SNEAK;
            case VANILLA_FORWARD -> SyncedKeyMappings.VANILLA_FORWARD;
            case VANILLA_BACKWARD -> SyncedKeyMappings.VANILLA_BACKWARD;
            case VANILLA_LEFT -> SyncedKeyMappings.VANILLA_LEFT;
            case VANILLA_RIGHT -> SyncedKeyMappings.VANILLA_RIGHT;
            case ARMOR_MODE_SWITCH -> SyncedKeyMappings.ARMOR_MODE_SWITCH;
            case ARMOR_HOVER -> SyncedKeyMappings.ARMOR_HOVER;
            case JETPACK_ENABLE -> SyncedKeyMappings.JETPACK_ENABLE;
            case BOOTS_ENABLE -> SyncedKeyMappings.BOOTS_ENABLE;
            case ARMOR_CHARGING -> SyncedKeyMappings.ARMOR_CHARGING;
            case TOOL_AOE_CHANGE -> SyncedKeyMappings.TOOL_AOE_CHANGE;
            case ACTION -> null;
        };
    }
}
