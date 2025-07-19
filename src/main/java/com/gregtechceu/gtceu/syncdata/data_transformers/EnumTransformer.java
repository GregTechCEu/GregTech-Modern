package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class EnumTransformer<E extends Enum<E>> implements IValueTransformer<E> {

    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    public EnumTransformer(Class<? extends Enum<?>> enumClass) {
        this.enumClass = (Class<E>) enumClass;
    }

    @Override
    public Tag serializeNBT(E value, boolean isSync, boolean isFullSync) {
        return StringTag.valueOf(value.name());
    }

    @Override
    public E deserializeNBT(Tag tag, E currentVal, boolean isSync) {
        return Enum.valueOf(enumClass, tag.getAsString());
    }
}
