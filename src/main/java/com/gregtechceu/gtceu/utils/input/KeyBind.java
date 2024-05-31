package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeysDown;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeysPressed;

import com.lowdragmc.lowdraglib.Platform;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.InputConstants;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public enum KeyBind {

    VANILLA_JUMP(() -> () -> Minecraft.getInstance().options.keyJump),
    VANILLA_SNEAK(() -> () -> Minecraft.getInstance().options.keyShift),
    VANILLA_FORWARD(() -> () -> Minecraft.getInstance().options.keyUp),
    VANILLA_BACKWARD(() -> () -> Minecraft.getInstance().options.keyDown),
    VANILLA_LEFT(() -> () -> Minecraft.getInstance().options.keyLeft),
    VANILLA_RIGHT(() -> () -> Minecraft.getInstance().options.keyRight),
    ARMOR_MODE_SWITCH("gtceu.key.armor_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_M),
    ARMOR_HOVER("gtceu.key.armor_hover", KeyConflictContext.IN_GAME, InputConstants.KEY_H),
    ARMOR_CHARGING("gtceu.key.armor_charging", KeyConflictContext.IN_GAME, InputConstants.KEY_N),
    QUARK_TOOL_MODE_SWITCH("gtceu.key.quarktool_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_R),
    TOOL_AOE_CHANGE("gtceu.key.tool_aoe_change", KeyConflictContext.IN_GAME, InputConstants.KEY_V);

    public static final KeyBind[] VALUES = values();

    private static double mouseDelta = 0.0;
    @OnlyIn(Dist.CLIENT)
    private KeyMapping keybinding;
    @OnlyIn(Dist.CLIENT)
    private boolean isKeyDown;
    @OnlyIn(Dist.CLIENT)
    private boolean isPressed;
    private final WeakHashMap<ServerPlayer, Boolean> mapping = new WeakHashMap<>();
    private final WeakHashMap<ServerPlayer, Set<IKeyPressedListener>> listeners = new WeakHashMap<>();
    public static void init() {
        GTCEu.LOGGER.info("Registering KeyBinds");
        if (Platform.isClient()) {
            MinecraftForge.EVENT_BUS.register(KeyBind.class);
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


    /**
     * Handle Keys which we track for "holds" on the server, meaning if a key is being pressed
     * down for a prolonged period of time. This is a state which gets saved on the server.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Int2BooleanMap updatingKeyDown = new Int2BooleanOpenHashMap();
            for (KeyBind keybind : VALUES) {
                boolean previousKeyDown = keybind.isKeyDown;
                keybind.isKeyDown = keybind.keybinding.isDown();
                if (previousKeyDown != keybind.isKeyDown) {
                    updatingKeyDown.put(keybind.ordinal(), keybind.isKeyDown());
                }
            }
            if (!updatingKeyDown.isEmpty()) {
                GTNetwork.NETWORK.sendToServer(new CPacketKeysDown(updatingKeyDown));
            }
        }
    }

    public void updateKeyDown(boolean keyDown, ServerPlayer player) {
        this.mapping.put(player, keyDown);
    }

    public boolean isKeyDown(Player player) {
        if (player.level().isClientSide) return keybinding.isDown();
        // potential NPE here on unboxing if it is returned directly
        Boolean isKeyDown = mapping.get((ServerPlayer) player);
        return isKeyDown != null ? isKeyDown : false;
    }

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    KeyBind(Supplier<Supplier<KeyMapping>> keybindingGetter) {
        if (Platform.isClient()) {
            this.keybindingGetter = keybindingGetter;
        }
    }

    KeyBind(String langKey, int button) {
        if (Platform.isClient()) {
            this.keybinding = new KeyMapping(langKey, button, GTCEu.NAME);
        }
    }

    KeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (Platform.isClient()) {
            this.keybinding = new KeyMapping(langKey, ctx, InputConstants.Type.KEYSYM, button, GTCEu.NAME);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public KeyMapping toMinecraft() {
        return this.keybinding;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isPressed() {
        return this.keybinding.consumeClick();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isDown();
    }

    /**
     * Handle Keys which we track for "presses" on the server, meaning a single input which
     * sends a packet to the server which informs all listeners.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onInputEvent(InputEvent.Key event) {
        IntList updatingPressed = new IntArrayList();
        for (KeyBind keybind : VALUES) {
            if (keybind.keybinding.consumeClick()) {
                updatingPressed.add(keybind.ordinal());
            }
        }
        if (!updatingPressed.isEmpty()) {
            GTNetwork.NETWORK.sendToServer(new CPacketKeysPressed(updatingPressed));
        }
    }

    public void onKeyPressed(ServerPlayer player) {
        Set<IKeyPressedListener> listenerSet = listeners.get(player);
        if (listenerSet != null && !listenerSet.isEmpty()) {
            for (var listener : listenerSet) {
                listener.onKeyPressed(player, this);
            }
        }
    }

    public void registerListener(ServerPlayer player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = listeners.computeIfAbsent(player, k -> new HashSet<>());
        listenerSet.add(listener);
    }

    public void removeListener(ServerPlayer player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = listeners.get(player);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }


}
