package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;

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
    public Tag serializeNBT(T[] value, ISyncManaged holder) {
        ListTag listTag = new ListTag();
        for (T element : value) {
            listTag.add(elementTransformer.serializeNBT(element, null));
        }
        return listTag;
    }

    @Override
    public T[] deserializeNBT(Tag tag, ISyncManaged holder, T[] current) {
        if (!(tag instanceof ListTag listTag)) {
            GTCEu.LOGGER.error("Tag is of type {}, not ListTag", tag.getType());
            return current;
        }

        if (listTag.size() != current.length) {
            current = Arrays.copyOf(current, listTag.size());
        }
        for (int i = 0; i < listTag.size(); i++) {
            if (elementTransformer.mustProvideObject()) {
                elementTransformer.deserializeNBT(IValueTransformer.stripLdlibWrapper(listTag.get(i)), null,
                        current[i]);
            } else {
                current[i] = elementTransformer.deserializeNBT(IValueTransformer.stripLdlibWrapper(listTag.get(i)),
                        null, null);
            }
        }
        return current;
    }
}
