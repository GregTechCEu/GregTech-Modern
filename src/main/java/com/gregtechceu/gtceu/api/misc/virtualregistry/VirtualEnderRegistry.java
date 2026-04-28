package com.gregtechceu.gtceu.api.misc.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class VirtualEnderRegistry extends SavedData {

    private static final String DATA_ID = GTCEu.MOD_ID + ".virtual_entry_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";
    public static final SavedDataType<VirtualEnderRegistry> TYPE = new SavedDataType<>(
            GTCEu.id("virtual_entry_data"),
            serverLevel -> new VirtualEnderRegistry(),
            VirtualEnderRegistry::codec);
    private static volatile VirtualEnderRegistry data;
    private final Map<UUID, VirtualRegistryMap> VIRTUAL_REGISTRIES = new HashMap<>();

    public VirtualEnderRegistry() {}

    public VirtualEnderRegistry(CompoundTag name, HolderLookup.@NotNull Provider registries) {
        readFromNBT(registries, name);
    }

    public static VirtualEnderRegistry getInstance() {
        if (data == null) {
            var server = GTCEu.getMinecraftServer();
            if (server != null) {
                data = server.overworld().getDataStorage().computeIfAbsent(TYPE);
            }
        }

        return data;
    }

    private static Codec<VirtualEnderRegistry> codec(ServerLevel serverLevel) {
        return CompoundTag.CODEC.xmap(
                tag -> new VirtualEnderRegistry(tag, serverLevel.registryAccess()),
                data -> data.save(new CompoundTag(), serverLevel.registryAccess()));
    }

    /**
     * To be called on server stopped event
     */
    public static void release() {
        if (data != null) {
            data = null;
            GTCEu.LOGGER.debug("VirtualEnderRegistry has been unloaded");
        }
    }

    public <T extends VirtualEntry> T getEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        return getRegistry(owner).getEntry(type, name);
    }

    public void addEntry(@Nullable UUID owner, String name, VirtualEntry entry) {
        getRegistry(owner).addEntry(name, entry);
    }

    public boolean hasEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        return getRegistry(owner).contains(type, name);
    }

    public @NotNull <T extends VirtualEntry> T getOrCreateEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        if (!hasEntry(owner, type, name)) addEntry(owner, name, type.createInstance());
        return getEntry(owner, type, name);
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner The uuid of the player the entry is private to, or null if the entry is public
     * @param type  Type of the registry to remove from
     * @param name  The name of the entry
     */
    public void deleteEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        var registry = getRegistry(owner);
        if (registry.contains(type, name)) {
            registry.deleteEntry(type, name);
            return;
        }
        GTCEu.LOGGER.warn("Attempted to delete {} entry {} of type {}, which does not exist",
                owner == null ? "public" : String.format("private [%s]", owner), name, type);
    }

    public <T extends VirtualEntry> void deleteEntryIf(@Nullable UUID owner, EntryTypes<T> type, String name,
                                                       Predicate<T> shouldDelete) {
        T entry = getEntry(owner, type, name);
        if (entry != null && shouldDelete.test(entry)) deleteEntry(owner, type, name);
    }

    public Set<String> getEntryNames(UUID owner, EntryTypes<?> type) {
        return getRegistry(owner).getEntryNames(type);
    }

    private VirtualRegistryMap getRegistry(UUID owner) {
        return getInstance().VIRTUAL_REGISTRIES.computeIfAbsent(owner, key -> new VirtualRegistryMap());
    }

    public final void readFromNBT(HolderLookup.@NotNull Provider registries, CompoundTag nbt) {
        if (nbt.contains(PUBLIC_KEY)) {
            VIRTUAL_REGISTRIES.put(null, new VirtualRegistryMap(registries, nbt.getCompoundOrEmpty(PUBLIC_KEY)));
        }
        if (nbt.contains(PRIVATE_KEY)) {
            CompoundTag privateEntries = nbt.getCompoundOrEmpty(PRIVATE_KEY);
            for (String owner : privateEntries.keySet()) {
                var privateMap = privateEntries.getCompoundOrEmpty(owner);
                VIRTUAL_REGISTRIES.put(UUID.fromString(owner), new VirtualRegistryMap(registries, privateMap));
            }
        }
    }

    @NotNull
    public final CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        var privateTag = new CompoundTag();
        for (var owner : VIRTUAL_REGISTRIES.keySet()) {
            var mapTag = VIRTUAL_REGISTRIES.get(owner).serializeNBT(registries);
            if (owner != null) {
                privateTag.put(owner.toString(), mapTag);
            } else {
                tag.put(PUBLIC_KEY, mapTag);
            }
        }
        tag.put(PRIVATE_KEY, privateTag);
        return tag;
    }

    @Override
    public boolean isDirty() {
        // can't think of a good way to mark dirty other than always return true;
        return true;
    }
}
