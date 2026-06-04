package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.Nullable;

public class LaserPipeBlockEntity extends PipeBlockEntity<LaserPipeVariant, LaserPipeSegmentProperties> {

    public LaserPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_LASER) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    /**
     * @param active   if the pipe should become active
     * @param duration how long the pipe should be active for
     */
    public void setActive(boolean active, int duration) {
        setPipeActive(this, this.getBlockState(), active, duration);
    }

    public boolean isActive() {
        return this.getBlockState().getValue(GTBlockStateProperties.ACTIVE);
    }

    @Override
    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        if (!getLevel().isClientSide && connected) {
            int connections = getConnections();
            // block connection if any side other than the requested side and its opposite side are already connected.
            connections &= ~(1 << side.ordinal());
            connections &= ~(1 << side.getOpposite().ordinal());
            if (connections != 0) return;

            // check the same for the targeted pipe
            BlockEntity tile = getLevel().getBlockEntity(getBlockPos().relative(side));
            if (tile instanceof LaserPipeBlockEntity other) {
                connections = other.getConnections();
                connections &= ~(1 << side.ordinal());
                connections &= ~(1 << side.getOpposite().ordinal());
                if (connections != 0) return;
            }
        }
        super.setConnection(side, connected, fromNeighbor);
    }

    public static BlockState setPipeActive(PipeBlockEntity<?, ?> blockEntity,
                                           BlockState state, boolean newActive, int duration) {
        if (!state.hasProperty(GTBlockStateProperties.ACTIVE) ||
                state.getValue(GTBlockStateProperties.ACTIVE) == newActive) {
            return state;
        }
        BlockState newState = state.setValue(GTBlockStateProperties.ACTIVE, newActive);
        if (blockEntity == null || blockEntity.getLevel() == null || blockEntity.isRemoved()) {
            return newState;
        }
        Level level = blockEntity.getLevel();

        level.setBlock(blockEntity.getBlockPos(), newState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        blockEntity.notifyBlockUpdate();
        blockEntity.setChanged();

        if (newActive && level instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueServerTask(serverLevel, () -> setPipeActive(blockEntity, newState, false, -1), duration);
        }
        return newState;
    }

    @Override
    public boolean canPipesConnect(Direction side, PipeBlockEntity<LaserPipeVariant, LaserPipeSegmentProperties> other) {
        return other instanceof LaserPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(Direction side, Block block, @Nullable BlockEntity blockEntity) {
        return blockEntity != null &&
                blockEntity.getCapability(GTCapability.CAPABILITY_LASER, side.getOpposite()).isPresent();
    }
}
