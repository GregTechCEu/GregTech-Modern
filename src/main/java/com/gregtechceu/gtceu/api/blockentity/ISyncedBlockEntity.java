package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Functions which sync data between the Server and Client sides in a TileEntity.
 */
public interface ISyncedBlockEntity {

    /**
     * Used to sync data from Server -> Client.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data is received in {@link #receiveInitialSyncData(FriendlyByteBuf)}.
     * <p>
     * Typically used to send server side data to the client on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link BlockEntity#getUpdateTag()}.
     *
     * @param buf the buffer to write data to
     */
    default void writeInitialSyncData(@NotNull FriendlyByteBuf buf) {}

    /**
     * Used to receive Server -> Client sync data.
     * Called during initial loading of the chunk or when many blocks change at once.
     * <p>
     * Data sent is from {@link #writeInitialSyncData(FriendlyByteBuf)}.
     * <p>
     * Typically used to receive server side data on initial chunk loading.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Client-Side</strong>.
     * <p>
     * Equivalent to {@link net.minecraft.world.level.block.entity.BlockEntity#handleUpdateTag(CompoundTag)}.
     *
     * @param buf the buffer to read data from
     */
    default void receiveInitialSyncData(@NotNull FriendlyByteBuf buf) {}

    /**
     * Used to send an anonymous Server -> Client packet.
     * Call to build up the packet to send to the client when it is re-synced.
     * <p>
     * Data is received in {@link #receiveCustomData(int, FriendlyByteBuf)};
     * <p>
     * Typically used to signal to the client that a rendering update is needed
     * when sending a server-side state update.
     * <p>
     * <em>Should be called manually</em>.
     * <p>
     * This method is called <strong>Server-Side</strong>.
     * <p>
     * Equivalent to {@link BlockEntity#getUpdatePacket()}
     *
     * @param discriminator the discriminator determining the packet sent.
     * @param dataWriter    a consumer which writes packet data to a buffer.
     * @see gregtech.api.capability.GregtechDataCodes
     */
    default void writeCustomData(int discriminator, @NotNull Consumer<@NotNull FriendlyByteBuf> dataWriter) {}

    /**
     * Used to receive an anonymous Server -> Client packet.
     * Called when receiving a packet for the location this TileEntity is currently in.
     * <p>
     * Data is sent with {@link #writeCustomData(int, Consumer)}.
     * <p>
     * Typically used to perform a rendering update when receiving server-side state changes.
     * <p>
     * <em>Should be called automatically</em>.
     * <p>
     * This method is called <strong>Client-Side</strong>.
     * <p>
     * Equivalent to {@link BlockEntity#onDataPacket(Connection, ClientboundBlockEntityDataPacket)}
     *
     * @param discriminator the discriminator determining the packet sent.
     * @param buf           the buffer containing the packet data.
     * @see gregtech.api.capability.GregtechDataCodes
     */
    default void receiveCustomData(int discriminator, @NotNull FriendlyByteBuf buf) {}
}
