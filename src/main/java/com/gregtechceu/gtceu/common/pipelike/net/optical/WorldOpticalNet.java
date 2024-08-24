package com.gregtechceu.gtceu.common.pipelike.net.optical;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.alg.SinglePathAlgorithm;
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

public class WorldOpticalNet extends WorldPipeNet implements BasicWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GTCapability.CAPABILITY_DATA_ACCESS };

    private static final String DATA_ID = "gtceu_world_optical_net";

    public static WorldOpticalNet getWorldNet(ServerLevel serverLevel) {
        WorldOpticalNet net = serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldOpticalNet netx = new WorldOpticalNet();
            netx.load(tag);
            return netx;
        }, WorldOpticalNet::new, DATA_ID);
        net.setLevel(serverLevel);
        return net;
    }

    public WorldOpticalNet() {
        super(false, SinglePathAlgorithm::new);
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
        return new IPipeCapabilityObject[] { new DataCapabilityObject(this) };
    }

    @Override
    public int getNetworkID() {
        return 4;
    }
}
