package com.gregtechceu.gtceu.syncsystem.data_transformers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnumTransformer<E extends Enum<E>> extends ValueTransformer<E> {

    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    public EnumTransformer(Class<? extends Enum<?>> enumClass) {
        this.enumClass = (Class<E>) enumClass;
    }

    @Override
    public Tag serializeNBT(E value, ValueTransformer.TransformerContext<E> context) {
        return StringTag.valueOf(value.name());
    }

    @Override
    public E deserializeNBT(Tag tag, ValueTransformer.TransformerContext<E> context) {
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
