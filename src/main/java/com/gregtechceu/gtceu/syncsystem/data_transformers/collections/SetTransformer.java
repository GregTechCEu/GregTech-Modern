package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

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
    public Tag serializeNBT(Set<T> value, ISyncManaged holder) {
        ListTag tag = new ListTag();
        for (T element : value) {
            tag.add(elementTransformer.serializeNBT(element, null));
        }
        return tag;
    }

    @Override
    public Set<T> deserializeNBT(Tag tag, ISyncManaged holder, Set<T> currentVal) {
        if (!(tag instanceof ListTag listTag)) return Set.of();
        Set<T> set = new HashSet<>();
        for (Tag elementTag : listTag) {
            set.add(elementTransformer.deserializeNBT(elementTag, null, null));
        }
        return set;
    }
}
