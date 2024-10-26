package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaserNetHandler implements ILaserContainer {

    private LaserPipeNet net;
    private final LaserPipeBlockEntity pipe;
    private final Direction facing;
    private final Level world;

    public LaserNetHandler(LaserPipeNet net, @NotNull LaserPipeBlockEntity pipe, @Nullable Direction facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getLevel();
    }

    public void updateNetwork(LaserPipeNet net) {
        this.net = net;
    }

    private void setPipesActive() {
        for (BlockPos pos : net.getAllNodes().keySet()) {
            if (world.getBlockEntity(pos) instanceof LaserPipeBlockEntity laserPipe) {
                laserPipe.setActive(true, 100);
            }
        }
    }

    @Nullable
    private ILaserContainer getInnerContainer() {
        if (net == null || pipe == null || pipe.isInValid() || (facing == null || pipe.isBlocked(facing))) {
            return null;
        }

        LaserRoutePath data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        return data.getHandler(net.getLevel());
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.acceptEnergyFromNetwork(side, voltage, amperage);
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.inputsEnergy(side);
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.outputsEnergy(side);
    }

    @Override
    public long changeEnergy(long amount) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.changeEnergy(amount);
    }

    @Override
    public long getEnergyStored() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyCapacity();
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    public LaserPipeNet getNet() {
        return net;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
