package com.gregtechceu.gtceu.utils.serialization.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A function that writes an object to a {@link FriendlyByteBuf}.
 *
 * @param <T> object type
 */
public interface IByteBufSerializer<T> {

    /**
     * Writes the object to the buffer.
     *
     * @param buffer buffer to write to
     * @param value  object to write
     */
    void serialize(FriendlyByteBuf buffer, T value);
}
