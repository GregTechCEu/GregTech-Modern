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
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class SyncedKeyMapping {

    private static final Int2ObjectMap<SyncedKeyMapping> KEYMAPPINGS = new Int2ObjectOpenHashMap<>();
    private static int syncIndex = 0;

    @OnlyIn(Dist.CLIENT)
    private KeyMapping keyMapping;
    @OnlyIn(Dist.CLIENT)
    private Supplier<Supplier<KeyMapping>> keyMappingGetter;
    private final boolean needsRegister;
    @OnlyIn(Dist.CLIENT)
    private int keyCode;
    @OnlyIn(Dist.CLIENT)
    private boolean isKeyDown;

    private static final Int2BooleanMap updatingKeyDown = new Int2BooleanOpenHashMap();

    private final WeakHashMap<ServerPlayer, Boolean> serverMapping = new WeakHashMap<>();
    private final WeakHashMap<ServerPlayer, Set<IKeyPressedListener>> playerListeners = new WeakHashMap<>();
    private final Set<IKeyPressedListener> globalListeners = Collections.newSetFromMap(new WeakHashMap<>());

    private SyncedKeyMapping(Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        if (GTCEu.isClientSide()) {
            this.keyMappingGetter = mcKeyMapping;
        }
        // Does not need to be registered, will be registered by MC
        this.needsRegister = false;

        KEYMAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(int keyCode) {
        if (GTCEu.isClientSide() && !GTCEu.isDataGen()) {
            this.keyCode = keyCode;
        }
        // Does not need to be registered, is not a configurable key mapping
        this.needsRegister = false;

        KEYMAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode, String category) {
        if (GTCEu.isClientSide() && !GTCEu.isDataGen()) {
            this.keyMapping = (KeyMapping) createKeyMapping(nameKey, ctx, keyCode, category);
        }
        this.needsRegister = true;
        KEYMAPPINGS.put(syncIndex++, this);
    }

    /**
     * Create a SyncedKeyMapping wrapper around a Minecraft {@link KeyMapping}.
     *
     * @param mcKeyMapping Doubly-wrapped supplier around a keymapping from
     *                     {@link net.minecraft.client.Options Minecraft.getInstance().options}.
     */
    public static SyncedKeyMapping createFromMC(Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        return new SyncedKeyMapping(mcKeyMapping);
    }

    /**
     * Create a new SyncedKeyMapping for a specified key code.
     *
     * @param keyCode The key code.
     */
    public static SyncedKeyMapping create(int keyCode) {
        return new SyncedKeyMapping(keyCode);
    }

    /**
     * Create a new SyncedKeyMapping with server held and pressed syncing to server.<br>
     * Will automatically create a keymapping entry in the MC options page under the GregTechCEu category.
     *
     * @param nameKey Translation key for the keymapping name.
     * @param ctx     Conflict context for the keymapping options category.
     * @param keyCode The key code, from {@link InputConstants}.
     */
    public static SyncedKeyMapping createConfigurable(String nameKey, IKeyConflictContext ctx, int keyCode) {
        return createConfigurable(nameKey, ctx, keyCode, GTCEu.NAME);
    }

    /**
     * Create a new SyncedKeyMapping with server held and pressed syncing to server.<br>
     * Will automatically create a keymapping entry in the MC options page under the specified category.
     *
     * @param nameKey  Translation key for the keymapping name.
     * @param ctx      Conflict context for the keymapping options category.
     * @param keyCode  The key code, from {@link InputConstants}.
     * @param category The category in the MC options page.
     */
    public static SyncedKeyMapping createConfigurable(String nameKey, IKeyConflictContext ctx, int keyCode,
                                                      String category) {
        return new SyncedKeyMapping(nameKey, ctx, keyCode, category);
    }

    @OnlyIn(Dist.CLIENT)
    private Object createKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode, String category) {
        return new KeyMapping(nameKey, ctx, InputConstants.Type.KEYSYM, keyCode, category);
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

    public static void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        for (SyncedKeyMapping value : KEYMAPPINGS.values()) {
            if (value.keyMappingGetter != null) {
                value.keyMapping = value.keyMappingGetter.get().get();
                value.keyMappingGetter = null;
            }
            if (value.keyMapping != null && value.needsRegister) {
                event.register(value.keyMapping);
            }
        }
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
            for (var entry : KEYMAPPINGS.int2ObjectEntrySet()) {
                SyncedKeyMapping keyMapping = entry.getValue();
                boolean previousKeyDown = keyMapping.isKeyDown;

                if (keyMapping.keyMapping != null) {
                    keyMapping.isKeyDown = keyMapping.keyMapping.isDown();
                } else {
                    long id = Minecraft.getInstance().getWindow().getWindow();
                    keyMapping.isKeyDown = InputConstants.isKeyDown(id, keyMapping.keyCode);
                }

                if (previousKeyDown != keyMapping.isKeyDown) {
                    updatingKeyDown.put(entry.getIntKey(), keyMapping.isKeyDown);
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

    @ApiStatus.Internal
    public static SyncedKeyMapping getFromSyncId(int id) {
        return KEYMAPPINGS.get(id);
    }
}
