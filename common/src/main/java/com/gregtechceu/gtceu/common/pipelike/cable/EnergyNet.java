package com.gregtechceu.gtceu.common.pipelike.cable;


import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.Node;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class EnergyNet extends PipeNet<CableData> {
    public static final int DEFAULT_TEMPERATURE = 293;
    public static final int MELT_TEMP = 3000;
    public static final int SMOKE_TEMP = 2000;
    private final Map<BlockPos, List<CableRoutePath>> NET_DATA = new HashMap<>();

    protected EnergyNet(LevelPipeNet<CableData, ? extends EnergyNet> world) {
        super(world);
        lastHeatUpdate = world.getWorld().getGameTime();
        lastVoltageUpdate = lastHeatUpdate;
    }

    public List<CableRoutePath> getNetData(BlockPos pipePos) {
        List<CableRoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = EnergyNetWalker.createNetData(this, pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(CableRoutePath::getDistance));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<CableData>> transferredNodes, PipeNet<CableData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((EnergyNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(CableData nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("voltage", nodeData.properties().getVoltage());
        tagCompound.putInt("amperage", nodeData.properties().getAmperage());
        tagCompound.putInt("loss", nodeData.properties().getLossPerBlock());
        tagCompound.putByte("connections", nodeData.connections());
    }

    @Override
    protected CableData readNodeData(CompoundTag tagCompound) {
        int voltage = tagCompound.getInt("voltage");
        int amperage = tagCompound.getInt("amperage");
        int lossPerBlock = tagCompound.getInt("loss");
        return new CableData(new WireProperties(voltage, amperage, lossPerBlock), tagCompound.getByte("connections"));
    }


    //////////////////////////////////////
    //*******     Pipe Status    *******//
    //////////////////////////////////////

    private final Long2IntMap cableHeat = new Long2IntOpenHashMap();
    private final Long2LongMap cableAmps = new Long2LongOpenHashMap();
    private long lastHeatUpdate, lastVoltageUpdate;

    private void updateCableHeat() {
        var latestTime = getWorldData().getWorld().getGameTime();
        long duration = latestTime - lastHeatUpdate;
        lastHeatUpdate = latestTime;
        if (duration > 0) {
            var iter = cableHeat.long2IntEntrySet().iterator();
            while (iter.hasNext()) {
                var entry = iter.next();
                if (containsNode(BlockPos.of(entry.getLongKey()))) {
                    var temp = entry.getIntValue();
                    var newTemp = temp - duration * 50;
                    if (newTemp > DEFAULT_TEMPERATURE) {
                        entry.setValue((int)newTemp);
                    } else {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        } else if (duration < 0) {
            cableHeat.clear();
        }
    }

    /**
     * apply cable heat.
     * @return if accumulated heat over threshold will burn cable directly and return true.
     */
    public boolean applyHeat(BlockPos cablePos, int heat) {
        updateCableHeat();
        var current = cableHeat.getOrDefault(cablePos.asLong(), DEFAULT_TEMPERATURE);
        if (current + heat > SMOKE_TEMP) {
            var facing = Direction.UP;
            float xPos = facing.getStepX() * 0.76F + cablePos.getX() + 0.25F;
            float yPos = facing.getStepY() * 0.76F + cablePos.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + cablePos.getZ() + 0.25F;

            float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            float xSpd = (float) Math.sin(temp) * 0.1F;
            float zSpd = (float) Math.cos(temp) * 0.1F;

            getLevel().sendParticles(ParticleTypes.SMOKE,
                    xPos + GTValues.RNG.nextFloat() * 0.5F,
                    yPos + GTValues.RNG.nextFloat() * 0.5F,
                    zPos + GTValues.RNG.nextFloat() * 0.5F,
                    10,
                    xSpd, ySpd, zSpd, 0.1);
        }
        if (current + heat <= MELT_TEMP) {
            cableHeat.put(cablePos.asLong(), current + heat);
        } else { // burn
            cableHeat.remove(cablePos.asLong());
            return true;
        }
        return false;
    }

    private void updateCableVoltage() {
        var latestTime = getWorldData().getWorld().getGameTime();
        if (lastVoltageUpdate != latestTime) {
            cableAmps.clear();
        }
        lastVoltageUpdate = latestTime;
    }

    /**
     * Should only be called internally
     *
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(BlockPos cablePos, long amps, int maxAmps) {
        updateCableVoltage();
        var lastAmps = cableAmps.getOrDefault(cablePos.asLong(), 0);
        cableAmps.put(cablePos.asLong(), lastAmps + amps);
        var dif = lastAmps + amps - maxAmps;
        if (dif > 0) {
            return applyHeat(cablePos, (int) (dif * 40));
        }
        return false;
    }
}
