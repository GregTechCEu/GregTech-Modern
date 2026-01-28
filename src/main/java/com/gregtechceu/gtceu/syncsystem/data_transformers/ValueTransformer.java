package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag}
 * 
 * @param <T> The type which this transformer can encode
 */
public interface ValueTransformer<T> {

    /**
     * A record holding information about the context from which this value transformer is currently being invoked.
     * 
     * @param holder       The sync object which holds the specific field being serialized by this transformer.
     * @param clazz        The actual type of the field currently being serialized/deserialized. (This may be a subtype
     *                     of the type which this transformer is registered for.)
     * @param genericArgs  The values of the generic arguments which this field has been declared with, or an empty
     *                     array if the type is not generic.
     * @param currentValue The current value (if any) of the field currently being serialized/deserialized.
     * @param fieldName    The name of the field being serialized, or a string denoting the current sync context if not
     *                     being invoked directly on a field.
     * @param isClientSync Whether NBT is currently being generated as part of a sync update to the client, not as NBT
     *                     being
     *                     written to the server save.
     */
    record TransformerContext<U>(@NotNull ISyncManaged holder, @NotNull Class<?> clazz,
                                 @NotNull Type @NotNull [] genericArgs,
                                 @Nullable U currentValue, @Nullable String fieldName, boolean isClientSync) {}

    /**
     * Casts a given NBT tag to a specific tag type, throwing an error if the tag cannot be casted.
     */
    @SuppressWarnings("unchecked")
    static <TagType extends Tag> TagType assertTagType(Class<TagType> cls, Tag tag, TransformerContext<?> ctx) {
        try {
            return (TagType) (tag);
        } catch (ClassCastException c) {
            throw new ClassCastException("Sync: Invalid tag type: expected %s, got %s [%s, field %s]"
                    .formatted(cls.toString(), tag.getClass().getName(), ctx.holder(), ctx.fieldName));
        }
    }

    /**
     * Extracts the actual data tag from an LDLib tag structure which is present in some serialized objects.
     */
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

    /**
     * A method which serializes this value into a tag, based on the current value and provided transformer context.
     */
    Tag serializeNBT(T value, TransformerContext<T> context);

    /**
     * A method which deserializes this value, based on the stored tag and provided transformer context.
     */
    @Nullable
    T deserializeNBT(Tag tag, TransformerContext<T> context);
}
