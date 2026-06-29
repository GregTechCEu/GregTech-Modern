package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeBlockEntity;

import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class PipeCoverContainer implements ICoverable, ISyncManaged {

    @Getter
    private final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    private final PipeBlockEntity<?> pipeTile;

    @SyncToClient
    @SaveField
    @RerenderOnChanged
    @Nullable
    private CoverBehavior up, down, north, south, west, east;

    public PipeCoverContainer(PipeBlockEntity<?> pipeTile) {
        this.pipeTile = pipeTile;
    }

    @Override
    public IGregtechBlockEntity getHolder() {
        return pipeTile;
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

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
        /*
        if (pipeTile instanceof ItemPipeBlockEntity itemPipe) {
            return getLevel() instanceof ServerLevel ? itemPipe.getHandler(side, useCoverCapability) :
                    (IItemHandlerModifiable) EmptyHandler.INSTANCE;
        } else {
            return null;
        }*/
    }

    @Override
    public @Nullable IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (pipeTile instanceof FluidPipeBlockEntity fluidPipe) {
            return fluidPipe.getTankList(side);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable CoverBehavior getCoverAtSide(Direction side) {
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
        getSyncDataHolder().resyncAllFields();
    }

    public boolean canPipePassThrough(Direction side) {
        var cover = getCoverAtSide(side);
        return cover == null || cover.canPipePassThrough();
    }
}
