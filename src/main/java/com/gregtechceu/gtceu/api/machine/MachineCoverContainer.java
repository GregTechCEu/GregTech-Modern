package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.GTUtil;

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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MachineCoverContainer implements ICoverable, IEnhancedManaged {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MachineCoverContainer.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Getter
    private final BlockPos pos;
    @Getter
    private final Level level;

    private final long offset = GTValues.RNG.nextInt(20);

    @DescSynced
    @Persisted
    @UpdateListener(methodName = "onCoverSet")
    @ReadOnlyManaged(onDirtyMethod = "onCoverDirty",
                     serializeMethod = "serializeCoverUid",
                     deserializeMethod = "deserializeCoverUid")
    private CoverBehavior up, down, north, south, west, east;

    public MachineCoverContainer(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
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
    public long getOffsetTimer() {
        if (level == null) return offset;
        else if (level.isClientSide()) return GTValues.CLIENT_TIME + offset;

        MinecraftServer server = level.getServer();
        if (server != null) return server.getTickCount() + offset;
        return offset;
    }

    @Override
    public void markDirty() {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.setChanged();
        }
    }

    @Override
    public void notifyBlockUpdate() {
        if (level != null) {
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }
    }

    @Override
    public void scheduleRenderUpdate() {
        if (level != null) {
            BlockState state = level.getBlockState(pos);
            if (level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) blockEntity.requestModelDataUpdate();
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    public void scheduleNeighborShapeUpdate() {
        if (level == null || pos == null)
            return;
        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    @Override
    public boolean isInValid() {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.isRemoved();
    }

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        ArrayList<VoxelShape> collisionList = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBE)
            machineBE.getMetaMachine().addCollisionBoundingBox(collisionList);
        else collisionList.add(Shapes.block());
        // noinspection RedundantIfStatement
        if (ICoverable.doesCoverCollide(side, collisionList, getCoverPlateThickness())) {
            // cover collision box overlaps with meta tile entity collision box
            return false;
        }

        return true;
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public @Nullable Direction getFrontFacing() {
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        return machine == null ? null : machine.getFrontFacing();
    }

    @Override
    public boolean shouldRenderBackSide() {
        return !level.getBlockState(pos).canOcclude();
    }

    @Nullable
    @Override
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine != null)
            return machine.subscribeServerTick(runnable);
        return null;
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) current.unsubscribe();
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
    public IItemHandler getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return GTCapabilityHelper.getItemHandler(level, pos, side);
        return machine.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public IFluidHandler getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return GTCapabilityHelper.getFluidHandler(level, pos, side);
        return machine.getFluidHandlerCap(side, useCoverCapability);
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
}
