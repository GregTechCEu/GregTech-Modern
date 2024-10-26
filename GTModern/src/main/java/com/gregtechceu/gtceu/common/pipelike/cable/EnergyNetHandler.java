package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;

public class EnergyNetHandler implements IEnergyContainer {

    private EnergyNet net;
    private boolean transfer;
    private final CableBlockEntity cable;
    private final Direction facing;

    public EnergyNetHandler(EnergyNet net, CableBlockEntity cable, Direction facing) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.facing = facing;
    }

    public EnergyNet getNet() {
        return net;
    }

    public void updateNetwork(EnergyNet net) {
        this.net = net;
    }

    @Override
    public long getEnergyCanBeInserted() {
        return getEnergyCapacity();
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (transfer) return 0;
        if (side == null) {
            if (facing == null) return 0;
            side = facing;
        }

        long amperesUsed = 0L;
        for (EnergyRoutePath path : net.getNetData(cable.getPipePos())) {
            if (path.getMaxLoss() >= voltage) {
                // Will lose all the energy with this path, so don't use it
                continue;
            }

            if (cable.getPipePos().equals(path.getTargetPipePos()) && side == path.getTargetFacing()) {
                // Do not insert into source handler
                continue;
            }

            IEnergyContainer dest = path.getHandler(getNet().getLevel());
            if (dest == null) continue;

            Direction facing = path.getTargetFacing().getOpposite();
            if (!dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;

            long pathVoltage = voltage - path.getMaxLoss();
            boolean cableBroken = false;
            for (CableBlockEntity cable : path.getPath()) {
                if (cable.getMaxVoltage() < voltage) {
                    int heat = (int) (Math.log(
                            GTUtil.getTierByVoltage(voltage) - GTUtil.getTierByVoltage(cable.getMaxVoltage())) *
                            45 + 36.5);
                    cable.applyHeat(heat);

                    cableBroken = cable.isInValid();
                    if (cableBroken) {
                        // a cable burned away (or insulation melted)
                        break;
                    }

                    // limit transfer to cables max and void rest
                    pathVoltage = Math.min(cable.getMaxVoltage(), pathVoltage);
                }
            }

            if (cableBroken) continue;

            transfer = true;
            long amps = dest.acceptEnergyFromNetwork(facing, pathVoltage, amperage - amperesUsed);
            transfer = false;
            if (amps == 0) continue;

            amperesUsed += amps;
            long voltageTraveled = voltage;
            for (CableBlockEntity cable : path.getPath()) {
                voltageTraveled -= cable.getNodeData().getLossPerBlock();
                if (voltageTraveled <= 0) break;

                if (!cable.isInValid()) {
                    cable.incrementAmperage(amps, voltageTraveled);
                }
            }

            if (amperage == amperesUsed) break;
        }

        net.addEnergyFluxPerSec(amperesUsed * voltage);
        return amperesUsed;
    }

    private void burnCable(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
        if (!getNet().getLevel().isClientSide) {
            getNet().getLevel().sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5 + getNet().getLevel().random.nextInt(3), 0.0, 0.0, 0.0, 0.1);
        }
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().getAmperage();
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().getVoltage();
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        GTCEu.LOGGER.warn("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(null,
                energyToAdd / getInputAmperage(),
                energyToAdd / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
