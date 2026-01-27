package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public class MapTransformer<K, V> implements ValueTransformer<Map<K, V>> {

    private @Nullable ValueTransformer<K> keyTransformer;
    private @Nullable ValueTransformer<V> valueTransformer;

    @SuppressWarnings("unchecked")
    private ValueTransformer<K> getKeyTransformer(ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (keyTransformer != null) return keyTransformer;
        var transformer = (ValueTransformer<K>) ValueTransformers.get(context.genericArgs()[0]);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialise map: Missing transformer for key type: %s"
                    .formatted(context.genericArgs()[0]));
        }
        keyTransformer = transformer;
        return keyTransformer;
    }

    @SuppressWarnings("unchecked")
    private ValueTransformer<V> getValueTransformer(ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (valueTransformer != null) return valueTransformer;
        var transformer = (ValueTransformer<V>) ValueTransformers.get(context.genericArgs()[0]);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialise map: Missing transformer for value type: %s"
                    .formatted(context.genericArgs()[1]));
        }
        valueTransformer = transformer;
        return valueTransformer;
    }

    @Override
    public Tag serializeNBT(Map<K, V> value, ValueTransformer.TransformerContext<Map<K, V>> context) {
        ListTag entries = new ListTag();
        for (var entry : value.entrySet()) {

            CompoundTag compound = new CompoundTag();
            compound.put("k",
                    getKeyTransformer(context).serializeNBT(entry.getKey(), new TransformerContext<>(context.holder(),
                            entry.getKey().getClass(), new Type[0], entry.getKey(), context.fieldName() + "[key]", context.isClientSync())));
            compound.put("v",
                    getValueTransformer(context).serializeNBT(entry.getValue(),
                            new TransformerContext<>(context.holder(),
                                    entry.getValue().getClass(), new Type[0], entry.getValue(), context.fieldName() + "[value]",
                                    context.isClientSync())));
            entries.add(compound);
        }
        return entries;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Map<K, V>> context) {
        var current = context.currentValue();
        ListTag listTag = ValueTransformer.assertTagType(ListTag.class, tag, context);
        if (current != null) current.clear();
        else current = new Object2ObjectOpenHashMap<>();
        for (Tag entryTag : listTag) {
            CompoundTag compound = (CompoundTag) entryTag;
            var ctx = new TransformerContext<>(
                    context.holder(), current.getClass(), new Type[0], null, context.fieldName(), context.isClientSync());

            Tag keyTag = compound.get("k");
            Tag valueTag = compound.get("v");
            if (keyTag == null || valueTag == null) continue;

            K key = getKeyTransformer(context).deserializeNBT(keyTag, (TransformerContext<K>) ctx);
            V value = getValueTransformer(context).deserializeNBT(valueTag, (TransformerContext<V>) ctx);
            if (key == null || value == null) continue;

            current.put(key, value);
        }
        return current;
    }
}
