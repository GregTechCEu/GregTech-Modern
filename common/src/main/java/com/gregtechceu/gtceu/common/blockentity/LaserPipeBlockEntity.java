package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserNetHandler;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeNet;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumMap;

public class LaserPipeBlockEntity extends PipeBlockEntity<LaserPipeType, LaserPipeNet.LaserData> {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LaserPipeBlockEntity.class, PipeBlockEntity.MANAGED_FIELD_HOLDER);

    private final EnumMap<Direction, LaserNetHandler> handlers = new EnumMap<>(Direction.class);
    // the LaserNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final ILaserContainer clientCapability = new DefaultLaserContainer();
    private WeakReference<LaserPipeNet> currentPipeNet = new WeakReference<>(null);
    private LaserNetHandler defaultHandler;

    private int ticksActive = 0;
    private int activeDuration = 0;
    @Getter
    @Persisted @DescSynced
    private boolean active = false;

    public LaserPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void initHandlers() {
        LaserPipeNet net = getLaserPipeNet();
        if (net == null) return;
        for (Direction facing : Direction.values()) {
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
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipePos())) {
            return currentPipeNet;
        }
        LevelLaserPipeNet worldNet = (LevelLaserPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getPipeLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
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
        boolean stateChanged = false;
        if (this.active && !active) {
            this.active = false;
            stateChanged = true;
        } else if (!this.active && active) {
            this.active = true;
            stateChanged = true;
            activeDuration = duration;
        } else if (this.active) {
            this.ticksActive = 0;
            this.activeDuration = duration;
        }

        if (stateChanged) {
            notifyBlockUpdate();
            markAsDirty();
        }
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof LaserPipeBlockEntity) {
                return false;
            }
            return side.getAxis() == this.getBlockState().getValue(BlockStateProperties.FACING).getAxis() && GTCapabilityHelper.getLaserContainer(level, getBlockPos().relative(side), side.getOpposite()) != null;
        }
        return false;
    }

    private static class DefaultLaserContainer implements ILaserContainer {
        @Override
        public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
            return 0;
        }

        @Override
        public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
            return 0;
        }

        @Override
        public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
            return 0;
        }
    }
}
