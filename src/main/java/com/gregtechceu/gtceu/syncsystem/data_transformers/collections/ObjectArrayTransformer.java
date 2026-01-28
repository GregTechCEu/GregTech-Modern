package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ObjectArrayTransformer<T> implements ValueTransformer<T[]> {

    private final ValueTransformer<T> elementTransformer;

    public ObjectArrayTransformer(ValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<T[]> parentContext) {
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
    public Tag serializeNBT(T[] value, ValueTransformer.TransformerContext<T[]> context) {
        ListTag listTag = new ListTag();
        for (T element : value) {
            listTag.add(elementTransformer.serializeNBT(element, getInnerElemContext(element, context)));
        }
        return listTag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T @Nullable [] deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T[]> context) {
        T[] current = context.currentValue();
        ListTag listTag = ValueTransformer.assertTagType(ListTag.class, tag, context);

        if (current == null) {
            current = (T[]) Array.newInstance((Class<T>) (context.genericArgs()[0]), listTag.size());
        }

        if (listTag.size() != current.length) {
            current = Arrays.copyOf(current, listTag.size());
        }
        for (int i = 0; i < listTag.size(); i++) {
            var currentV = current[i];
            T result = elementTransformer.deserializeNBT(ValueTransformer.stripLdlibWrapper(listTag.get(i)),
                    getInnerElemContext(null, context));
            if (result == null) return current;
            if (result != currentV) current[i] = result;
        }
        return current;
    }
}
