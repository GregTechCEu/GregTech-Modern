package com.gregtechceu.gtceu.utils.input;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeyDown;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public enum SyncedKeyMapping {

    ARMOR_MODE_SWITCH("gtceu.key.armor_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_M)

    ;

    public static final SyncedKeyMapping[] VALUES = values();

    @OnlyIn(Dist.CLIENT)
    private KeyMapping keyMapping;
    @OnlyIn(Dist.CLIENT)
    private int keyCode;
    @OnlyIn(Dist.CLIENT)
    private boolean isKeyDown;

    private static final Int2BooleanMap updatingKeyDown = new Int2BooleanOpenHashMap();

    private final WeakHashMap<ServerPlayer, Boolean> serverMapping = new WeakHashMap<>();
    private final WeakHashMap<ServerPlayer, Set<IKeyPressedListener>> playerListeners = new WeakHashMap<>();
    private final Set<IKeyPressedListener> globalListeners = Collections.newSetFromMap(new WeakHashMap<>());

    public static void init() {
        if (GTCEu.isClientSide()) {
            MinecraftForge.EVENT_BUS.register(SyncedKeyMapping.class);
        }
    }

    /**
     * Create a SyncedKeyMapping wrapper around a Minecraft {@link KeyMapping}.
     *
     * @param mcKeyMapping Doubly-wrapped supplier around a keymapping from
     *                     {@link net.minecraft.client.Options Minecraft.getInstance().options}.
     */
    SyncedKeyMapping(Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        if (GTCEu.isClientSide()) {
            this.keyMapping = mcKeyMapping.get().get();
        }
    }

    /**
     * Create a new SyncedKeyMapping for a specified key code.
     *
     * @param keyCode The key code.
     */
    SyncedKeyMapping(int keyCode) {
        if (GTCEu.isClientSide()) {
            this.keyCode = keyCode;
        }
    }

    /**
     * Create a new SyncedKeyMapping with server held and pressed syncing to server.<br>
     * Will automatically create a keymapping entry in the MC options page.
     *
     * @param nameKey Translation key for the keymapping name.
     * @param ctx     Conflict context for the keymapping options category.
     * @param keyCode The key code, from {@link InputConstants}.
     */
    SyncedKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode) {
        if (GTCEu.isClientSide()) {
            this.keyMapping = (KeyMapping) createKeyMapping(nameKey, ctx, keyCode);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private Object createKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode) {
        return new KeyMapping(nameKey, ctx, InputConstants.Type.KEYSYM, keyCode, GTCEu.NAME);
    }

    /**
     * Check if a player is currently holding down this key.
     *
     * @param player The player to check.
     *
     * @return If the key is held.
     */
    public boolean isKeyDown(Player player) {
        if (player.level().isClientSide) {
            if (keyMapping != null) {
                return keyMapping.isDown();
            }
            long id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, keyCode);
        }
        Boolean isKeyDown = serverMapping.get((ServerPlayer) player);
        return isKeyDown != null ? isKeyDown : false;
    }

    /**
     * Registers an {@link IKeyPressedListener} to this key, which will have its {@link IKeyPressedListener#onKeyPressed
     * onKeyPressed} method called when the provided player presses this key.
     *
     * @param player   The player who owns this listener.
     * @param listener The handler for the key clicked event.
     */
    public SyncedKeyMapping registerPlayerListener(ServerPlayer player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners
                .computeIfAbsent(player, $ -> Collections.newSetFromMap(new WeakHashMap<>()));
        listenerSet.add(listener);
        return this;
    }

    /**
     * Remove a player's listener on this keymapping for a provided player.
     *
     * @param player   The player who owns this listener.
     * @param listener The handler for the key clicked event.
     */
    public void removePlayerListener(ServerPlayer player, IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Registers an {@link IKeyPressedListener} to this key, which will have its {@link IKeyPressedListener#onKeyPressed
     * onKeyPressed} method called when any player presses this key.
     *
     * @param listener The handler for the key clicked event.
     */
    public SyncedKeyMapping registerGlobalListener(IKeyPressedListener listener) {
        globalListeners.add(listener);
        return this;
    }

    /**
     * Remove a global listener on this keybinding.
     *
     * @param listener The handler for the key clicked event.
     */
    public void removeGlobalListener(IKeyPressedListener listener) {
        globalListeners.remove(listener);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            updatingKeyDown.clear();
            for (var keyMapping : VALUES) {
                boolean previousKeyDown = keyMapping.isKeyDown;

                if (keyMapping.keyMapping != null) {
                    keyMapping.isKeyDown = keyMapping.keyMapping.isDown();
                } else {
                    long id = Minecraft.getInstance().getWindow().getWindow();
                    keyMapping.isKeyDown = InputConstants.isKeyDown(id, keyMapping.keyCode);
                }

                if (previousKeyDown != keyMapping.isKeyDown) {
                    updatingKeyDown.put(keyMapping.ordinal(), keyMapping.isKeyDown);
                }
            }
            if (!updatingKeyDown.isEmpty()) {
                GTNetwork.sendToServer(new CPacketKeyDown(updatingKeyDown));
            }
        }
    }

    @ApiStatus.Internal
    public void serverActivate(boolean keyDown, ServerPlayer player) {
        this.serverMapping.put(player, keyDown);

        // Player listeners
        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null && !listenerSet.isEmpty()) {
            for (IKeyPressedListener listener : listenerSet) {
                listener.onKeyPressed(player, this, keyDown);
            }
        }
        // Global listeners
        for (IKeyPressedListener listener : globalListeners) {
            listener.onKeyPressed(player, this, keyDown);
        }
    }
}
