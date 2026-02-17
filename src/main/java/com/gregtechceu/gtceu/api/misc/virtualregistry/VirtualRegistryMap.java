package com.gregtechceu.gtceu.api.misc.virtualregistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualRegistryMap implements INBTSerializable<CompoundTag> {

    private final Map<EntryTypes<?>, Map<String, VirtualEntry>> registryMap = new ConcurrentHashMap<>();

    public VirtualRegistryMap() {}

    public VirtualRegistryMap(CompoundTag tag) {
        deserializeNBT(tag);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends VirtualEntry> T getEntry(EntryTypes<T> type, String name) {
        return (T) registryMap.getOrDefault(type, Collections.emptyMap()).get(name);
    }

    public void addEntry(String name, VirtualEntry entry) {
        registryMap.computeIfAbsent(entry.getType(), k -> new ConcurrentHashMap<>()).put(name, entry);
    }

    public <T extends VirtualEntry> Map<String, VirtualEntry> getEntries(EntryTypes<T> type) {
        return registryMap.get(type);
    }

    public boolean contains(EntryTypes<?> type, String name) {
        return registryMap.containsKey(type) && registryMap.get(type).containsKey(name);
    }

    public void deleteEntry(EntryTypes<?> type, String name) {
        Map<String, VirtualEntry> entries = registryMap.get(type);
        if (entries != null) {
            entries.remove(name);
            if (entries.isEmpty()) {
                registryMap.remove(type);
            }
        }
    }

    public void clear() {
        registryMap.clear();
    }

    public Set<String> getEntryNames(EntryTypes<?> type) {
        return new HashSet<>(registryMap.getOrDefault(type, Collections.emptyMap()).keySet());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<EntryTypes<?>, Map<String, VirtualEntry>> entry : registryMap.entrySet()) {
            CompoundTag entriesTag = new CompoundTag();
            for (Map.Entry<String, VirtualEntry> subEntry : entry.getValue().entrySet()) {
                entriesTag.put(subEntry.getKey(), subEntry.getValue().serializeNBT());
            }
            tag.put(entry.getKey().toString(), entriesTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String entryTypeString : nbt.getAllKeys()) {
            ResourceLocation entryTypeLoc = ResourceLocation.tryParse(entryTypeString);
            if (entryTypeLoc == null) continue;
            EntryTypes<?> type = EntryTypes.fromLocation(entryTypeLoc);
            if (type == null) continue;

            CompoundTag virtualEntries = nbt.getCompound(entryTypeString);
            for (String name : virtualEntries.getAllKeys()) {
                CompoundTag entryTag = virtualEntries.getCompound(name);
                addEntry(name, type.createInstance(entryTag));
            }
        }
    }
}
