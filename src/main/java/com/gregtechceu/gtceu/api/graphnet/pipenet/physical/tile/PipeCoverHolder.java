package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;

import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;

public class PipeCoverHolder implements ICoverable, INBTSerializable<CompoundTag> {

    private final PipeBlockEntity holder;
    private final EnumMap<Direction, CoverBehavior> covers = new EnumMap<>(Direction.class);
    private final EnumSet<Direction> tickingCovers = EnumSet.noneOf(Direction.class);
    private final int[] sidedRedstoneInput = new int[6];

    public PipeCoverHolder(PipeBlockEntity holder) {
        this.holder = holder;
    }

    protected final void addCoverSilent(@NotNull Direction side, @NotNull CoverBehavior cover) {
        // we checked before if the side already has a cover
        this.covers.put(side, cover);
        if (cover instanceof ITickSubscription) {
            tickingCovers.add(side);
            holder.addTicker(new TickableSubscription(this::update));
        }
    }

    @Override
    public final void addCover(@NotNull Direction side, @NotNull CoverBehavior cover) {
        addCoverSilent(side, cover);
        if (!getLevel().isClientSide) {
            // do not sync or handle logic on client side
            CoverSaveHandler.writeCoverPlacement(this, COVER_ATTACHED_PIPE, side, cover);
            if (holder.isConnected(side) && !cover.canPipePassThrough()) {
                PipeBlock.disconnectTile(holder, holder.getPipeNeighbor(side, true), side);
            }
        }

        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @Override
    public final void removeCover(@NotNull Direction side) {
        Cover cover = getCoverAtSide(side);
        if (cover == null) return;

        dropCover(side);
        covers.remove(side);
        tickingCovers.remove(side);
        if (tickingCovers.isEmpty()) holder.removeTicker(this);
        writeCustomData(COVER_REMOVED_PIPE, buffer -> buffer.writeByte(side.getIndex()));
        holder.notifyBlockUpdate();
        holder.markAsDirty();
    }

    @Override
    public @NotNull ItemStack getStackForm() {
        return holder.getDrop();
    }

    public void onLoad() {
        for (Direction side : GTUtil.DIRECTIONS) {
            this.sidedRedstoneInput[side.getIndex()] = GTUtility.getRedstonePower(getLevel(), getPos(), side);
        }
    }

    @Override
    public final int getInputRedstoneSignal(@NotNull Direction side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; // covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.getIndex()];
    }

    public void updateInputRedstoneSignals() {
        for (Direction side : GTUtil.DIRECTIONS) {
            int redstoneValue = GTUtility.getRedstonePower(getLevel(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.getIndex()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.getIndex()] = redstoneValue;
                Cover cover = getCoverAtSide(side);
                if (cover != null) {
                    cover.onRedstoneInputSignalChange(redstoneValue);
                }
            }
        }
    }

    @Override
    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        BlockPos pos = getPos();
        getLevel().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void scheduleNeighborShapeUpdate() {

    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        return false;
    }

    @Override
    public double getCoverPlateThickness() {
        float thickness = holder.getBlockType().getStructure().getRenderThickness();
        // no cover plate for pipes >= 1 block thick
        if (thickness >= 1) return 0;

        // If the available space for the cover is less than the regular cover plate thickness, use that

        // need to divide by 2 because thickness is centered on the block, so the space is half on each side of the pipe
        return Math.min(1.0 / 16.0, (1.0 - thickness) / 2);
    }

    @Override
    public Direction getFrontFacing() {
        return null;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return false;
    }

    @Override
    public IItemTransfer getItemTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public IFluidTransfer getFluidTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {

    }

    @Override
    public boolean shouldRenderCoverBackSides() {
        return false;
    }

    @Override
    public int getPaintingColorForRendering() {
        return ConfigHolder.client.defaultPaintingColor;
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull Direction side) {
        return holder.canConnectTo(side);
    }

    @Override
    public final boolean acceptsCovers() {
        return covers.size() < GTUtil.DIRECTIONS.length;
    }

    public boolean canConnectRedstone(@Nullable Direction side) {
        // so far null side means either upwards or downwards redstone wire connection
        // so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(Direction.UP) ||
                    canConnectRedstone(Direction.DOWN);
        }
        CoverBehavior cover = getCoverAtSide(side);
        return cover != null && cover.canConnectRedstone();
    }

