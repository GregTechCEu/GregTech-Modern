package com.gregtechceu.gtceu.api.sync_system.data_transformers.collections;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ObjectArrayTransformer<T> implements ValueTransformer<T[]> {

    private final ValueTransformer<T> elementTransformer;

    public ObjectArrayTransformer(ValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<T[]> parentContext) {
        return new TransformerContext<>(parentContext.holder(),
                parentContext.type().getArrayComponentType(), elem, parentContext.fieldName() + "[element]",
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
            current = (T[]) Array.newInstance((Class<T>) (context.type().getArrayComponentType().getRawType()),
                    listTag.size());
        }

        if (listTag.size() != current.length) {
            current = Arrays.copyOf(current, listTag.size());
        }
        for (int i = 0; i < listTag.size(); i++) {
            T result = elementTransformer.deserializeNBT(TagCompatibilityFixer.stripLDLibPayloadWrapper(listTag.get(i)),
                    getInnerElemContext(current[i], context));
            if (result == null) return current;
            current[i] = result;
        }
        return current;
    }
}
