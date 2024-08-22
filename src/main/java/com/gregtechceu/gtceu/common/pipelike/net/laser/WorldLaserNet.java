package com.gregtechceu.gtceu.common.pipelike.net.laser;

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

public class WorldLaserNet extends WorldPipeNet implements BasicWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GTCapability.CAPABILITY_LASER };

    private static final String DATA_ID = "gtceu_world_laser_net";

    public static WorldLaserNet getWorldNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldLaserNet net = new WorldLaserNet();
            net.load(tag);
            return net;
        }, WorldLaserNet::new, DATA_ID);
    }

    public WorldLaserNet() {
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
        return new IPipeCapabilityObject[] { new LaserCapabilityObject(this) };
    }

    @Override
    public int getNetworkID() {
        return 3;
    }
}
