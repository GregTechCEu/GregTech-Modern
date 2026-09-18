package brachy.modularui.api;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.ByteBuf;

/**
 * A function that can write any data to an {@link FriendlyByteBuf}.
 */
public interface IPacketWriter<B extends ByteBuf> {

    /**
     * Writes any data to a packet buffer
     *
     * @param buffer buffer to write to
     */
    void write(B buffer);
}
