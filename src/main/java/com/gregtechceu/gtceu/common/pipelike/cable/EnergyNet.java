package com.gregtechceu.gtceu.common.pipelike.cable;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.*;

public class EnergyNet extends PipeNet<WireProperties> {

    private final Map<BlockPos, List<EnergyRoutePath>> NET_DATA = new HashMap<>();

    private long lastEnergyFluxPerSec;
    private long energyFluxPerSec;
    private long lastTime;

    protected EnergyNet(LevelPipeNet<WireProperties, ? extends EnergyNet> world) {
        super(world);
    }

    public List<EnergyRoutePath> getNetData(BlockPos pipePos) {
        List<EnergyRoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = EnergyNetWalker.createNetData(this, pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(EnergyRoutePath::getDistance));
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
    protected void transferNodeData(Map<BlockPos, Node<WireProperties>> transferredNodes,
                                    PipeNet<WireProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((EnergyNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(WireProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putLong("voltage", nodeData.getVoltage());
        tagCompound.putInt("amperage", nodeData.getAmperage());
        tagCompound.putInt("loss", nodeData.getLossPerBlock());
    }

    @Override
    protected WireProperties readNodeData(CompoundTag tagCompound) {
        long voltage = tagCompound.getLong("voltage");
        int amperage = tagCompound.getInt("amperage");
        int lossPerBlock = tagCompound.getInt("loss");
        return new WireProperties(voltage, amperage, lossPerBlock);
    }

    //////////////////////////////////////
    // ******* Pipe Status *******//
    //////////////////////////////////////

    public long getEnergyFluxPerSec() {
        Level world = getLevel();
        if (world != null && !world.isClientSide && (world.getGameTime() - lastTime) >= 20) {
            lastTime = world.getGameTime();
            clearCache();
        }
        return lastEnergyFluxPerSec;
    }

    public void addEnergyFluxPerSec(long energy) {
        energyFluxPerSec += energy;
    }

    public void clearCache() {
        lastEnergyFluxPerSec = energyFluxPerSec;
        energyFluxPerSec = 0;
    }
}
