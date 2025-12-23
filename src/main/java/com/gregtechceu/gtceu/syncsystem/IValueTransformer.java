package com.gregtechceu.gtceu.syncsystem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag}
 */
public interface IValueTransformer<T> {

    static Tag stripLdlibWrapper(Tag t) {
        if (!(t instanceof CompoundTag tag)) return t;
        if (tag.contains("p") && tag.contains("t")) {
            return tag.getCompound("p");
        }
        if (tag.contains("t", Tag.TAG_COMPOUND)) {
            return tag.getCompound("t").getCompound("p");
        }
        return tag;
    }

    default boolean mustProvideObject() {
        return false;
    }

    default Tag serializeClientSyncNBT(@Nullable T value, ISyncManaged holder) {
        return serializeNBT(value, holder);
    }

    default T deserializeClientNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
        return deserializeNBT(tag, holder, currentVal);
    }

    Tag serializeNBT(T value, ISyncManaged holder);

    T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal);
}
