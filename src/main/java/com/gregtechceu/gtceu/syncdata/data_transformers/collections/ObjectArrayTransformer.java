package com.gregtechceu.gtceu.syncdata.data_transformers.collections;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ObjectArrayTransformer<T> implements IValueTransformer<T[]> {

    private final IValueTransformer<T> elementTransformer;

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    public ObjectArrayTransformer(IValueTransformer<T> elementTransformer) {
        this.elementTransformer = elementTransformer;
    }

    @Override
    public Tag serializeNBT(T[] value, boolean isSync, boolean isFullSync) {
        ListTag listTag = new ListTag();
        for (T element : value) {
            listTag.add(elementTransformer.serializeNBT(element, isSync, isFullSync));
        }
        return listTag;
    }

    @Override
    public T[] deserializeNBT(Tag tag, T[] currentVal, boolean isSync) {
        if (!(tag instanceof ListTag listTag)) throw new IllegalArgumentException("Expected ListTag");

        for (int i = 0; i < listTag.size(); i++) {
            if (elementTransformer.mustProvideObject())
                elementTransformer.deserializeNBT(listTag.get(i), currentVal[i], isSync);
            else currentVal[i] = elementTransformer.deserializeNBT(listTag.get(i), null, isSync);
        }
        return currentVal;
    }
}
