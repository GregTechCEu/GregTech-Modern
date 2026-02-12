package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;
import com.gregtechceu.gtceu.api.sync_system.TypeDeclaration;

import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param type         An object describing the type of the field currently being serialized/deserialized.
     * @param currentValue The current value (if any) of the field currently being serialized/deserialized.
     * @param fieldName    The name of the field being serialized, or a string denoting the current sync context if not
     *                     being invoked directly on a field.
     * @param isClientSync Whether NBT is currently being generated as part of a sync update to the client, not as NBT
     *                     being
     *                     written to the server save.
     */
    record TransformerContext<U>(@NotNull ISyncManaged holder, @NotNull TypeDeclaration type,
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
     * A method which serializes this value into a tag, based on the current value and provided transformer context.
     */
    Tag serializeNBT(T value, TransformerContext<T> context);

    /**
     * A method which deserializes this value, based on the stored tag and provided transformer context.
     */
    @Nullable
    T deserializeNBT(Tag tag, TransformerContext<T> context);
}
