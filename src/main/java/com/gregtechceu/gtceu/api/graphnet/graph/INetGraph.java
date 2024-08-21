package com.gregtechceu.gtceu.api.graphnet.graph;

import com.gregtechceu.gtceu.api.graphnet.GraphNetBacker;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.ApiStatus;
import org.jgrapht.Graph;

public interface INetGraph extends Graph<GraphVertex, GraphEdge> {

    void prepareForAlgorithmRun(IPredicateTestObject testObject, SimulatorKey simulator, long queryTick);

    boolean isDirected();

    /**
     * This should only be called by {@link GraphNetBacker}
     */
    @ApiStatus.Internal
    void setupInternal(GraphNetBacker backer, boolean dynamicWeights);
}