    public int getOutputRedstoneSignal(@Nullable Direction side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        CoverBehavior cover = getCoverAtSide(side);
        return cover == null ? 0 : cover.getRedstoneSignalOutput();
    }

    public int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (Direction side : GTUtil.DIRECTIONS) {
            CoverBehavior cover = getCoverAtSide(side);
            if (cover == null) continue;
            highestSignal = Math.max(highestSignal, cover.getRedstoneSignalOutput());
        }
        return highestSignal;
    }

    @Override
    public void update() {
        if (!getLevel().isClientSide) {
            updateCovers();
        }
    }

    @Override
    public void writeCoverData(@NotNull Cover cover, int discriminator, @NotNull Consumer<@NotNull PacketBuffer> buf) {
        writeCustomData(UPDATE_COVER_DATA_PIPE, buffer -> {
            buffer.writeByte(cover.getAttachedSide().getIndex());
            buffer.writeVarInt(discriminator);
            buf.accept(buffer);
        });
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        CoverSaveHandler.writeInitialSyncData(buf, this);
    }

    public void readInitialSyncData(PacketBuffer buf) {
        CoverSaveHandler.receiveInitialSyncData(buf, this);
    }

    @Override
    public void writeCustomData(int dataId, @NotNull Consumer<PacketBuffer> writer) {
        holder.writeCustomData(dataId, writer);
    }

    public void readCustomData(int dataId, PacketBuffer buf) {
        if (dataId == COVER_ATTACHED_PIPE) {
            CoverSaveHandler.readCoverPlacement(buf, this);
        } else if (dataId == COVER_REMOVED_PIPE) {
            // cover removed event
            Direction placementSide = GTUtil.DIRECTIONS[buf.readByte()];
            this.covers.remove(placementSide);
            this.tickingCovers.remove(placementSide);
            if (this.tickingCovers.isEmpty()) holder.removeTicker(this);
            holder.scheduleRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_PIPE) {
            // cover custom data received
            Direction coverSide = GTUtil.DIRECTIONS[buf.readByte()];
            Cover cover = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (cover != null) {
                cover.readCustomData(internalId, buf);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CoverSaveHandler.writeCoverNBT(tag, this);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CoverSaveHandler.readCoverNBT(nbt, this, this::addCoverSilent);
    }

    @Override
    public Level getLevel() {
        return holder.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return holder.getBlockPos();
    }

    @Override
    public @Nullable BlockEntity getNeighbor(@NotNull Direction facing) {
        return holder.getNeighbor(facing);
    }

    @Override
    public long getOffsetTimer() {
        return holder.getOffsetTimer();
    }

    @Nullable
    @Override
    public CoverBehavior getCoverAtSide(@NotNull Direction side) {
        return covers.get(side);
    }

    @Override
    public boolean hasAnyCover() {
        return !covers.isEmpty();
    }

    @Override
    public void markDirty() {
        holder.markAsDirty();
    }

    @Override
    public boolean isInValid() {
        return holder.isRemoved();
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction side) {
        return holder.getCapabilityCoverQuery(capability, side);
    }

    @OnlyIn(Dist.CLIENT)
    public CoverRendererPackage createPackage() {
        if (covers.isEmpty()) return CoverRendererPackage.EMPTY;
        CoverRendererPackage rendererPackage = new CoverRendererPackage(shouldRenderCoverBackSides());
        for (var cover : covers.entrySet()) {
            rendererPackage.addRenderer(cover.getValue().getRenderer(), cover.getKey());
        }
        return rendererPackage;
    }

    @Override
    public @Nullable TickableSubscription subscribeServerTick(Runnable runnable) {
        return holder.subscribeServerTick(runnable);
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {
        holder.unsubscribe(current);
    }
}
