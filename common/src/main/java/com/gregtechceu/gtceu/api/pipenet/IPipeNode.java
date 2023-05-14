package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.pipelike.Node;
import com.gregtechceu.gtlib.pipelike.PipeNet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public interface IPipeNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends IAttachData> extends ITickSubscription {

    long getOffsetTimer();

    /**
     * Get Cover Container.
     */
    ICoverable getCoverContainer();

    /**
     * If node is connected to the specific side
     * @param side face
     */
    boolean isConnected(Direction side);

    /**
     * If tube is set to block connection from the specific side
     * @param side face
     */
    boolean isBlocked(Direction side);

    /**
     * Unsafe!!! to set internal connections.
     * In general, you shouldn't call it yourself.
     */
    void setConnections(int connections);

    /**
     * set to block connection from the specific side
     * @param side face
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
     * should be called when neighbours / inner changed or the first time placing this pipe.
     */
    default void updateConnections() {
        var net = getPipeNet();
        if (net != null) {
            var pos = getPipePos();
            net.onNeighbourUpdate(pos);
            int connections = 0;
            var data = getNodeData();
            if (data == null) {
                GTLib.LOGGER.warn("data shouldn't be null here, did you add pipe without placement?");
                net.getWorldData().addNode(pos, getPipeType().modifyProperties(getPipeBlock().getFallbackType()), Node.DEFAULT_MARK, Node.ALL_OPENED, true);
                data = getNodeData();
                if (data == null) {
                    throw new IllegalStateException("data shouldn't be null here!");
                }
            }

            for (Direction side : Direction.values()) {
                if (isBlocked(side)) {
                    continue;
                }
                if (net.isNodeConnectedTo(pos, side)) {
                    connections |= 1 << side.ordinal();
                } else if (!net.containsNode(pos.relative(side))){
                    var canAttach = canAttachTo(side);
                    if (canAttach) {
                        connections |= 1 << side.ordinal();
                    }
                    if (data.setAttached(side, canAttach)) {
                        net.updateNodeData(pos, data);
                    }
                }
            }
            setConnections(connections);
        }
    }

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
            return GTLib.isRemote();
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

    default void notifyBlockUpdate() {
        var level = getPipeLevel();
        if (level != null) {
            level.updateNeighborsAt(getPipePos(), level.getBlockState(getPipePos()).getBlock());
        }
    }

    default void scheduleRenderUpdate() {
        var pos = getPipePos();
        var level = getPipeLevel();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, 1 << 3);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default void serverTick() {

    }

    default void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        if (!isRemote()) {
            updateConnections();
        }
        getCoverContainer().onNeighborChanged(block, fromPos, isMoving);
    }

}
