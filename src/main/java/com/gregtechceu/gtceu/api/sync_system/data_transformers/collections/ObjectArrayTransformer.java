package com.gregtechceu.gtceu.api.sync_system.data_transformers.collections;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

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
        return parentContext.createChildContext(parentContext.type().getArrayComponentType(), elem,
                parentContext.fieldName() + "[element]");
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

    @Override
    public void writeToPacket(FriendlyByteBuf buf, @Nullable T[] value, TransformerContext<T[]> context) {
        buf.writeVarInt(value.length);
        for (T elem : value) {
            buf.writeBoolean(elem != null);
            if (elem == null) continue;
            elementTransformer.writeToPacket(buf, elem, getInnerElemContext(elem, context));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T @Nullable [] readFromPacket(FriendlyByteBuf buf, TransformerContext<T[]> context) {
        @Nullable
        T @Nullable [] current = context.currentValue();
        int length = buf.readVarInt();

        if (current == null) {
            current = (T[]) Array.newInstance((Class<T>) (context.type().getArrayComponentType().getRawType()),
                    length);
        }

        if (length != current.length) {
            current = Arrays.copyOf(current, length);
        }
        for (int i = 0; i < length; i++) {
            if (!buf.readBoolean()) {
                current[i] = null;
                continue;
            }
            T result = elementTransformer.readFromPacket(buf, getInnerElemContext(current[i], context));
            current[i] = result;
        }
        return current;
    }
}
