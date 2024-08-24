package com.gregtechceu.gtceu.common.pipelike.net.item;

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
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.pipelike.net.fluid.WorldFluidNet;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldItemNet extends WorldPipeNet implements FlowWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] {
            ForgeCapabilities.ITEM_HANDLER };

    private static final String DATA_ID = "gtceu_world_item_net";

    public static WorldItemNet getWorldNet(ServerLevel serverLevel) {
        WorldItemNet net = serverLevel.getDataStorage().computeIfAbsent(tag -> {
            WorldItemNet netx = new WorldItemNet();
            netx.load(tag);
            return netx;
        }, WorldItemNet::new, DATA_ID);
        net.setLevel(serverLevel);
        return net;
    }

    public WorldItemNet() {
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
        if (a instanceof ConveyorCover filter) {
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
        if (b instanceof ConveyorCover filter) {
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
        return net instanceof WorldFluidNet;
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new ItemCapabilityObject(this, node) };
    }

    @Override
    public Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                   @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, FlowWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public @NotNull NetFlowEdge getNewEdge() {
        return new NetFlowEdge(2, 5);
    }

    @Override
    public int getNetworkID() {
        return 2;
    }
}
