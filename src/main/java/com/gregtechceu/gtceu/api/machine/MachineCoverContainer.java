package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote MachineCoverContainer
 */
public class MachineCoverContainer implements ICoverable, IEnhancedManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MachineCoverContainer.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    private final MetaMachine machine;
    @DescSynced
    @Persisted
    @UpdateListener(methodName = "onCoverSet")
    @ReadOnlyManaged(onDirtyMethod = "onCoverDirty",
                     serializeMethod = "serializeCoverUid",
                     deserializeMethod = "deserializeCoverUid")
    private CoverBehavior up, down, north, south, west, east;

    private final int[] sidedRedstoneInput = new int[6];

    public MachineCoverContainer(MetaMachine machine) {
        this.machine = machine;
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
    public void onLoad() {
        ICoverable.super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, () -> {
                for (Direction side : GTUtil.DIRECTIONS) {
                    this.sidedRedstoneInput[side.get3DDataValue()] = GTUtil.getRedstonePower(getLevel(), getPos(), side);
                }
            }));
        }
    }

    @Override
    public Level getLevel() {
        return machine.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return machine.getPos();
    }

    @Override
    public long getOffsetTimer() {
        return machine.getOffsetTimer();
    }

    @Override
    public void markDirty() {
        machine.markDirty();
    }

    @Override
    public void notifyBlockUpdate() {
        machine.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        machine.scheduleRenderUpdate();
    }

    @Override
    public void scheduleNeighborShapeUpdate() {
        machine.scheduleNeighborShapeUpdate();
    }

    @Override
    public boolean isInValid() {
        return machine.isInValid();
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        ArrayList<VoxelShape> collisionList = new ArrayList<>();
        machine.addCollisionBoundingBox(collisionList);
        // noinspection RedundantIfStatement
        if (ICoverable.doesCoverCollide(side, collisionList, getCoverPlateThickness())) {
            // cover collision box overlaps with meta tile entity collision box
            return false;
        }

        return true;
    }

    @Override
    public boolean acceptsCovers() {
        return up == null || down == null || north == null || south == null || west == null || east == null;
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public Direction getFrontFacing() {
        return machine.getFrontFacing();
    }

    @Override
    public boolean shouldRenderBackSide() {
        return !machine.getBlockState().canOcclude();
    }

    @Nullable
    @Override
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        return machine.subscribeServerTick(runnable);
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {
        machine.unsubscribe(current);
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

    @Override
    public @Nullable BlockEntity getNeighbor(@NotNull Direction side) {
        return machine.getNeighbor(side);
    }

    @Override
    public int getInputRedstoneSignal(@NotNull Direction side, boolean ignoreCover) {
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

    @Override
    public IItemTransfer getItemTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getItemTransferCap(side, useCoverCapability);
    }

    @Override
    public IFluidTransfer getFluidTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        return machine.getFluidTransferCap(side, useCoverCapability);
    }

    @SuppressWarnings("unused")
    private boolean onCoverDirty(CoverBehavior coverBehavior) {
        if (coverBehavior != null) {
            for (IRef ref : coverBehavior.getSyncStorage().getNonLazyFields()) {
                ref.update();
            }
            return coverBehavior.getSyncStorage().hasDirtySyncFields() ||
                    coverBehavior.getSyncStorage().hasDirtyPersistedFields();
        }
        return false;
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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
        // FIXME
        return LazyOptional.empty();
    }
}
