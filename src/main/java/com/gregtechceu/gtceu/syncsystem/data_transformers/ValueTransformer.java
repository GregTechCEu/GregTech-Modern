package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag}
 */
public abstract class ValueTransformer<T> {

    public static Tag stripLdlibWrapper(Tag t) {
        if (!(t instanceof CompoundTag tag)) return t;
        if (tag.contains("p") && tag.contains("t")) {
            return tag.getCompound("p");
        }
        if (tag.contains("t", Tag.TAG_COMPOUND)) {
            return tag.getCompound("t").getCompound("p");
        }
        return tag;
    }

    public boolean mustProvideObject() {
        return false;
    }

    public Tag serializeClientSyncNBT(@Nullable T value, ISyncManaged holder) {
        return serializeNBT(value, holder);
    }

    public T deserializeClientNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
        return deserializeNBT(tag, holder, currentVal);
    }

    public abstract Tag serializeNBT(T value, ISyncManaged holder);

    public abstract T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal);
}
