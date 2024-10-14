package com.gregtechceu.gtceu.utils.virtualregistry;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VirtualRegistryMap implements ITagSerializable<CompoundTag> {

    private final Map<EntryTypes<?>, Map<String, VirtualEntry>> registryMap = new HashMap<>();

    public VirtualRegistryMap(CompoundTag tag) {
        deserializeNBT(tag);
    }

    public VirtualRegistryMap() {}

    @Nullable
    public <T extends VirtualEntry> T getEntry(EntryTypes<T> type, String name) {
        if(!contains(type, name))
            return null;

        return (T) registryMap.get(type).get(name);
    }

    public void addEntry(String name, VirtualEntry entry) {
        registryMap.computeIfAbsent(entry.getType(), k -> new HashMap<>())
                .put(name, entry);
    }

    public boolean contains(EntryTypes<?> type, String name) {
        if(!registryMap.containsKey(type))
            return false;

        return registryMap.get(type).containsKey(name);
    }

    public void deleteEntry(EntryTypes<?> type, String name) {
        registryMap.get(type).remove(name);
    }

    public void clear() {
        registryMap.clear();
    }

    public Set<String> getEntryNames(EntryTypes<?> type) {
        return registryMap.get(type).keySet();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for(var type : registryMap.keySet()) {
            var entriesTag = new CompoundTag();
            var entries = registryMap.get(type);
            for(var name : entries.keySet()) {
                entriesTag.put(name, entries.get(name).serializeNBT());
            }
            tag.put(type.toString(), entriesTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for(var entryType : nbt.getAllKeys()) {
            EntryTypes<?> type;
            if(entryType.contains(":")) {
                type = EntryTypes.fromLocation(entryType);
            } else {
                type = EntryTypes.fromString(entryType);
            }
            if(type == null) continue;

            var virtualEntries = nbt.getCompound(entryType);
            for(var name : virtualEntries.getAllKeys()) {
                var entry = virtualEntries.getCompound(name);
                addEntry(name, type.createInstace(entry));
            }
        }
    }
}
