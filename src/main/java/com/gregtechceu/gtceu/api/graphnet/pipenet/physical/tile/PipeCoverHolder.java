package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererPackage;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.editor.runtime.PersistedParser;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.*;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;

public class PipeCoverHolder implements ICoverable, IEnhancedManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PipeCoverHolder.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    private final PipeBlockEntity holder;
    @RequireRerender
    @ReadOnlyManaged(onDirtyMethod = "onCoversDirty",
                     serializeMethod = "serializeCovers",
                     deserializeMethod = "deserializeCovers")
    private final EnumMap<Direction, CoverBehavior> covers = new EnumMap<>(Direction.class);
    private final int[] sidedRedstoneInput = new int[6];

    public PipeCoverHolder(PipeBlockEntity holder) {
        this.holder = holder;
    }

    protected final void addCoverSilent(@NotNull Direction side, @NotNull CoverBehavior cover) {
        // we checked before if the side already has a cover
        this.covers.put(side, cover);
    }

    @Override
    public final boolean acceptsCovers() {
        return covers.size() < GTUtil.DIRECTIONS.length;
    }

    @Override
    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
        if (coverBehavior != null) {
            addCoverSilent(side, coverBehavior);
            if (!getLevel().isClientSide) {
                // do not sync or handle logic on client side
                coverBehavior.getSyncStorage().markAllDirty();
                if (holder.isConnected(side) && !coverBehavior.canPipePassThrough()) {
                    PipeBlock.disconnectTile(holder, holder.getPipeNeighbor(side, true), side);
                }
            }

            holder.notifyBlockUpdate();
            holder.markAsDirty();
        }
    }

    @Override
    public boolean removeCover(boolean dropItself, Direction side, @Nullable Player player) {
        CoverBehavior cover = getCoverAtSide(side);
        if (cover == null) return ICoverable.super.removeCover(dropItself, side, player);

        holder.notifyBlockUpdate();
        holder.markAsDirty();
        return ICoverable.super.removeCover(side, player);
    }

    @Override
    public void onLoad() {
        ICoverable.super.onLoad();
        for (Direction side : GTUtil.DIRECTIONS) {
            this.sidedRedstoneInput[side.get3DDataValue()] = GTUtil.getRedstonePower(getLevel(), getPos(), side);
        }
    }

    @Override
    public final int getInputRedstoneSignal(@NotNull Direction side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; // covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.get3DDataValue()];
    }

    public void updateInputRedstoneSignals() {
        for (Direction side : GTUtil.DIRECTIONS) {
            int redstoneValue = GTUtil.getRedstonePower(getLevel(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.get3DDataValue()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.get3DDataValue()] = redstoneValue;
                CoverBehavior cover = getCoverAtSide(side);
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
        holder.scheduleRenderUpdate();
    }

    @Override
    public void scheduleNeighborShapeUpdate() {
        holder.scheduleNeighborShapeUpdate();
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        return holder.canConnectTo(side);
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

    public boolean shouldRenderCoverBackSides() {
        return false;
    }

    public int getPaintingColorForRendering() {
        return Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();
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
    public Level getLevel() {
        return holder.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return holder.getBlockPos();
    }

    @Override
    public @Nullable BlockEntity getNeighbor(@NotNull Direction side) {
        return holder.getNeighbor(side);
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        holder.onChanged();
    }

    @SuppressWarnings("unused")
    private boolean onCoversDirty(EnumMap<Direction, CoverBehavior> covers) {
        for (CoverBehavior coverBehavior : covers.values()) {
            if (coverBehavior != null) {
                for (IRef ref : coverBehavior.getSyncStorage().getNonLazyFields()) {
                    ref.update();
                }
                if (coverBehavior.getSyncStorage().hasDirtySyncFields() ||
                        coverBehavior.getSyncStorage().hasDirtyPersistedFields()) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private CompoundTag serializeCovers(EnumMap<Direction, CoverBehavior> covers) {
        CompoundTag tagCompound = new CompoundTag();
        ListTag coversList = new ListTag();
        for (var entry : covers.entrySet()) {
            CoverBehavior cover = entry.getValue();
            if (cover != null) {
                CompoundTag tag = new CompoundTag();
                ResourceLocation coverId = cover.coverDefinition.getId();
                tag.putString("id", coverId.toString());
                tag.putByte("side", (byte) entry.getKey().get3DDataValue());
                PersistedParser.serializeNBT(tag, cover.getClass(), cover);
                coversList.add(tag);
            }
        }
        tagCompound.put("Covers", coversList);
        return tagCompound;
    }

    @SuppressWarnings("unused")
    private EnumMap<Direction, CoverBehavior> deserializeCovers(CompoundTag tagCompound) {
        EnumMap<Direction, CoverBehavior> map = new EnumMap<>(Direction.class);

        ListTag coversList = tagCompound.getList("Covers", Tag.TAG_COMPOUND);
        for (int index = 0; index < coversList.size(); index++) {
            CompoundTag tag = coversList.getCompound(index);
            if (tag.contains("id", Tag.TAG_STRING)) {
                Direction coverSide = Direction.from3DDataValue(tag.getByte("Side"));
                ResourceLocation coverLocation = new ResourceLocation(tag.getString("CoverId"));
                CoverDefinition coverDefinition = GTRegistries.COVERS.get(coverLocation);
                if (coverDefinition == null) {
                    GTCEu.LOGGER.warn("Unable to find CoverDefinition for ResourceLocation {} at position {}",
                            coverLocation, holder.getBlockPos());
                } else {
                    CoverBehavior cover = coverDefinition.createCoverBehavior(this, coverSide);
                    PersistedParser.deserializeNBT(tag, new HashMap<>(), cover.getClass(), cover);
                    map.put(coverSide, cover);
                }
            }
        }
        return map;
    }
}
