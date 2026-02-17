package com.gregtechceu.gtceu.api.misc.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VirtualEnderRegistry extends SavedData {

    private static final String DATA_ID = GTCEu.MOD_ID + ".virtual_entry_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";

    private VirtualRegistryMap PUBLIC_REGISTRY = new VirtualRegistryMap();
    private final Map<UUID, VirtualRegistryMap> VIRTUAL_REGISTRIES = new HashMap<>();

    public VirtualEnderRegistry() {}

    public VirtualEnderRegistry(CompoundTag name) {
        readFromNBT(name);
    }

    public static VirtualEnderRegistry get(ServerLevel sLvl) {
        return sLvl.getServer().overworld().getDataStorage()
                .computeIfAbsent(VirtualEnderRegistry::new, VirtualEnderRegistry::new, DATA_ID);
    }

    public <T extends VirtualEntry> @Nullable T getEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        if (owner == null) return PUBLIC_REGISTRY.getEntry(type, name);
        return getRegistry(owner).getEntry(type, name);
    }

    public <T extends VirtualEntry> Map<String, VirtualEntry> getEntries(@Nullable UUID owner, EntryTypes<T> type) {
        if (owner == null) return PUBLIC_REGISTRY.getEntries(type);
        return VIRTUAL_REGISTRIES.get(owner).getEntries(type);
    }

    public void addEntry(@Nullable UUID owner, String name, VirtualEntry entry) {
        if (owner == null) PUBLIC_REGISTRY.addEntry(name, entry);
        else getRegistry(owner).addEntry(name, entry);
    }

    public boolean hasEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        if (owner == null) return PUBLIC_REGISTRY.contains(type, name);
        return getRegistry(owner).contains(type, name);
    }

    public <T extends VirtualEntry> T getOrCreateEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        if (!hasEntry(owner, type, name)) addEntry(owner, name, type.createInstance());
        return Objects.requireNonNull(getEntry(owner, type, name));
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner The uuid of the player the entry is private to, or null if the entry is public
     * @param type  Type of the registry to remove from
     * @param name  The name of the entry
     */
    public void forceDeleteEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        if (owner == null) {
            PUBLIC_REGISTRY.deleteEntry(type, name);
            return;
        }

        var registry = getRegistry(owner);
        if (registry.contains(type, name)) {
            registry.deleteEntry(type, name);
        }
    }

    public <T extends VirtualEntry> void tryDeleteEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        T entry = getEntry(owner, type, name);
        if (entry == null) return;
        if (entry.canRemove()) forceDeleteEntry(owner, type, name);
    }

    private VirtualRegistryMap getRegistry(UUID owner) {
        return VIRTUAL_REGISTRIES.computeIfAbsent(owner, key -> new VirtualRegistryMap());
    }

    public final void readFromNBT(CompoundTag nbt) {
        if (nbt.contains(PUBLIC_KEY)) {
            PUBLIC_REGISTRY = new VirtualRegistryMap(nbt.getCompound(PUBLIC_KEY));
        }

        if (nbt.contains(PRIVATE_KEY)) {
            CompoundTag privateEntries = nbt.getCompound(PRIVATE_KEY);
            for (String owner : privateEntries.getAllKeys()) {
                var privateMap = privateEntries.getCompound(owner);
                VIRTUAL_REGISTRIES.put(UUID.fromString(owner), new VirtualRegistryMap(privateMap));
            }
        }
    }

    @Override
    public final CompoundTag save(CompoundTag tag) {
        var privateTag = new CompoundTag();
        tag.put(PUBLIC_KEY, PUBLIC_REGISTRY.serializeNBT());
        for (var owner : VIRTUAL_REGISTRIES.keySet()) {
            if (VIRTUAL_REGISTRIES.get(owner).isEmpty()) continue;
            var mapTag = VIRTUAL_REGISTRIES.get(owner).serializeNBT();
            privateTag.put(owner.toString(), mapTag);
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
