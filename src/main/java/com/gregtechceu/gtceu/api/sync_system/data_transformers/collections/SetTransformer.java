package com.gregtechceu.gtceu.api.sync_system.data_transformers.collections;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Set;

public class SetTransformer<T> implements ValueTransformer<Set<T>> {

    private @Nullable ValueTransformer<T> elementTransformer = null;

    @SuppressWarnings("unchecked")
    private ValueTransformer<T> getElemTransformer(ValueTransformer.TransformerContext<Set<T>> context) {
        if (elementTransformer != null) return elementTransformer;
        Type elemType = context.type().getGenericTypeArgs()[0].getRawType();
        var transformer = (ValueTransformer<T>) ValueTransformers.get(elemType);
        if (transformer == null) {
            throw new IllegalStateException("Sync: Failed to serialize set: Missing transformer for inner type: %s"
                    .formatted(elemType));
        }
        elementTransformer = transformer;
        return elementTransformer;
    }

    private ValueTransformer.TransformerContext<T> getInnerElemContext(@Nullable T elem,
                                                                       ValueTransformer.TransformerContext<Set<T>> parentContext) {
        return parentContext.createChildContext(parentContext.type().getGenericTypeArgs()[0], elem,
                parentContext.fieldName() + "[element]");
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
            T value = getElemTransformer(context).deserializeNBT(
                    TagCompatibilityFixer.stripLDLibPayloadWrapper(elementTag), getInnerElemContext(null, context));
            if (value != null) current.add(value);
        }
        return current;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, Set<T> value, TransformerContext<Set<T>> context) {
        buf.writeVarInt(value.size());
        for (T elem : value) {
            getElemTransformer(context).writeToPacket(buf, elem, getInnerElemContext(elem, context));
        }
    }

    @Override
    public @Nullable Set<T> readFromPacket(FriendlyByteBuf buf, TransformerContext<Set<T>> context) {
        var len = buf.readVarInt();
        var current = context.currentValue();

        if (current != null) current.clear();
        else current = new ObjectOpenHashSet<>(len);

        for (int i = 0; i < len; i++) {
            T val = getElemTransformer(context).readFromPacket(buf, getInnerElemContext(null, context));
            if (val != null) current.add(val);
        }
        return current;
    }
}
