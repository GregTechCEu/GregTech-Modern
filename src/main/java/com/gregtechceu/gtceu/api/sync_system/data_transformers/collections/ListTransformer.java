package com.gregtechceu.gtceu.api.sync_system.data_transformers.collections;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListTransformer<T> implements ValueTransformer<List<T>> {

    private @Nullable ValueTransformer<T> elementTransformer = null;

    @SuppressWarnings("unchecked")
    private ValueTransformer<T> getElemTransformer(ValueTransformer.TransformerContext<List<T>> context) {
        if (elementTransformer != null) return elementTransformer;
        var innerType = context.type().getGenericTypeArgs()[0].getRawType();
        var transformer = (ValueTransformer<T>) ValueTransformers.get(innerType);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize list: Missing transformer for inner type: %s"
                    .formatted(innerType));
        }
        elementTransformer = transformer;
        return elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<List<T>> parentContext) {
        return parentContext.createChildContext(parentContext.type().getGenericTypeArgs()[0], elem,
                parentContext.fieldName() + "[element]");
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
        else current = new ArrayList<>(listTag.size());
        for (var t : listTag) {
            T val = getElemTransformer(context).deserializeNBT(TagCompatibilityFixer.stripLDLibPayloadWrapper(t),
                    getInnerElemContext(null, context));
            if (val != null) current.add(val);
        }
        return current;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, List<T> value, TransformerContext<List<T>> context) {
        buf.writeVarInt(value.size());
        for (T elem : value) {
            getElemTransformer(context).writeToPacket(buf, elem, getInnerElemContext(elem, context));
        }
    }

    @Override
    public @Nullable List<T> readFromPacket(FriendlyByteBuf buf, TransformerContext<List<T>> context) {
        var len = buf.readVarInt();
        var current = context.currentValue();

        if (current != null) current.clear();
        else current = new ArrayList<>(len);

        for (int i = 0; i < len; i++) {
            T val = getElemTransformer(context).readFromPacket(buf, getInnerElemContext(null, context));
            if (val != null) current.add(val);
        }
        return current;
    }
}
