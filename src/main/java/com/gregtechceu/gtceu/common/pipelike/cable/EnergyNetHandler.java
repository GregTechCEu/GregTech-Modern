package com.gregtechceu.gtceu.common.pipelike.cable;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EnergyNetHandler implements IEnergyContainer {

    private final EnergyNet net;
    private final CableBlockEntity cable;
    private final List<CableRoutePath> paths;

    public EnergyNetHandler(EnergyNet net, CableBlockEntity cable) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.paths = net.getNetData(cable.getPipePos());
    }

    public EnergyNet getNet() {
        return net;
    }

    @Override
    public long getEnergyCanBeInserted() {
        return getEnergyCapacity();
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        long amperesUsed = 0L;
        Set<BlockPos> burnedCables = new HashSet<>();
        for (CableRoutePath path : paths) {
            if (path.getMaxLoss() >= voltage)
                continue;
            if (Objects.equals(cable.getPipePos(), path.getPipePos()) && side == path.getFaceToHandler()) {
                //Do not insert into source handler
                continue;
            }
            IEnergyContainer dest = path.getHandler(cable.getPipeLevel());
            Direction facing = path.getFaceToHandler().getOpposite();
            if (dest == null || !dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;
            long v = voltage - path.getMaxLoss();
            if (v <= 0)
                continue;

            for (var pair : path.getPath()) {
                var cable = pair.getB().properties();
                if (cable.getVoltage() < voltage) {
                    int heat = (int) (Math.log(GTUtil.getTierByVoltage(voltage) - GTUtil.getTierByVoltage(cable.getVoltage())) * 45 + 36.5);

                    if (net.applyHeat(pair.getA(), heat)) {
                        // a cable burned away (or insulation melted)
                        // recompute net data
                        burnedCables.add(pair.getA());
                    }

                    v = Math.min(cable.getVoltage(), v); // limit transfer to cables max and void rest
                }
            }

            if (!burnedCables.isEmpty()) {
                break;
            }

            long amps = dest.acceptEnergyFromNetwork(facing, v, amperage - amperesUsed);
            if(amps == 0)
                continue;
            amperesUsed += amps;

            long voltageTraveled = voltage;
            for (var pair : path.getPath()) {
                var cable = pair.getB().properties();
                voltageTraveled -= cable.getLossPerBlock();
                if (voltageTraveled <= 0)
                    break;
                if (net.incrementAmperage(pair.getA(), amps, cable.getAmperage())) {
                    // a cable burned away (or insulation melted)
                    // recompute net data
                    burnedCables.add(pair.getA());
                }
            }

            if (!burnedCables.isEmpty() || amperage == amperesUsed)
                break;
        }
        for (BlockPos pos : burnedCables) {
            burnCable(net.getLevel(), pos);
        }
        return amperesUsed;
    }

    private void burnCable(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().properties.getAmperage();
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().properties.getVoltage();
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
