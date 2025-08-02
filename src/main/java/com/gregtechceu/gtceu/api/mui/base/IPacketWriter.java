package com.gregtechceu.gtceu.api.mui.base;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A function that can write any data to an {@link FriendlyByteBuf}.
 */
public interface IPacketWriter {

    /**
     * Writes any data to a packet buffer
     *
     * @param buffer buffer to write to
     */
    void write(FriendlyByteBuf buffer);
}
