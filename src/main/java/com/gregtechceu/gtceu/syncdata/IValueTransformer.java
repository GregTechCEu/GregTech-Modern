package com.gregtechceu.gtceu.syncdata;

import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag}
 */
public interface IValueTransformer<T> {

    default boolean mustProvideObject() {
        return false;
    }

    Tag serializeNBT(T value, boolean isSync, boolean isFullSync);

    T deserializeNBT(Tag tag, @Nullable T currentVal, boolean isSync);
}
