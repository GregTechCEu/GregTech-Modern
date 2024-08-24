package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IRPCBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class NeighborCacheBlockEntity extends BaseSyncedBlockEntity implements INeighborCache,
                                               IAsyncAutoSyncBlockEntity, IRPCBlockEntity, IAutoPersistBlockEntity {

    private final BlockEntity[] neighbors = new BlockEntity[6];
    private boolean neighborsInvalidated = false;

    /**
     * @param doInvalidationHere set to false if you override {@link NeighborCacheBlockEntity#invalidateNeighbors()}
     *                           with a method that references something you do not yet have set.
     */
    public NeighborCacheBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState,
                                    boolean doInvalidationHere) {
        super(type, pos, blockState);
        if (doInvalidationHere) invalidateNeighbors();
    }

    protected void invalidateNeighbors() {
        if (!this.neighborsInvalidated) {
            Arrays.fill(this.neighbors, this);
            this.neighborsInvalidated = true;
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void setLevel(@NotNull Level LevelIn) {
        super.setLevel(LevelIn);
        invalidateNeighbors();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateNeighbors();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        invalidateNeighbors();
    }

    @Override
    public @Nullable BlockEntity getNeighbor(@NotNull Direction facing) {
        if (level == null || this.getBlockPos() == null) return null;
        int i = facing.get3DDataValue();
        BlockEntity neighbor = this.neighbors[i];
        if (neighbor == this || (neighbor != null && neighbor.isRemoved())) {
            neighbor = level.getBlockEntity(worldPosition.relative(facing));
            this.neighbors[i] = neighbor;
            this.neighborsInvalidated = false;
        }
        return neighbor;
    }

    @Override
    public @Nullable BlockEntity getNeighborNoChunkloading(@NotNull Direction facing) {
        if (level == null || this.getBlockPos() == null) return null;
        int i = facing.get3DDataValue();
        BlockEntity neighbor = this.neighbors[i];
        if (neighbor == this || (neighbor != null && neighbor.isRemoved())) {
            BlockPos pos = getBlockPos().relative(facing);
            if (level.isLoaded(pos)) {
                neighbor = level.getBlockEntity(pos);
                this.neighbors[i] = neighbor;
                this.neighborsInvalidated = false;
            } else return null;
        }
        return neighbor;
    }

    public void onNeighborChanged(Block fromBlock, BlockPos fromPos, boolean isMoving) {
        Direction facing = GTUtil.getFacingToNeighbor(this.getBlockPos(), fromPos);
        if (facing != null) this.neighbors[facing.get3DDataValue()] = this;
    }
}
