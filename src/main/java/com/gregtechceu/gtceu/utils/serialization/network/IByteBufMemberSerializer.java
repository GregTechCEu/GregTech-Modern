package com.gregtechceu.gtceu.utils.serialization.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A function that writes an object to a {@link FriendlyByteBuf}.
 *
 * @param <T> object type
 */
public interface IByteBufMemberSerializer<T> {

    /**
     * Writes the object to the buffer.
     *
     * @param value  object to write
     * @param buffer buffer to write to
     */
    void serialize(T value, FriendlyByteBuf buffer);

    default IByteBufSerializer<T> asBasic() {
        return (buffer, value) -> this.serialize(value, buffer);
    }
}
