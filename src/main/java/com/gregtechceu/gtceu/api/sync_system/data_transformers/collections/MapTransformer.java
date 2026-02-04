package com.gregtechceu.gtceu.api.sync_system.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MapTransformer<K, V> implements ValueTransformer<Map<K, V>> {

    private @Nullable ValueTransformer<K> keyTransformer;
    private @Nullable ValueTransformer<V> valueTransformer;

    @SuppressWarnings("unchecked")
    private ValueTransformer<K> getKeyTransformer(ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (keyTransformer != null) return keyTransformer;
        var innerType = context.type().getGenericTypeArgs()[0].getRawType();
        var transformer = (ValueTransformer<K>) ValueTransformers.get(innerType);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize map: Missing transformer for key type: %s"
                    .formatted(innerType));
        }
        keyTransformer = transformer;
        return keyTransformer;
    }

    @SuppressWarnings("unchecked")
    private ValueTransformer<V> getValueTransformer(ValueTransformer.TransformerContext<Map<K, V>> context) {
        if (valueTransformer != null) return valueTransformer;
        var innerType = context.type().getGenericTypeArgs()[1].getRawType();
        var transformer = (ValueTransformer<V>) ValueTransformers.get(innerType);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize map: Missing transformer for value type: %s"
                    .formatted(innerType));
        }
        valueTransformer = transformer;
        return valueTransformer;
    }

    private ValueTransformer.TransformerContext<K> getInnerKeyContext(@Nullable K key,
                                                                      ValueTransformer.TransformerContext<Map<K, V>> parentContext) {
        return new TransformerContext<>(parentContext.holder(),
                parentContext.type().getGenericTypeArgs()[0], key, parentContext.fieldName() + "[key]",
                parentContext.isClientSync());
    }

    private ValueTransformer.TransformerContext<V> getInnerValueContext(@Nullable V value,
                                                                        ValueTransformer.TransformerContext<Map<K, V>> parentContext) {
        return new TransformerContext<>(parentContext.holder(),
                parentContext.type().getGenericTypeArgs()[1], value,
                parentContext.fieldName() + "[value]",
                parentContext.isClientSync());
    }

    @Override
    public Tag serializeNBT(Map<K, V> value, ValueTransformer.TransformerContext<Map<K, V>> context) {
        ListTag entries = new ListTag();
        for (var entry : value.entrySet()) {
            CompoundTag compound = new CompoundTag();
            compound.put("k",
                    getKeyTransformer(context).serializeNBT(entry.getKey(),
                            getInnerKeyContext(entry.getKey(), context)));
            compound.put("v",
                    getValueTransformer(context).serializeNBT(entry.getValue(),
                            getInnerValueContext(entry.getValue(), context)));
            entries.add(compound);
        }
        return entries;
    }

    @Override
    public Map<K, V> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Map<K, V>> context) {
        var current = context.currentValue();
        ListTag listTag = ValueTransformer.assertTagType(ListTag.class, tag, context);
        if (current != null) current.clear();
        else current = new Object2ObjectOpenHashMap<>();
        for (Tag entryTag : listTag) {
            CompoundTag compound = (CompoundTag) entryTag;

            Tag keyTag = compound.get("k");
            Tag valueTag = compound.get("v");
            if (keyTag == null || valueTag == null) continue;

            K key = getKeyTransformer(context).deserializeNBT(keyTag, getInnerKeyContext(null, context));
            V value = getValueTransformer(context).deserializeNBT(valueTag, getInnerValueContext(null, context));
            if (key == null || value == null) {
                GTCEu.LOGGER.warn(
                        "Sync: Skipping null key or field while deserializing map: [key: {}, value: {}] [nbt key: {}, nbt value: {}]",
                        key, value, keyTag, valueTag);
                continue;
            } ;
            current.put(key, value);
        }
        return current;
    }
}
