package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeysPressed;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanMutablePair;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Supplier;

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
    VANILLA_JUMP(() -> () -> Minecraft.getInstance().options.keyJump),
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_SNEAK}
     */
    @Deprecated
    VANILLA_SNEAK(() -> () -> Minecraft.getInstance().options.keyShift),
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_FORWARD}
     */
    @Deprecated
    VANILLA_FORWARD(() -> () -> Minecraft.getInstance().options.keyUp),
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_BACKWARD}
     */
    @Deprecated
    VANILLA_BACKWARD(() -> () -> Minecraft.getInstance().options.keyDown),
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_LEFT}
     */
    @Deprecated
    VANILLA_LEFT(() -> () -> Minecraft.getInstance().options.keyLeft),
    /**
     * @deprecated {@link SyncedKeyMappings#VANILLA_RIGHT}
     */
    @Deprecated
    VANILLA_RIGHT(() -> () -> Minecraft.getInstance().options.keyRight),
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
     * @deprecated unused
     */
    @Deprecated(forRemoval = true)
    ACTION("gtceu.key.action", KeyConflictContext.GUI, InputConstants.KEY_DELETE),
    ;

    public static final KeyBind[] VALUES = values();

    private static double mouseDelta = 0.0;

    public static void init() {
        GTCEu.LOGGER.info("Registering KeyBinds");
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(KeyBind.class);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onInputEvent(InputEvent.Key event) {
        List<KeyBind> updating = new ArrayList<>();
        for (KeyBind keybind : VALUES) {
            boolean previousPressed = keybind.isPressed;
            boolean previousKeyDown = keybind.isKeyDown;
            keybind.isPressed = keybind.isPressed();
            keybind.isKeyDown = keybind.isKeyDown();
            if (previousPressed != keybind.isPressed || previousKeyDown != keybind.isKeyDown) {
                updating.add(keybind);
            }
        }
        if (!updating.isEmpty()) {
            try {
                GTNetwork.sendToServer(new CPacketKeysPressed(updating));
            } catch (NullPointerException exception) {
                GTCEu.LOGGER.error("Keys pressed packet failed to send with an exception", exception);
            }
        }
    }

    public static void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        Arrays.stream(VALUES).forEach(value -> {
            if (value.keybindingGetter == null) {
                event.register(value.keybinding);
            } else {
                value.keybinding = value.keybindingGetter.get().get();
            }
        });
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        mouseDelta = event.getScrollDelta();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean scrollingUp() {
        return mouseDelta > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean notScrolling() {
        return mouseDelta == 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean scrollingDown() {
        return mouseDelta < 0;
    }

    @OnlyIn(Dist.CLIENT)
    private Supplier<Supplier<KeyMapping>> keybindingGetter;
    @OnlyIn(Dist.CLIENT)
    private KeyMapping keybinding;
    @OnlyIn(Dist.CLIENT)
    private boolean isPressed, isKeyDown;

    private final WeakHashMap<ServerPlayer, BooleanBooleanMutablePair> mapping = new WeakHashMap<>();

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind(Supplier<Supplier<KeyMapping>> keybindingGetter) {
        if (GTCEu.isClientSide()) {
            this.keybindingGetter = keybindingGetter;
        }
    }

    KeyBind(String langKey, int button) {
        if (GTCEu.isClientSide()) {
            this.keybinding = new KeyMapping(langKey, button, GTCEu.NAME);
        }
    }

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (GTCEu.isClientSide()) {
            this.keybinding = new KeyMapping(langKey, ctx, InputConstants.Type.KEYSYM, button, GTCEu.NAME);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping toMinecraft() {
        return this.keybinding;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isPressed() {
        return this.keybinding.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isDown();
    }

    public void update(boolean pressed, boolean keyDown, ServerPlayer player) {
        BooleanBooleanMutablePair pair = this.mapping.get(player);
        if (pair == null) {
            this.mapping.put(player, BooleanBooleanMutablePair.of(pressed, keyDown));
        } else {
            pair.left(pressed);
            pair.right(keyDown);
        }
    }

    public boolean isPressed(Player player) {
        if (player.level().isClientSide) {
            return isPressed();
        } else {
            BooleanBooleanMutablePair pair = this.mapping.get((ServerPlayer) player);
            return pair != null && pair.leftBoolean();
        }
    }

    public boolean isKeyDown(Player player) {
        if (player.level().isClientSide) {
            return isKeyDown();
        } else {
            BooleanBooleanMutablePair pair = this.mapping.get((ServerPlayer) player);
            return pair != null && pair.rightBoolean();
        }
    }
}
