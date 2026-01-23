package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag}
 */
public abstract class ValueTransformer<T> {

    public record TransformerContext<U>(@NotNull ISyncManaged holder, @Nullable U currentValue, boolean isClientSync) { }

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

    public abstract Tag serializeNBT(T value, TransformerContext<T> context);

    public abstract T deserializeNBT(Tag tag, TransformerContext<T> context);
}
