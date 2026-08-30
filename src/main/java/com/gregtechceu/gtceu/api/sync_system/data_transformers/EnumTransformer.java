package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

public class EnumTransformer<E extends Enum<E>> implements ValueTransformer<E> {

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
        var enumString = ValueTransformer.assertTagType(StringTag.class, tag, context).getAsString();
        E value = null;
        try {
            value = Enum.valueOf(enumClass, enumString);
        } catch (IllegalArgumentException e) {
            for (E val : enumClass.getEnumConstants()) {
                if (val.name().toLowerCase().equals(enumString)) value = val;
            }
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Unknown enum constant: %s[%s]".formatted(enumClass.getName(), enumString));
        }
        return value;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, E value, TransformerContext<E> context) {
        buf.writeEnum(value);
    }

    @Override
    public @Nullable E readFromPacket(FriendlyByteBuf buf, TransformerContext<E> context) {
        return buf.readEnum(enumClass);
    }
}
