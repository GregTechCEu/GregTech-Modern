package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListTransformer<T> implements ValueTransformer<List<T>> {

    private @Nullable ValueTransformer<T> elementTransformer = null;

    @SuppressWarnings("unchecked")
    private ValueTransformer<T> getElemTransformer(ValueTransformer.TransformerContext<List<T>> context) {
        if (elementTransformer != null) return elementTransformer;
        var transformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize list: Missing transformer for inner type: %s"
                    .formatted(context.genericArgs()[0]));
        }
        elementTransformer = transformer;
        return elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<List<T>> parentContext) {
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
    public Tag serializeNBT(List<T> value, ValueTransformer.TransformerContext<List<T>> context) {
        ListTag list = new ListTag();
        for (var obj : value) {
            list.add(getElemTransformer(context).serializeNBT(obj, getInnerElemContext(obj, context)));
        }
        return list;
    }

    @Override
    public @Nullable List<T> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<List<T>> context) {
        var current = context.currentValue();
        ListTag listTag = ValueTransformer.assertTagType(ListTag.class, tag, context);
        if (current != null) current.clear();
        else current = new ArrayList<>();
        List<T> finalCurrent = current;
        for (var t : listTag) {
            T val = getElemTransformer(context).deserializeNBT(ValueTransformer.stripLdlibWrapper(t),
                    getInnerElemContext(null, context));
            if (val != null) finalCurrent.add(val);
        }
        return current;
    }
}
