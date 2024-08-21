package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.network.PacketDataList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SyncedBlockEntity extends BlockEntity implements ISyncedBlockEntity, INeighborCache {

    private final PacketDataList updates = new PacketDataList();

    public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public @Nullable BlockEntity getNeighbor(@NotNull Direction side) {
        if (level == null || worldPosition == null) return null;
        return level.getBlockEntity(worldPosition.relative(side));
    }

    @Override
    public final void writeCustomData(int discriminator, @NotNull Consumer<@NotNull FriendlyByteBuf> dataWriter) {
        ByteBuf backedBuffer = Unpooled.buffer();
        dataWriter.accept(new FriendlyByteBuf(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        this.updates.add(discriminator, updateData);
        notifyWorld();
    }

    /**
     * Adds all data packets from another synced tile entity. Useful when the old tile is replaced with a new one.
     *
     * @param syncedBlockEntity other synced tile entity
     */
    public void addPacketsFrom(SyncedBlockEntity syncedBlockEntity) {
        if (this == syncedBlockEntity || syncedBlockEntity.updates.isEmpty()) return;
        boolean wasEmpty = this.updates.isEmpty();
        this.updates.addAll(syncedBlockEntity.updates);
        syncedBlockEntity.updates.clear();
        if (wasEmpty) notifyWorld(); // if the data is not empty we already notified the world
    }

    private void notifyWorld() {
        BlockState blockState = getBlockState();
        level.sendBlockUpdated(getBlockPos(), blockState, blockState, 0);
    }

    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        if (this.updates.isEmpty()) {
            return null;
        }
        CompoundTag updateTag = new CompoundTag();
        updateTag.put("d", this.updates.dumpToNbt());
        return ClientboundBlockEntityDataPacket.create(this, be -> updateTag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag updateTag = pkt.getTag();
        ListTag listTag = updateTag.getList("d", Tag.TAG_COMPOUND);
        for (Tag entryBase : listTag) {
            CompoundTag entryTag = (CompoundTag) entryBase;
            for (String discriminatorKey : entryTag.getAllKeys()) {
                ByteBuf backedBuffer = Unpooled.copiedBuffer(entryTag.getByteArray(discriminatorKey));
                receiveCustomData(Integer.parseInt(discriminatorKey), new FriendlyByteBuf(backedBuffer));
                if (backedBuffer.readableBytes() != 0) {
                    GTCEu.LOGGER.error(
                            "Class {} failed to finish reading receiveCustomData with discriminator {} and {} bytes remaining",
                            BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()), discriminatorKey,
                            backedBuffer.readableBytes());
                }
            }
        }
    }

    @Override
    public final @NotNull CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        ByteBuf backedBuffer = Unpooled.buffer();
        writeInitialSyncData(new FriendlyByteBuf(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateTag.putByteArray("d", updateData);
        return updateTag;
    }

    @Override
    public final void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag); // deserializes Forge data and capabilities
        byte[] updateData = tag.getByteArray("d");
        ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
        receiveInitialSyncData(new FriendlyByteBuf(backedBuffer));
        if (backedBuffer.readableBytes() != 0) {
            GTCEu.LOGGER.error("Class {} failed to finish reading initialSyncData with {} bytes remaining",
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()), backedBuffer.readableBytes());
        }
    }
}
