package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ListTransformer<T> implements ValueTransformer<List<T>> {

    private ValueTransformer<T> elementTransformer = null;

    @Override
    @SuppressWarnings("unchecked")
    public Tag serializeNBT(List<T> value, ValueTransformer.TransformerContext<List<T>> context) {
        if (elementTransformer == null)
            elementTransformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);

        ListTag list = new ListTag();
        for (var obj : value) {
            list.add(elementTransformer.serializeNBT(obj, null));
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<List<T>> context) {
        if (elementTransformer == null)
            elementTransformer = (ValueTransformer<T>) ValueTransformers.get(context.genericArgs()[0]);

        var current = context.currentValue();
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return current;
        }
        if (current != null) current.clear();
        else current = new ArrayList<>();
        List<T> finalCurrent = current;
        listTag.forEach(t -> finalCurrent
                .add(elementTransformer.deserializeNBT(ValueTransformer.stripLdlibWrapper(t), null)));

        return current;
    }
}
