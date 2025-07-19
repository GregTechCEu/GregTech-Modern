package com.gregtechceu.gtceu.syncdata.data_transformers.collections;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class SetTransformer<T> implements IValueTransformer<Set<T>> {

    private final IValueTransformer<T> elementTransformer;

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    public SetTransformer(IValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    @Override
    public Tag serializeNBT(Set<T> value, boolean isSync, boolean isFullSync) {
        ListTag tag = new ListTag();
        for (T element : value) {
            tag.add(elementTransformer.serializeNBT(element, isSync, isFullSync));
        }
        return tag;
    }

    @Override
    public Set<T> deserializeNBT(Tag tag, Set<T> currentVal, boolean isSync) {
        if (!(tag instanceof ListTag listTag)) return Set.of();
        Set<T> set = new HashSet<>();
        for (Tag elementTag : listTag) {
            set.add(elementTransformer.deserializeNBT(elementTag, null, isSync));
        }
        return set;
    }
}
