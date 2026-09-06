package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.api.sync_system.TypeDeclaration;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that provides a set of methods for encoding/decoding a value of type {@code <T>} into a
 * {@link Tag} and {@link FriendlyByteBuf}
 * 
 * @param <T> The type which this transformer can encode
 */
public interface ValueTransformer<T> {

    /**
     * A record holding information about the context from which this value transformer is currently being invoked.
     *
     * @param holder       The object which holds the specific field being serialized by this transformer.
     * @param type         An object describing the type of the field currently being serialized/deserialized.
     * @param currentValue The current value (if any) of the field currently being serialized/deserialized.
     * @param fieldName    The name of the field being serialized, or a string denoting the current sync context if not
     *                     being invoked directly on a field.
     * @param lookup       The current registry lookup context.
     *
     */
    record TransformerContext<U>(Object holder, TypeDeclaration type,
                                 @Nullable U currentValue, @Nullable String fieldName,
                                 boolean isClientFullSyncUpdate, HolderLookup.Provider lookup,
                                 RegistryOps<Tag> nbtOps) {

        public TransformerContext(Object holder, TypeDeclaration type,
                                  @Nullable U currentValue, @Nullable String fieldName, boolean isClientSync,
                                  boolean isClientFullSyncUpdate, HolderLookup.Provider lookup) {
            this(holder, type, currentValue, fieldName, isClientFullSyncUpdate, lookup,
                    RegistryOps.create(NbtOps.INSTANCE, lookup));
        }

        @SuppressWarnings("NullableProblems")
        public <V> TransformerContext<V> createChildContext(TypeDeclaration childType,
                                                            @Nullable V childValue, @Nullable String childFieldName) {
            return new TransformerContext<>(holder, childType, childValue, childFieldName,
                    isClientFullSyncUpdate, lookup, nbtOps);
        }
    }

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
     * Serializes this value into a tag, based on the current value and provided transformer context.
     */
    Tag serializeNBT(T value, TransformerContext<T> context);

    /**
     * Deserializes this value, based on the stored tag and provided transformer context.
     */
    @Nullable
    T deserializeNBT(Tag tag, TransformerContext<T> context);

    /**
     * Writes this value to a {@link FriendlyByteBuf} to be sent to the client.
     */
    void writeToPacket(FriendlyByteBuf buf, T value, TransformerContext<T> context);

    /**
     * Reads this value from a {@link FriendlyByteBuf}.
     */
    @Nullable
    T readFromPacket(FriendlyByteBuf buf, TransformerContext<T> context);
}
