package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class LaserNetHandler implements ILaserContainer {
    private LaserPipeNet net;
    private final LaserPipeBlockEntity pipe;
    private final Direction facing;
    private final Level world;

    public LaserNetHandler(LaserPipeNet net, @Nonnull LaserPipeBlockEntity pipe, @Nullable Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getLevel();
    }

    public void updateNetwork(LaserPipeNet net) {
        this.net = net;
    }

    @Override
    public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.changeEnergy(amount, seen);
    }

    @Override
    public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.getEnergyStored(seen);
    }

    @Nullable
    private ILaserContainer getInnerContainer(@Nonnull Collection<ILaserContainer> seen) {
        if (net == null || pipe == null || pipe.isInValid() || pipe.isBlocked(facing)) {
            return null;
        }

        LaserPipeNet.LaserData data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        ILaserContainer handler = GTCapabilityHelper.getLaserContainer(world, pipe.getPipePos(), data.getFaceToHandler().getOpposite());
        if (seen.contains(handler)) {
            return null;
        }
        return handler;
    }

    @Override
    public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.getEnergyCapacity(seen);
    }

    public LaserPipeNet getNet() {
        return net;
    }
}
