package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformers;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public Tag serializeNBT(List<T> value, ValueTransformer.TransformerContext<List<T>> context) {
        ListTag list = new ListTag();
        for (var obj : value) {
            list.add(getElemTransformer(context).serializeNBT(obj,
                    new TransformerContext<>(context.holder(), obj.getClass(),
                            new Type[0], obj, context.fieldName() + "[element]", context.isClientSync())));
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
                    new TransformerContext<>(
                            context.holder(), finalCurrent.getClass(), new Type[0], null,
                            context.fieldName() + "[element]",
                            context.isClientSync()));
            if (val != null) finalCurrent.add(val);
        }
        return current;
    }
}
