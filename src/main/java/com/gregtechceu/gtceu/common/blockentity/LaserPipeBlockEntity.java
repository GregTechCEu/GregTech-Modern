package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.pipelike.laser.*;
import com.gregtechceu.gtceu.utils.GTUtil;
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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

public class LaserPipeBlockEntity extends PipeBlockEntity<LaserPipeType, LaserPipeProperties> {

    @Getter
    protected final EnumMap<Direction, LaserNetHandler> handlers = new EnumMap<>(Direction.class);
    // the LaserNetHandler can only be created on the server, so we have an empty placeholder for the client
    public final ILaserContainer clientCapability = new DefaultLaserContainer();
    private WeakReference<LaserPipeNet> currentPipeNet = new WeakReference<>(null);
    @Getter
    protected LaserNetHandler defaultHandler;

    public LaserPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<LaserPipeBlockEntity> cableBlockEntityBlockEntityType) {}

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_LASER) {
            if (getLevel().isClientSide())
                return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> clientCapability));
            if (side != null && !isConnected(side)) return LazyOptional.empty();
            if (handlers.isEmpty()) {
                initHandlers();
            }
            checkNetwork();
            return GTCapability.CAPABILITY_LASER.orEmpty(cap,
                    LazyOptional.of(() -> handlers.getOrDefault(side, defaultHandler)));
        } else if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(this::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    public void initHandlers() {
        LaserPipeNet net = getLaserPipeNet();
        if (net == null) return;
        for (Direction facing : GTUtil.DIRECTIONS) {
            handlers.put(facing, new LaserNetHandler(net, this, facing));
        }
        defaultHandler = new LaserNetHandler(net, this, null);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            LaserPipeNet current = getLaserPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (LaserNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    public LaserPipeNet getLaserPipeNet() {
        if (level == null || level.isClientSide) {
            return null;
        }
        LaserPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(this.getBlockPos())) {
            return currentPipeNet;
        }
        LevelLaserPipeNet worldNet = (LevelLaserPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) this.getLevel());
        currentPipeNet = worldNet.getNetFromPos(this.getBlockPos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
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
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof LaserPipeBlockEntity) {
                return false;
            }
            return GTCapabilityHelper.getLaser(level, getBlockPos().relative(side), side.getOpposite()) != null;
        }
        return false;
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
            if (tile instanceof IPipeNode<?, ?> pipeTile &&
                    pipeTile.getPipeType().getClass() == this.getPipeType().getClass()) {
                connections = pipeTile.getConnections();
                connections &= ~(1 << side.ordinal());
                connections &= ~(1 << side.getOpposite().ordinal());
                if (connections != 0) return;
            }
        }
        super.setConnection(side, connected, fromNeighbor);
    }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
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

    private static class DefaultLaserContainer implements ILaserContainer {

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            return 0;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return false;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            return 0;
        }

        @Override
        public long getEnergyStored() {
            return 0;
        }

        @Override
        public long getEnergyCapacity() {
            return 0;
        }

        @Override
        public long getInputAmperage() {
            return 0;
        }

        @Override
        public long getInputVoltage() {
            return 0;
        }
    }
}
