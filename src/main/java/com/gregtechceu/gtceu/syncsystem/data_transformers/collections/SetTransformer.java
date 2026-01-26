package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Set;

public class SetTransformer<T> implements ValueTransformer<Set<T>> {

    private @Nullable ValueTransformer<T> elementTransformer = null;

    @SuppressWarnings("unchecked")
    private ValueTransformer<T> getElemTransformer(ValueTransformer.TransformerContext<Set<T>> context) {
        if (elementTransformer != null) return elementTransformer;
        var transformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialise set: Missing transformer for inner type: %s"
                    .formatted(context.genericArgs()[0]));
        }
        elementTransformer = transformer;
        return elementTransformer;
    }

    @Override
    public Tag serializeNBT(Set<T> value, ValueTransformer.TransformerContext<Set<T>> context) {
        ListTag tag = new ListTag();
        for (T element : value) {
            tag.add(getElemTransformer(context).serializeNBT(element, new TransformerContext<>(context.holder(),
                    element.getClass(), new Type[0], element, context.isClientSync())));
        }
        return tag;
    }

    @Override
    public Set<T> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Set<T>> context) {
        var current = context.currentValue();
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return Set.of();
        }
        if (current != null) current.clear();
        else current = new ObjectOpenHashSet<>();
        for (Tag elementTag : listTag) {
            T value = getElemTransformer(context).deserializeNBT(elementTag, new TransformerContext<>(context.holder(),
                    current.getClass(), new Type[0], null, context.isClientSync()));
            if (value != null) current.add(value);
        }
        return current;
    }
}
