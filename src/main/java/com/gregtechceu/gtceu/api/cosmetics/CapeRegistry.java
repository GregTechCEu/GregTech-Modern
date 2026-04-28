package com.gregtechceu.gtceu.api.cosmetics;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.common.network.packets.SPacketNotifyCapeChange;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.RegisterCapesEventJS;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.common.commands.GTCommands.ERROR_NO_SUCH_CAPE;

public class CapeRegistry extends SavedData {

    /**
     * pseudo-registry lookup map of ID->texture.
     */
    public static final Map<Identifier, Identifier> ALL_CAPES = new HashMap<>();
    /**
     * Set of all the free capes' IDs
     */
    private static final Set<Identifier> FREE_CAPES = new HashSet<>();

    // This map should always have TreeSet values for iteration consistency.
    private static final Map<UUID, Set<Identifier>> UNLOCKED_CAPES = new HashMap<>();
    private static final Map<UUID, Identifier> CURRENT_CAPES = new HashMap<>();

    private static final CapeRegistry INSTANCE = new CapeRegistry();

    private CapeRegistry() {}

    private static void initCapes() {
        RegisterGTCapesEvent event = new RegisterGTCapesEvent();
        NeoForge.EVENT_BUS.post(event);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent(event);
        }
        save();
    }

    public static void registerToServer(ServerLevel level) {
        CapeRegistry.INSTANCE.init();
    }

    private CapeRegistry init() {
        clearMaps();
        initCapes();
        return this;
    }

    public static void save() {
        INSTANCE.setDirty();
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag unlockedCapesTag = new ListTag();
        for (Map.Entry<UUID, Set<Identifier>> entry : UNLOCKED_CAPES.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putIntArray("owner", UUIDUtil.uuidToIntArray(entry.getKey()));

            ListTag capesTag = new ListTag();
            for (Identifier cape : entry.getValue()) {
                capesTag.add(StringTag.valueOf(cape.toString()));
            }
            entryTag.put("capes", capesTag);

            unlockedCapesTag.add(entryTag);
        }
        tag.put("unlocked_capes", unlockedCapesTag);

        ListTag currentCapesTag = new ListTag();
        for (Map.Entry<UUID, Identifier> entry : CURRENT_CAPES.entrySet()) {
            if (entry.getValue() == null)
                continue;
            String capeLocation = entry.getValue().toString();

            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("cape", capeLocation);
            entryTag.putIntArray("owner", UUIDUtil.uuidToIntArray(entry.getKey()));

            currentCapesTag.add(entryTag);
        }
        tag.put("current_capes", currentCapesTag);

        return tag;
    }

    private CapeRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        init();

        ListTag unlockedCapesTag = tag.getListOrEmpty("unlocked_capes");
        for (int i = 0; i < unlockedCapesTag.size(); i++) {
            CompoundTag entryTag = unlockedCapesTag.getCompoundOrEmpty(i);
            UUID uuid = entryTag.getIntArray("owner").map(UUIDUtil::uuidFromIntArray).orElse(new UUID(0L, 0L));

            Set<Identifier> capes = UNLOCKED_CAPES.computeIfAbsent(uuid, CapeRegistry::makeSet);

            ListTag capesTag = entryTag.getListOrEmpty("capes");
            for (int j = 0; j < capesTag.size(); j++) {
                String capeId = capesTag.getString(j).orElse("");
                if (capeId.isEmpty())
                    continue;
                capes.add(Identifier.parse(capeId));
            }
            UNLOCKED_CAPES.put(uuid, capes);
        }

        ListTag currentCapesTag = tag.getListOrEmpty("current_capes");
        for (int i = 0; i < currentCapesTag.size(); i++) {
            CompoundTag entryTag = currentCapesTag.getCompoundOrEmpty(i);
            String capeId = entryTag.getStringOr("cape", "");
            if (capeId.isEmpty())
                continue;
            UUID uuid = entryTag.getIntArray("owner").map(UUIDUtil::uuidFromIntArray).orElse(new UUID(0L, 0L));
            CURRENT_CAPES.put(uuid, Identifier.parse(capeId));
        }

        return this;
    }

    @Nullable
    public static Identifier getPlayerCapeId(UUID uuid) {
        return CURRENT_CAPES.get(uuid);
    }

    @Nullable
    public static Identifier getPlayerCapeTexture(UUID uuid) {
        return ALL_CAPES.getOrDefault(getPlayerCapeId(uuid), null);
    }

    /**
     * Allows one to check what capes a specific player has unlocked through CapesRegistry.
     *
     * @param uuid The player data used to get what capes the player has through internal maps.
     * @return A list of ResourceLocations containing the cape textures that the player has unlocked.
     */
    public static Set<Identifier> getUnlockedCapes(UUID uuid) {
        return UNLOCKED_CAPES.getOrDefault(uuid, Collections.emptySet());
    }

    /**
     * Registers a cape.<br>
     * use {@link RegisterGTCapesEvent#registerCape(Identifier, Identifier)} instead of calling this
     * directly.
     *
     * @param id      An identifier for the cape
     * @param texture The full path to the cape's texture in a resource pack
     *
     * @see RegisterGTCapesEvent#registerCape(Identifier, Identifier)
     */
    @ApiStatus.Internal
    public static void registerCape(Identifier id, Identifier texture) {
        ALL_CAPES.put(id, texture);
    }

    /**
     * Registers a cape that will always be unlocked for all players.<br>
     * use {@link RegisterGTCapesEvent#registerCape(Identifier, Identifier)} instead of calling this
     * directly.
     *
     * @param id      An identifier for the cape
     * @param texture The full path to the cape's texture in a resource pack
     *
     * @see RegisterGTCapesEvent#registerFreeCape(Identifier, Identifier)
     */
    @ApiStatus.Internal
    public static void registerFreeCape(Identifier id, Identifier texture) {
        registerCape(id, texture);
        FREE_CAPES.add(id);
    }

    /**
     * Automatically makes a cape available to a player.<br>
     * <strong>DOES NOT SAVE AUTOMATICALLY;
     * PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.</strong>
     *
     * @param owner The UUID of the player to give the cape to
     * @param cape  The cape to give
     * @see #removeCape(UUID, Identifier)
     */
    @SneakyThrows(CommandSyntaxException.class)
    public static boolean unlockCape(UUID owner, @NotNull Identifier cape) {
        if (!CapeRegistry.ALL_CAPES.containsKey(cape)) {
            throw ERROR_NO_SUCH_CAPE.create(cape.toString());
        }
        Set<Identifier> capes = UNLOCKED_CAPES.computeIfAbsent(owner, CapeRegistry::makeSet);
        if (capes.contains(cape)) {
            return false;
        }
        capes.add(cape);
        UNLOCKED_CAPES.put(owner, capes);
        return true;
    }

    /**
     * Automatically removes a cape from a player.<br>
     * <strong>DOES NOT SAVE AUTOMATICALLY;
     * PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.</strong>
     *
     * @param owner The UUID of the player to take the cape from
     * @param cape  The cape to take
     * @see #unlockCape(UUID, Identifier)
     */
    @SneakyThrows(CommandSyntaxException.class)
    public static boolean removeCape(UUID owner, @NotNull Identifier cape) {
        if (!CapeRegistry.ALL_CAPES.containsKey(cape)) {
            throw ERROR_NO_SUCH_CAPE.create(cape.toString());
        }
        if (FREE_CAPES.contains(cape)) {
            return false;
        }
        Set<Identifier> capes = UNLOCKED_CAPES.get(owner);
        if (capes == null || !capes.contains(cape)) {
            return false;
        }
        capes.remove(cape);
        UNLOCKED_CAPES.put(owner, capes);
        if (cape.equals(getPlayerCapeId(owner))) {
            setActiveCape(owner, null);
        }
        return true;
    }

    public static void clearMaps() {
        UNLOCKED_CAPES.clear();
        CURRENT_CAPES.clear();
    }

    @SneakyThrows(CommandSyntaxException.class)
    public static void giveRawCape(UUID uuid, @Nullable Identifier cape) {
        if (cape != null && !CapeRegistry.ALL_CAPES.containsKey(cape)) {
            throw ERROR_NO_SUCH_CAPE.create(cape.toString());
        }
        CURRENT_CAPES.put(uuid, cape);
    }

    /**
     * Sets a player's current cape.
     *
     * @param player The UUID of the player
     * @param cape   The cape to set, or {@code null} to remove the current cape.
     */
    @SneakyThrows(CommandSyntaxException.class)
    public static boolean setActiveCape(UUID player, @Nullable Identifier cape) {
        if (cape != null && !CapeRegistry.ALL_CAPES.containsKey(cape)) {
            throw ERROR_NO_SUCH_CAPE.create(cape.toString());
        }
        Set<Identifier> capes = UNLOCKED_CAPES.get(player);
        if (capes == null || cape != null && !capes.contains(cape)) {
            return false;
        }
        CURRENT_CAPES.put(player, cape);
        PacketDistributor.sendToAllPlayers(new SPacketNotifyCapeChange(player, cape));
        save();
        return true;
    }

    // For loading capes when the player logs in, so that it's synced to the clients.
    public static void loadCurrentCapesOnLogin(ServerPlayer player) {
        UUID uuid = player.getUUID();
        // sync to others
        PacketDistributor.sendToAllPlayers(new SPacketNotifyCapeChange(uuid, CURRENT_CAPES.get(uuid)));
        // sync to the one who's logging in
        for (ServerPlayer otherPlayer : player.level().getServer().getPlayerList().getPlayers()) {
            uuid = otherPlayer.getUUID();
            PacketDistributor.sendToPlayer(player, new SPacketNotifyCapeChange(uuid, CURRENT_CAPES.get(uuid)));
        }
    }

    // Runs on login and gives the player all free capes & capes they've already unlocked.
    public static void detectNewCapes(ServerPlayer player) {
        var playerCapes = UNLOCKED_CAPES.get(player.getUUID());
        if (playerCapes == null || !playerCapes.containsAll(FREE_CAPES)) {
            for (Identifier cape : FREE_CAPES) {
                unlockCape(player.getUUID(), cape);
            }
            save();
        }
    }

    private static final Comparator<Identifier> SET_COMPARATOR = (o1, o2) -> {
        int result = o1.compareTo(o2);
        boolean isFirstFree = FREE_CAPES.contains(o1);
        if (isFirstFree ^ FREE_CAPES.contains(o2)) {
            if (isFirstFree) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return result;
        }
    };

    private static Set<Identifier> makeSet(UUID ignored) {
        return new TreeSet<>(SET_COMPARATOR);
    }

    private static class KJSCallWrapper {

        public static void fireKJSEvent(RegisterGTCapesEvent event) {
            GTCEuServerEvents.REGISTER_CAPES.post(ScriptType.SERVER, new RegisterCapesEventJS(event));
        }
    }
}
