package com.gregtechceu.gtceu.common.pipelike.net.duct;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.BasicWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldDuctNet extends WorldPipeNet implements BasicWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GTCapability.CAPABILITY_HAZARD_CONTAINER };

    private static final String DATA_ID = "gtceu_world_laser_net";

    public static WorldDuctNet getWorldNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldDuctNet net = new WorldDuctNet();
            net.load(tag);
            return net;
        }, WorldDuctNet::new, DATA_ID);
    }

    public WorldDuctNet() {
        super(false, DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    public Iterator<BasicWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                    @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, BasicWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new DuctCapabilityObject(this) };
    }

    @Override
    public int getNetworkID() {
        return 3;
    }
}
