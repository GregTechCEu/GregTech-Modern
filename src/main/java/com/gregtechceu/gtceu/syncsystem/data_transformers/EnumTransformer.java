package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class EnumTransformer<E extends Enum<E>> implements IValueTransformer<E> {

    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    public EnumTransformer(Class<? extends Enum<?>> enumClass) {
        this.enumClass = (Class<E>) enumClass;
    }

    @Override
    public Tag serializeNBT(E value, ISyncManaged holder) {
        return StringTag.valueOf(value.name());
    }

    @Override
    public E deserializeNBT(Tag tag, ISyncManaged holder, @Nullable E currentVal) {
        E value = null;
        try {
            value = Enum.valueOf(enumClass, tag.getAsString());
        } catch (IllegalArgumentException e) {
            for (E val : enumClass.getEnumConstants()) {
                if (val.name().toLowerCase().equals(tag.getAsString())) value = val;
            }
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Unknown enum constant: %s[%s]".formatted(enumClass.getCanonicalName(), tag.getAsString()));
        }
        return value;
    }
}
