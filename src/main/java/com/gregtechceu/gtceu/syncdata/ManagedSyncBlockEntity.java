package com.gregtechceu.gtceu.syncdata;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A BlockEntity that manages sync and save data via the {@code ISyncManaged} syncdata system.
 * 
 * @see ISyncManaged
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ManagedSyncBlockEntity extends BlockEntity implements ISyncManaged {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    public ManagedSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    protected ISyncManaged @NotNull [] getSyncObjects() {
        return new ISyncManaged[] { this };
    }

    // Called when this BlockEntity is saved or loaded

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        Arrays.stream(getSyncObjects()).map(obj -> obj.getSyncDataHolder().saveNBT()).forEach(tag::merge);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Arrays.stream(getSyncObjects()).forEach(obj -> obj.getSyncDataHolder().loadFromNBT(tag));
    }

    // Called when a client loads this BlockEntity

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        Arrays.stream(getSyncObjects()).map(obj -> obj.getSyncDataHolder().getClientSyncNBT(true)).forEach(tag::merge);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        Arrays.stream(getSyncObjects()).forEach(obj -> obj.getSyncDataHolder().loadClientSyncNBT(tag));
    }

    // Called when this BlockEntity has changed and must send changes to client.

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (entity) -> {
            if (!(entity instanceof ManagedSyncBlockEntity syncEntity)) return new CompoundTag();
            var tag = new CompoundTag();
            Arrays.stream(syncEntity.getSyncObjects()).map(obj -> obj.getSyncDataHolder().getClientSyncNBT(false))
                    .forEach(tag::merge);
            return tag;
        });
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag compound = pkt.getTag();
        if (compound != null) {
            Arrays.stream(getSyncObjects()).forEach(obj -> obj.getSyncDataHolder().loadClientSyncNBT(compound));
        }
    }

    @Override
    public void markAsChanged() {
        var level = getLevel();
        if (level instanceof ServerLevel sLvl) {
            sLvl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
