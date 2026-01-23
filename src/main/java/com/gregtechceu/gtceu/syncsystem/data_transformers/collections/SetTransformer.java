package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetTransformer<T> extends ValueTransformer<Set<T>> {

    private ValueTransformer<T> elementTransformer = null;

    @Override
    @SuppressWarnings("unchecked")
    public Tag serializeNBT(Set<T> value, ValueTransformer.TransformerContext<Set<T>> context) {
        if (elementTransformer == null) elementTransformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);

        ListTag tag = new ListTag();
        for (T element : value) {
            tag.add(elementTransformer.serializeNBT(element, null));
        }
        return tag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<Set<T>> context) {
        if (elementTransformer == null) elementTransformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);

        var current = context.currentValue();
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return Set.of();
        }
        if (current != null) current.clear();
        else current = new HashSet<>();
        for (Tag elementTag : listTag) {
            current.add(elementTransformer.deserializeNBT(elementTag, null));
        }
        return current;
    }
}
