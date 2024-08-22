package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.AbstractGroupData;
import com.gregtechceu.gtceu.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import com.gregtechceu.gtceu.api.graphnet.edge.NetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class WorldEnergyNet extends WorldPipeNet implements FlowWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GTCapability.CAPABILITY_ENERGY_CONTAINER };

    private static final String DATA_ID = "gtceu_world_energy_net";

    public static WorldEnergyNet getWorldNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldEnergyNet net = new WorldEnergyNet();
            net.load(tag);
            return net;
        }, WorldEnergyNet::new, DATA_ID);
    }

    public WorldEnergyNet() {
        super(false, DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    public boolean usesDynamicWeights(int algorithmID) {
        return true;
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new EnergyCapabilityObject(this, node) };
    }

    @Override
    public Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                   @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, FlowWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public @NotNull NetFlowEdge getNewEdge() {
        return new NetFlowEdge(1);
    }

    @Override
    public AbstractGroupData getBlankGroupData() {
        return new EnergyGroupData();
    }

    @Override
    public int getNetworkID() {
        return 0;
    }
}
