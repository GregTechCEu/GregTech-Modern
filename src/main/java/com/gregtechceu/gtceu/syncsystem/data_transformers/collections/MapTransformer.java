package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class MapTransformer<K, V> extends ValueTransformer<Map<K, V>> {

    private final ValueTransformer<K> keyTransformer;
    private final ValueTransformer<V> valueTransformer;

    public MapTransformer(ValueTransformer<K> keyTransformer, ValueTransformer<V> valueTransformer) {
        this.keyTransformer = keyTransformer;
        this.valueTransformer = valueTransformer;
    }

    @Override
    public Tag serializeNBT(Map<K, V> value, ValueTransformer.TransformerContext<Map<K, V>> context) {
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
    public Map<K, V> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Map<K, V>> context) {
        var current = context.currentValue();
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return current;
        }
        if (current != null) current.clear();
        else current = new HashMap<>();
        for (Tag entryTag : listTag) {
            CompoundTag compound = (CompoundTag) entryTag;
            K key = keyTransformer.deserializeNBT(compound.get("k"), null);
            V value = valueTransformer.deserializeNBT(compound.get("v"), null);
            current.put(key, value);
        }
        return current;
    }
}
