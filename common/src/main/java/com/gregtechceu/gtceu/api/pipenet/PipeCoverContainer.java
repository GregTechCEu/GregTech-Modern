package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.syncdata.EnhancedFieldManagedStorage;
import com.gregtechceu.gtceu.api.syncdata.IEnhancedManaged;
import com.gregtechceu.gtceu.api.syncdata.UpdateListener;
import com.gregtechceu.gtceu.api.transfer.fluid.NoOpFluidTransfer;
import com.gregtechceu.gtceu.api.transfer.item.NoOpItemTransfer;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote PipeCoverContainer
 */

public class PipeCoverContainer implements ICoverable, IEnhancedManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PipeCoverContainer.class);
    @Getter
    private final EnhancedFieldManagedStorage syncStorage = new EnhancedFieldManagedStorage(this);
    private final IPipeNode<?, ?> pipeTile;

    @DescSynced
    @Persisted
    @UpdateListener(methodName = "onCoverSet")
    @ReadOnlyManaged(onDirtyMethod = "onCoverDirty", serializeMethod = "serializeCoverUid", deserializeMethod = "deserializeCoverUid")
    private CoverBehavior up, down, north, south, west, east;

    public PipeCoverContainer(IPipeNode<?, ?> pipeTile) {
        this.pipeTile = pipeTile;
    }

    @SuppressWarnings("unused")
    private void onCoverSet(CoverBehavior newValue, CoverBehavior oldValue) {
        if (newValue != oldValue && (newValue == null || oldValue == null)) {
            scheduleRenderUpdate();
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::markDirty);
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
    public long getOffsetTimer() {
        return pipeTile.getOffsetTimer();
    }

    @Override
    public void markDirty() {
        pipeTile.markAsDirty();
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
        return true;
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
    public IItemTransfer getItemTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        if (pipeTile instanceof ItemPipeBlockEntity itemPipe) {
            return itemPipe.getHandler(side, useCoverCapability);
        } else {
            return NoOpItemTransfer.INSTANCE;
        }
    }

    @Override
    public IFluidTransfer getFluidTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        if (pipeTile instanceof FluidPipeBlockEntity fluidPipe) {
            return fluidPipe.getHandler(side, useCoverCapability);
        } else {
            return NoOpFluidTransfer.INSTANCE;
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
        switch (side) {
            case UP -> up = coverBehavior;
            case SOUTH -> south = coverBehavior;
            case WEST -> west = coverBehavior;
            case DOWN -> down = coverBehavior;
            case EAST -> east = coverBehavior;
            case NORTH -> north = coverBehavior;
        }
        if (coverBehavior != null) {
            coverBehavior.getSyncStorage().markAllDirty();
        }
    }

    @SuppressWarnings("unused")
    private boolean onCoverDirty(CoverBehavior coverBehavior) {
        return coverBehavior != null && (coverBehavior.getSyncStorage().hasDirtySyncFields() || coverBehavior.getSyncStorage().hasDirtyPersistedFields());
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
        var side = Direction.values()[uid.getInt("side")];
        var definition = GTRegistries.COVERS.get(definitionId);
        if (definition != null) {
            return definition.createCoverBehavior(this, side);
        }
        GTCEu.LOGGER.error("couldn't find cover definition {}", definitionId);
        throw new RuntimeException();
    }

}
