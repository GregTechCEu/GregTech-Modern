package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public class SetTransformer<T> implements ValueTransformer<Set<T>> {

    private @Nullable ValueTransformer<T> elementTransformer = null;

    @SuppressWarnings("unchecked")
    private ValueTransformer<T> getElemTransformer(ValueTransformer.TransformerContext<Set<T>> context) {
        if (elementTransformer != null) return elementTransformer;
        var transformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize set: Missing transformer for inner type: %s"
                    .formatted(context.genericArgs()[0]));
        }
        elementTransformer = transformer;
        return elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<Set<T>> parentContext) {
        Type[] generics;
        Class<?> clazz;
        if (parentContext.genericArgs()[0] instanceof ParameterizedType parameterizedType) {
            generics = parameterizedType.getActualTypeArguments();
            clazz = (Class<?>) parameterizedType.getRawType();
        } else {
            generics = new Type[0];
            clazz = (Class<?>) parentContext.genericArgs()[0];
        }
        if (elem != null) clazz = elem.getClass();
        return new TransformerContext<>(parentContext.holder(),
                clazz, generics, elem, parentContext.fieldName() + "[element]",
                parentContext.isClientSync());
    }

    @Override
    public Tag serializeNBT(Set<T> value, ValueTransformer.TransformerContext<Set<T>> context) {
        ListTag tag = new ListTag();
        for (T element : value) {
            tag.add(getElemTransformer(context).serializeNBT(element, getInnerElemContext(element, context)));
        }
        return tag;
    }

    @Override
    public Set<T> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Set<T>> context) {
        ListTag listTag = ValueTransformer.assertTagType(ListTag.class, tag, context);
        var current = context.currentValue();
        if (current != null) current.clear();
        else current = new ObjectOpenHashSet<>();
        for (Tag elementTag : listTag) {
            T value = getElemTransformer(context).deserializeNBT(elementTag, getInnerElemContext(null, context));
            if (value != null) current.add(value);
        }
        return current;
    }
}
