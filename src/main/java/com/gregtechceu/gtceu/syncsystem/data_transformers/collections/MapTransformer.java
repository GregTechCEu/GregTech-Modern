package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MapTransformer<K, V> extends ValueTransformer<Map<K, V>> {

    private ValueTransformer<K> keyTransformer;
    private ValueTransformer<V> valueTransformer;

    @Override
    @SuppressWarnings("unchecked")
    public Tag serializeNBT(Map<K, V> value, ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (keyTransformer == null) keyTransformer = (ValueTransformer<K>) ValueTransformers.get(context.genericArgs()[0]);
        if (valueTransformer == null) valueTransformer = (ValueTransformer<V>) ValueTransformers.get(context.genericArgs()[1]);

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
    @SuppressWarnings("unchecked")
    public Map<K, V> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (keyTransformer == null) keyTransformer = (ValueTransformer<K>) ValueTransformers.get(context.genericArgs()[0]);
        if (valueTransformer == null) valueTransformer = (ValueTransformer<V>) ValueTransformers.get(context.genericArgs()[1]);

        var current = context.currentValue();
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return Map.of();
        }
        if (current != null) current.clear();
        else current = new HashMap<>();
        for (Tag entryTag : listTag) {
            CompoundTag compound = (CompoundTag) entryTag;
            K key = keyTransformer.deserializeNBT(Objects.requireNonNull(compound.get("k")), null);
            V value = valueTransformer.deserializeNBT(Objects.requireNonNull(compound.get("v")), null);
            current.put(key, value);
        }
        return current;
    }
}
