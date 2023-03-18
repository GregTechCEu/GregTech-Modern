package com.lowdragmc.gtceu.api.pipenet;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.capability.ICoverable;
import com.lowdragmc.gtceu.api.cover.CoverBehavior;
import com.lowdragmc.gtceu.api.cover.CoverDefinition;
import com.lowdragmc.gtceu.api.machine.TickableSubscription;
import com.lowdragmc.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote PipeCoverContainer
 */

public class PipeCoverContainer implements ICoverable, IManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PipeCoverContainer.class);
    private final FieldManagedStorage storage = new FieldManagedStorage(this);
    private final IPipeNode<?, ?> pipeTile;

    @DescSynced
    @Persisted
    @ReadOnlyManaged(onDirtyMethod = "onCoverDirty", serializeMethod = "serializeCoverUid", deserializeMethod = "deserializeCoverUid")
    private CoverBehavior up, down, north, south, west, east;

    public PipeCoverContainer(IPipeNode<?, ?> pipeTile) {
        this.pipeTile = pipeTile;
        if (isRemote()) {
            addSyncUpdateListener("up", this::onCoverSet);
            addSyncUpdateListener("down", this::onCoverSet);
            addSyncUpdateListener("north", this::onCoverSet);
            addSyncUpdateListener("south", this::onCoverSet);
            addSyncUpdateListener("west", this::onCoverSet);
            addSyncUpdateListener("east", this::onCoverSet);
        }
    }

    private void onCoverSet(String var1, Object newValue, Object oldValue) {
        if (newValue != oldValue && (newValue == null || oldValue == null)) {
            scheduleRenderUpdate();
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return storage;
    }

    @Override
    public void onChanged() {
        markDirty();
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
    public boolean isInValid() {
        return pipeTile.isInValid();
    }

    @Override
    public boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition, ServerPlayer player) {
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(coverDefinition, side) || !coverBehavior.canAttach()) {
            return false;
        }
        if (getCoverAtSide(side) != null) {
            removeCover(side);
        }
        coverBehavior.onAttached(itemStack, player);
        coverBehavior.onLoad();
        setCoverAtSide(coverBehavior, side);
        notifyBlockUpdate();
        markDirty();
        // TODO achievement
//        AdvancementTriggers.FIRST_COVER_PLACE.trigger((EntityPlayerMP) player);
        return true;
    }

    @Override
    public boolean removeCover(Direction side) {
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getDrops();
        coverBehavior.onRemoved();
        setCoverAtSide(null, side);
        for (ItemStack dropStack : drops) {
            Block.popResource(getLevel(), getPos(), dropStack);
        }
        notifyBlockUpdate();
        markDirty();
        return true;
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
    public int getPaintingColorForRendering() {
        return -1;
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

    private void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
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
        return coverBehavior != null && coverBehavior.getSyncStorage().hasDirtyFields();
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
