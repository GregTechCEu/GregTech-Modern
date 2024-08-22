package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.NetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.predicate.BlockedPredicate;
import com.gregtechceu.gtceu.api.graphnet.pipenet.predicate.FilterPredicate;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.pipelike.net.item.WorldItemNet;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldFluidNet extends WorldPipeNet implements FlowWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { ForgeCapabilities.FLUID_HANDLER };

    private static final String DATA_ID = "gtceu_world_fluid_net";

    public static WorldFluidNet getWorldNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldFluidNet net = new WorldFluidNet();
            net.load(tag);
            return net;
        }, WorldFluidNet::new, DATA_ID);
    }

    public WorldFluidNet() {
        super(false, DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    public boolean supportsPredication() {
        return true;
    }

    @Override
    protected void coverPredication(@NotNull NetEdge edge, @Nullable CoverBehavior a, @Nullable CoverBehavior b) {
        super.coverPredication(edge, a, b);
        if (edge.getPredicateHandler().hasPredicate(BlockedPredicate.INSTANCE)) return;
        FilterPredicate predicate = null;
        if (a instanceof PumpCover filter) {
            if (filter.getManualIOMode() == ManualIOMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.INSTANCE);
                return;
            } else if (filter.getManualIOMode() == ManualIOMode.FILTERED &&
                    filter.getIo() != IO.IN) {
                        predicate = FilterPredicate.INSTANCE.getNew();
                        predicate.setSourceFilter(filter.getFilterHandler().getFilter());
                    }
        }
        if (b instanceof PumpCover filter) {
            if (filter.getManualIOMode() == ManualIOMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.INSTANCE);
                return;
            } else if (filter.getManualIOMode() == ManualIOMode.FILTERED &&
                    filter.getIo() != IO.OUT) {
                        if (predicate == null) predicate = FilterPredicate.INSTANCE.getNew();
                        predicate.setTargetFilter(filter.getFilterHandler().getFilter());
                    }
        }
        if (predicate != null) edge.getPredicateHandler().setPredicate(predicate);
    }

    @Override
    public boolean usesDynamicWeights(int algorithmID) {
        return true;
    }

    @Override
    public boolean clashesWith(IGraphNet net) {
        return net instanceof WorldItemNet;
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new FluidCapabilityObject(this, node) };
    }

    @Override
    public Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                   @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, FlowWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public @NotNull NetFlowEdge getNewEdge() {
        return new NetFlowEdge(10);
    }

    @Override
    public int getNetworkID() {
        return 1;
    }
}
