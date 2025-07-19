package com.gregtechceu.gtceu.syncdata.data_transformers.collections;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public class ListTransformer<T> implements IValueTransformer<List<T>> {

    private final IValueTransformer<T> elementTransformer;

    public ListTransformer(IValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    @Override
    public Tag serializeNBT(List<T> value, boolean isSync, boolean isFullSync) {
        ListTag list = new ListTag();
        for (var obj : value) {
            list.add(elementTransformer.serializeNBT(obj, isSync, isFullSync));
        }
        return list;
    }

    @Override
    public List<T> deserializeNBT(Tag tag, List<T> current, boolean isSync) {
        if (!(tag instanceof ListTag listTag)) return List.of();
        return listTag.stream().map((t) -> elementTransformer.deserializeNBT(t, null, isSync)).toList();
    }
}
