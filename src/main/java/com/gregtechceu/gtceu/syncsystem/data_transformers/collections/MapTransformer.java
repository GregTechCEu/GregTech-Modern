package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class MapTransformer<K, V> implements IValueTransformer<Map<K, V>> {

    private final IValueTransformer<K> keyTransformer;
    private final IValueTransformer<V> valueTransformer;

    public MapTransformer(IValueTransformer<K> keyTransformer, IValueTransformer<V> valueTransformer) {
        this.keyTransformer = keyTransformer;
        this.valueTransformer = valueTransformer;
    }

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(Map<K, V> value, ISyncManaged holder) {
        ListTag entries = new ListTag();
        for (var entry : value.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.put("k", keyTransformer.serializeNBT(entry.getKey(), null));
            compound.put("v", valueTransformer.serializeNBT(entry.getValue(), null));
            entries.add(compound);
        }
        return entries;
    }

    @Override
    public Map<K, V> deserializeNBT(Tag tag, ISyncManaged holder, Map<K, V> current) {
        if (!(tag instanceof ListTag listTag)) return current;
        current.clear();
        for (Tag entryTag : listTag) {
            CompoundTag compound = (CompoundTag) entryTag;
            K key = keyTransformer.deserializeNBT(compound.get("k"), null, null);
            V value = valueTransformer.deserializeNBT(compound.get("v"), null, null);
            current.put(key, value);
        }
        return current;
    }
}
