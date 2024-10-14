package com.gregtechceu.gtceu.utils.virtualregistry;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class VirtualEnderRegistry extends SavedData {

    private static final String DATA_ID = GTCEu.MOD_ID + ".virtual_entry_data";
    private static final String PUBLIC_KEY = "Public";
    private static final String PRIVATE_KEY = "Private";
    private static final Map<UUID, VirtualRegistryMap> VIRTUAL_REGISTRIES = new HashMap<>();

    private final ServerLevel serverLevel;

    public static VirtualEnderRegistry getOrCreate(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(tag -> new VirtualEnderRegistry(level, tag),
                () -> new VirtualEnderRegistry(level),"gtceu_virtual_ender_registry");
    }

    public VirtualEnderRegistry(ServerLevel level) {
        this.serverLevel = level;
    }

    public VirtualEnderRegistry(ServerLevel level, CompoundTag tag) {
        this(level);


        if(tag.contains(PUBLIC_KEY)) {
            VIRTUAL_REGISTRIES.put(null, new VirtualRegistryMap(tag.getCompound(PUBLIC_KEY)));
        }
        if(tag.contains(PRIVATE_KEY)) {
            CompoundTag privateEntries = tag.getCompound(PRIVATE_KEY);
            for(String owner : privateEntries.getAllKeys()) {
                var privateMap = privateEntries.getCompound(owner);
                VIRTUAL_REGISTRIES.put(UUID.fromString(owner), new VirtualRegistryMap(privateMap));
            }
        }
    }


    public static <T extends VirtualEntry> T getEntry(@Nullable UUID owner, EntryTypes<T> type, String name) {
        return getRegistry(owner).getEntry(type, name);
    }

    public static void addEntry(@Nullable UUID owner, String name, VirtualEntry entry) {
        getRegistry(owner).addEntry(name, entry);
    }

    public static boolean hasEntry(@Nullable UUID owner, EntryTypes<?> type, String name) {
        return getRegistry(owner).contains(type, name);
    }

    public static @NotNull <T extends VirtualEntry> T getOrCreateEntry(@Nullable UUID owner, EntryTypes<T> type,
                                                                       String name) {
        if (!hasEntry(owner, type, name))
            addEntry(owner, name, type.createInstance());

        return getEntry(owner, type, name);
    }

    /**
     * Removes an entry from the registry. Use with caution!
     *
     * @param owner The uuid of the player the entry is private to, or null if the entry is public
     * @param type  Type of the registry to remove from
     * @param name  The name of the entry
     */
    public static void deleteEntry(@org.jetbrains.annotations.Nullable UUID owner, EntryTypes<?> type, String name) {
        var registry = getRegistry(owner);
        if (registry.contains(type, name)) {
            registry.deleteEntry(type, name);
            return;
        }
        GTCEu.LOGGER.warn("Attempted to delete {} entry {} of type {}, which does not exist",
                owner == null ? "public" : String.format("private [%s]", owner), name, type);
    }

    public static <T extends VirtualEntry> void deleteEntry(@org.jetbrains.annotations.Nullable UUID owner, EntryTypes<T> type, String name,
                                                            Predicate<T> shouldDelete) {
        T entry = getEntry(owner, type, name);
        if (entry != null && shouldDelete.test(entry))
            deleteEntry(owner, type, name);
    }

    public static Set<String> getEntryNames(UUID owner, EntryTypes<?> type) {
        return getRegistry(owner).getEntryNames(type);
    }

    /**
     * To be called on server stopped event
     */
    public static void clearMaps() {
        VIRTUAL_REGISTRIES.clear();
    }

    private static VirtualRegistryMap getRegistry(UUID owner) {
        return VIRTUAL_REGISTRIES.computeIfAbsent(owner, key -> new VirtualRegistryMap());
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        var privateTag = new CompoundTag();
        for(var owner : VIRTUAL_REGISTRIES.keySet()) {
            var mapTag = VIRTUAL_REGISTRIES.get(owner).serializeNBT();
            if(owner != null) {
                privateTag.put(owner.toString(), mapTag);
            } else {
                compoundTag.put(PUBLIC_KEY, mapTag);
            }
        }
        compoundTag.put(PRIVATE_KEY, privateTag);
        return compoundTag;
    }
}
