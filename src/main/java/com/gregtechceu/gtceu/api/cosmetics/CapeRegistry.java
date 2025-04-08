package com.gregtechceu.gtceu.api.cosmetics;

import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SPacketNotifyCapeChange;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("DeprecatedIsStillUsed")
public class CapeRegistry extends SavedData {

    /**
     * pseudo-registry lookup map of ID->texture.
     */
    public static final Map<ResourceLocation, ResourceLocation> ALL_CAPES = new HashMap<>();
    /**
     * Set of all the free capes' IDs
     */
    private static final Set<ResourceLocation> FREE_CAPES = new HashSet<>();

    private static final Map<UUID, List<ResourceLocation>> UNLOCKED_CAPES = new HashMap<>();
    private static final Map<UUID, ResourceLocation> CURRENT_CAPES = new HashMap<>();

    private static final CapeRegistry INSTANCE = new CapeRegistry();

    private CapeRegistry() {}

    private static void initCapes() {
        MinecraftForge.EVENT_BUS.post(new RegisterGTCapesEvent());
        save();
    }

    private static void registerDevCapes() {

        save();
    }

    public static void registerToServer(ServerLevel level) {
        level.getDataStorage().computeIfAbsent(CapeRegistry.INSTANCE::load, CapeRegistry.INSTANCE::init, "gtceu_capes");
    }

    private CapeRegistry init() {
        clearMaps();
        initCapes();
        registerDevCapes();
        return this;
    }

    public static void save() {
        INSTANCE.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag unlockedCapesTag = new ListTag();
        for (Map.Entry<UUID, List<ResourceLocation>> entry : UNLOCKED_CAPES.entrySet()) {
            for (ResourceLocation cape : entry.getValue()) {
                String capeLocation = cape.toString();

                CompoundTag entryTag = new CompoundTag();

                entryTag.putString("cape", capeLocation);
                entryTag.putUUID("owner", entry.getKey());

                unlockedCapesTag.add(entryTag);
            }
        }
        tag.put("unlocked_capes", unlockedCapesTag);

        ListTag currentCapesTag = new ListTag();
        for (Map.Entry<UUID, ResourceLocation> entry : CURRENT_CAPES.entrySet()) {
            if (entry.getValue() == null)
                continue;
            String capeLocation = entry.getValue().toString();

            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("cape", capeLocation);
            entryTag.putUUID("owner", entry.getKey());

            currentCapesTag.add(entryTag);
        }
        tag.put("current_capes", currentCapesTag);

        return tag;
    }

