package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPipeNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
                          extends ITickSubscription, IPaintable {

    long getOffsetTimer();

    /**
     * Get Cover Container.
     */
    ICoverable getCoverContainer();

    /**
     * If tube is set to block connection from the specific side
     * 
     * @param side face
     */
    default boolean isBlocked(Direction side) {
        return PipeBlockEntity.isFaceBlocked(getBlockedConnections(), side);
    }

    /**
     * Unsafe!!! to set internal connections.
     * In general, you shouldn't call it yourself.
     */
    void setConnections(int connections);

    int getConnections();

    int getNumConnections();

    /**
     * set to block connection from the specific side
     * 
     * @param side      face
     * @param isBlocked is blocked
     */
    void setBlocked(Direction side, boolean isBlocked);

    /**
     * Whether pipe can attach to specific side.
     * e.g. check if there is an energyContainer nearby.
     */
    boolean canAttachTo(Direction side);

    /**
     * get connections for rendering and collision.
     */
    int getVisualConnections();

    /**
     * If node is connected to the specific side
     * 
     * @param side face
     */
    default boolean isConnected(Direction side) {
        return PipeBlockEntity.isConnected(getConnections(), side);
    }

    void setConnection(Direction side, boolean connected, boolean fromNeighbor);

    // if a face is blocked it will still render as connected, but it won't be able to receive stuff from that direction
    default boolean canHaveBlockedFaces() {
        return true;
    }

    int getBlockedConnections();

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default Level getPipeLevel() {
        return self().getLevel();
    }

    default BlockPos getPipePos() {
        return self().getBlockPos();
    }

    default void markAsDirty() {
        self().setChanged();
    }

    default boolean isInValid() {
        return self().isRemoved();
    }

    default boolean isRemote() {
        var level = getPipeLevel();
        if (level == null) {
            return GTCEu.isClientThread();
        }
        return level.isClientSide;
    }

    @SuppressWarnings("unchecked")
    default PipeBlock<PipeType, NodeDataType, ?> getPipeBlock() {
        return (PipeBlock<PipeType, NodeDataType, ?>) self().getBlockState().getBlock();
    }

    @Nullable
    default PipeNet<NodeDataType> getPipeNet() {
        if (getPipeLevel() instanceof ServerLevel serverLevel) {
            return getPipeBlock().getWorldPipeNet(serverLevel).getNetFromPos(getPipePos());
        }
        return null;
    }

    default PipeType getPipeType() {
        return getPipeBlock().pipeType;
    }

    @Nullable
    default NodeDataType getNodeData() {
        var net = getPipeNet();
        if (net != null) {
            return net.getNodeAt(getPipePos()).data;
        }
        return null;
    }

    void notifyBlockUpdate();

    default void scheduleRenderUpdate() {
        var pos = getPipePos();
        var level = getPipeLevel();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default void serverTick() {}

    default void scheduleNeighborShapeUpdate() {
        Level level = getPipeLevel();
        BlockPos pos = getPipePos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    default BlockEntity getNeighbor(Direction direction) {
        return getPipeLevel().getBlockEntity(getPipePos().relative(direction));
    }

    @Override
    default int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @NotNull
    Material getFrameMaterial();
}
