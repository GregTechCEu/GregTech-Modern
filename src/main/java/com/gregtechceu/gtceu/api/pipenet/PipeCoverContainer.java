package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.syncdata.ISyncManaged;
import com.gregtechceu.gtceu.syncdata.SyncDataHolder;
import com.gregtechceu.gtceu.syncdata.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncdata.annotations.SaveField;
import com.gregtechceu.gtceu.syncdata.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class PipeCoverContainer implements ICoverable, ISyncManaged {

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private final IPipeNode<?, ?> pipeTile;

    @SyncToClient
    @SaveField
    @RerenderOnChanged
    private CoverBehavior up, down, north, south, west, east;

    public PipeCoverContainer(IPipeNode<?, ?> pipeTile) {
        this.pipeTile = pipeTile;
    }

    @Override
    public void markAsChanged() {
        var level = getLevel();
        if (level instanceof ServerLevel sLvl) {
            sLvl.sendBlockUpdated(getPos(), pipeTile.getState(), pipeTile.getState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Level getLevel() {
        return pipeTile.getPipeLevel();
    }

    @Override
    public BlockPos getPos() {
        return pipeTile.getPipePos();
    }

    @Override
    public BlockState getState() {
        return pipeTile.getState();
    }

    @Override
    public long getOffsetTimer() {
        return pipeTile.getOffsetTimer();
    }

    @Override
    public void notifyBlockUpdate() {
        pipeTile.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        pipeTile.scheduleRenderUpdate();
    }

    @Override
    public void scheduleNeighborShapeUpdate() {
        pipeTile.scheduleNeighborShapeUpdate();
    }

    @Override
    public boolean isInValid() {
        return pipeTile.isInValid();
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        return getCoverAtSide(side) == null;
    }

    @Override
    public double getCoverPlateThickness() {
        float thickness = pipeTile.getPipeType().getThickness();
        // no cover plate for pipes >= 1 block thick
        if (thickness >= 1) return 0;

        // If the available space for the cover is less than the regular cover plate thickness, use that

        // need to divide by 2 because thickness is centered on the block, so the space is half on each side of the pipe
        return Math.min(1.0 / 16.0, (1.0 - thickness) / 2);
    }

    @Override
    public Direction getFrontFacing() {
        return Direction.NORTH;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return true;
    }

    @Nullable
    @Override
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        return pipeTile.subscribeServerTick(runnable);
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {
        pipeTile.unsubscribe(current);
    }

    @Override
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (pipeTile instanceof ItemPipeBlockEntity itemPipe) {
            return getLevel() instanceof ServerLevel ? itemPipe.getHandler(side, useCoverCapability) :
                    (IItemHandlerModifiable) EmptyHandler.INSTANCE;
        } else {
            return null;
        }
    }

    @Override
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (pipeTile instanceof FluidPipeBlockEntity fluidPipe) {
            return fluidPipe.getTankList(side);
        } else {
            return null;
        }
    }

    @Override
    public CoverBehavior getCoverAtSide(Direction side) {
        return switch (side) {
            case UP -> up;
            case SOUTH -> south;
            case WEST -> west;
            case DOWN -> down;
            case EAST -> east;
            case NORTH -> north;
        };
    }

    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
        var previousCover = getCoverAtSide(side);
        switch (side) {
            case UP -> up = coverBehavior;
            case SOUTH -> south = coverBehavior;
            case WEST -> west = coverBehavior;
            case DOWN -> down = coverBehavior;
            case EAST -> east = coverBehavior;
            case NORTH -> north = coverBehavior;
        }
        if (coverBehavior != null) {
            if (coverBehavior.canPipePassThrough()) {
                pipeTile.setConnection(side, true, false);
            }
        } else if (previousCover != null && previousCover.canPipePassThrough()) {
            pipeTile.setConnection(side, false, false);
        }
    }

    @SuppressWarnings("unused")
    private CompoundTag serializeCoverUid(CoverBehavior coverBehavior) {
        var uid = new CompoundTag();
        uid.putString("id", GTRegistries.COVERS.getKey(coverBehavior.coverDefinition).toString());
        uid.putInt("side", coverBehavior.attachedSide.ordinal());
        return uid;
    }

    @SuppressWarnings("unused")
    private CoverBehavior deserializeCoverUid(CompoundTag uid) {
        var definitionId = new ResourceLocation(uid.getString("id"));
        var side = GTUtil.DIRECTIONS[uid.getInt("side")];
        var definition = GTRegistries.COVERS.get(definitionId);
        if (definition != null) {
            return definition.createCoverBehavior(this, side);
        }
        GTCEu.LOGGER.error("couldn't find cover definition {}", definitionId);
        throw new RuntimeException();
    }
}