    private CapeRegistry load(CompoundTag tag) {
        init();

        ListTag unlockedCapesTag = tag.getList("unlocked_capes", Tag.TAG_COMPOUND);
        for (int i = 0; i < unlockedCapesTag.size(); i++) {
            CompoundTag entryTag = unlockedCapesTag.getCompound(i);
            String capeLocation = entryTag.getString("cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = entryTag.getUUID("owner");

            List<ResourceLocation> capes = UNLOCKED_CAPES.computeIfAbsent(uuid, $ -> new ArrayList<>());
            capes.add(new ResourceLocation(capeLocation));
            UNLOCKED_CAPES.put(uuid, capes);
        }

        ListTag currentCapesTag = tag.getList("current_capes", Tag.TAG_COMPOUND);
        for (int i = 0; i < currentCapesTag.size(); i++) {
            CompoundTag entryTag = currentCapesTag.getCompound(i);
            String capeLocation = entryTag.getString("cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = entryTag.getUUID("owner");
            CURRENT_CAPES.put(uuid, new ResourceLocation(capeLocation));
        }

        return this;
    }

    public static ResourceLocation getPlayerCapeId(UUID uuid) {
        return CURRENT_CAPES.get(uuid);
    }

    public static ResourceLocation getPlayerCapeTexture(UUID uuid) {
        return ALL_CAPES.get(getPlayerCapeId(uuid));
    }

    /**
     * Allows one to check what capes a specific player has unlocked through CapesRegistry.
     *
     * @param uuid The player data used to get what capes the player has through internal maps.
     * @return A list of ResourceLocations containing the cape textures that the player has unlocked.
     */
    public static List<ResourceLocation> getUnlockedCapes(UUID uuid) {
        return UNLOCKED_CAPES.getOrDefault(uuid, Collections.emptyList());
    }

    /**
     * Makes a cape available to the {@code /gtceu cape} command, allowing it to be used in advancements etc.
     *
     * @param id   An identifier for giving the cape with commands etc.
     * @param cape The ResourceLocation that points to the cape that can be unlocked through the advancement.
     *
     * @deprecated use the {@link com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent} event.
     */
    @ApiStatus.Internal
    public static void registerCape(ResourceLocation id, ResourceLocation cape) {
        ALL_CAPES.put(id, cape);
    }

    /**
     * Adds a cape that will always be unlocked for all players.
     *
     * @param id   An identifier for giving the cape with commands etc.
     * @param cape A ResourceLocation pointing to the cape texture.
     *
     * @deprecated use the {@link com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent} event.
     */
    @ApiStatus.Internal
    public static void registerFreeCape(ResourceLocation id, ResourceLocation cape) {
        registerCape(id, cape);
        FREE_CAPES.add(id);
    }

    /**
     * Automatically gives a cape to a player. may be used for a reward etc.
     * <br>
     * DOES NOT SAVE AUTOMATICALLY; PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.
     *
     * @param owner  The UUID of the player to be given the cape.
     * @param capeId The ResourceLocation that holds the cape used here.
     */
    public static boolean unlockCape(UUID owner, ResourceLocation capeId) {
        List<ResourceLocation> capes = UNLOCKED_CAPES.computeIfAbsent(owner, $ -> new ArrayList<>());
        if (capes.contains(capeId)) {
            return false;
        }
        capes.add(capeId);
        UNLOCKED_CAPES.put(owner, capes);
        return true;
    }

    /**
     * Automatically removes a cape from a player
     * <br>
     * DOES NOT SAVE AUTOMATICALLY; PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.
     *
     * @param uuid The UUID of the player to be given the cape.
     * @param cape The ResourceLocation that holds the cape used here.
     */
    public static boolean removeCape(UUID uuid, ResourceLocation cape) {
        List<ResourceLocation> capes = UNLOCKED_CAPES.get(uuid);
        if (capes == null || !capes.contains(cape)) {
            return false;
        }
        capes.remove(cape);
        UNLOCKED_CAPES.put(uuid, capes);
        return true;
    }

    public static void clearMaps() {
        UNLOCKED_CAPES.clear();
        CURRENT_CAPES.clear();
    }

    public static void giveRawCape(UUID uuid, ResourceLocation cape) {
        CURRENT_CAPES.put(uuid, cape);
    }

    public static void giveCape(UUID uuid, ResourceLocation cape) {
        CURRENT_CAPES.put(uuid, cape);
        GTNetwork.NETWORK.sendToAll(new SPacketNotifyCapeChange(uuid, cape));
        save();
    }

    // For loading capes when the player logs in, so that it's synced to the clients.
    public static void loadCurrentCapesOnLogin(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            UUID uuid = player.getUUID();
            // sync to others
            GTNetwork.NETWORK.sendToAll(new SPacketNotifyCapeChange(uuid, CURRENT_CAPES.get(uuid)));
            // sync to the one who's logging in
            for (ServerPlayer otherPlayer : serverPlayer.getServer().getPlayerList().getPlayers()) {
                uuid = otherPlayer.getUUID();
                GTNetwork.NETWORK.sendToPlayer(new SPacketNotifyCapeChange(uuid, CURRENT_CAPES.get(uuid)), serverPlayer);
            }
        }
    }

    // Runs on login and gives the player all free capes & capes they've already unlocked.
    public static void detectNewCapes(Player player) {
        if (player instanceof ServerPlayer) {
            var playerCapes = UNLOCKED_CAPES.get(player.getUUID());
            if (playerCapes == null || !new HashSet<>(playerCapes).containsAll(FREE_CAPES)) {
                for (ResourceLocation cape : FREE_CAPES) {
                    unlockCape(player.getUUID(), cape);
                }
                save();
            }
        }
    }
}
