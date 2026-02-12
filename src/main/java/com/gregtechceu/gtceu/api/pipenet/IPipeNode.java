package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPipeNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
                          extends ITickSubscription, IPaintable, IGregtechBlockEntity {

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

    default BlockState getState() {
        return self().getBlockState();
    }

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    @SuppressWarnings("unchecked")
    default PipeBlock<PipeType, NodeDataType, ?> getPipeBlock() {
        return (PipeBlock<PipeType, NodeDataType, ?>) self().getBlockState().getBlock();
    }

    @Nullable
    default PipeNet<NodeDataType> getPipeNet() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return getPipeBlock().getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
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
            return net.getNodeAt(getBlockPos()).data;
        }
        return null;
    }

    default void serverTick() {}

    @Override
    default int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @NotNull
    Material getFrameMaterial();
}
