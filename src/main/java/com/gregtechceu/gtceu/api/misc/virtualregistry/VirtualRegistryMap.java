package com.gregtechceu.gtceu.api.misc.virtualregistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VirtualRegistryMap implements INBTSerializable<CompoundTag> {

    private final Map<EntryTypes<?>, Map<String, VirtualEntry>> registryMap = new Object2ObjectOpenHashMap<>();

    public VirtualRegistryMap() {}

    public VirtualRegistryMap(HolderLookup.Provider registries, CompoundTag tag) {
        deserializeNBT(registries, tag);
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends VirtualEntry> T getEntry(EntryTypes<T> type, String name) {
        return (T) registryMap.getOrDefault(type, Collections.emptyMap()).get(name);
    }

    public void addEntry(String name, VirtualEntry entry) {
        registryMap.computeIfAbsent(entry.getType(), k -> new Object2ObjectOpenHashMap<>()).put(name, entry);
    }

    public <T extends VirtualEntry> Map<String, VirtualEntry> getEntries(EntryTypes<T> type) {
        return registryMap.getOrDefault(type, new Object2ObjectOpenHashMap<>());
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

    public boolean isEmpty() {
        return registryMap.isEmpty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<EntryTypes<?>, Map<String, VirtualEntry>> entry : registryMap.entrySet()) {
            ListTag entriesTag = new ListTag();
            for (VirtualEntry innerEntry : entry.getValue().values()) {
                if (innerEntry.canRemove()) continue;
                entriesTag.add(innerEntry.serializeNBT(registries));
            }
            tag.put(entry.getKey().toString(), entriesTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        for (String entryTypeString : nbt.getAllKeys()) {
            ResourceLocation entryTypeLoc = ResourceLocation.tryParse(entryTypeString);
            if (entryTypeLoc == null) continue;
            EntryTypes<?> type = EntryTypes.fromLocation(entryTypeLoc);
            if (type == null) continue;

            Tag virtualEntries = nbt.get(entryTypeString);

            // backwards compat
            if (virtualEntries instanceof CompoundTag compoundTag) {
                for (String name : compoundTag.getAllKeys()) {
                    CompoundTag entryTag = compoundTag.getCompound(name);
                    VirtualEntry entry = type.createInstance(registries, entryTag);
                    if (entry.canRemove()) continue;
                    addEntry(entry.getColorStr(), type.createInstance(registries, entryTag));
                }
            } else {
                ListTag listTag = (ListTag) virtualEntries;
                for (int i = 0; i < Objects.requireNonNull(listTag).size(); i++) {
                    var entry = type.createInstance(registries, listTag.getCompound(i));
                    if (entry.canRemove()) continue;
                    addEntry(entry.getColorStr(), entry);
                }
            }

        }
    }
}
