package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.ReversibleLossOperator;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITraverseData<N extends NetNode, P extends INetPath<N, ?>> {

    IGraphNet getGraphNet();

    IPredicateTestObject getTestObject();

    @Nullable
    SimulatorKey getSimulatorKey();

    long getQueryTick();

    /**
     * Called before walking the next path. Should reset per-path logics to prepare.
     *
     * @param path the next path
     * @param flow how much flow can be provided to the path.
     * @return whether the path should be skipped
     */
    boolean prepareForPathWalk(@NotNull P path, long flow);

    /**
     * Reports that the traverse is traversing to a node, for additional logic to be run.
     *
     * @param node             the node being traversed
     * @param flowReachingNode the flow that has reached this node.
     * @return the loss operator for the node.
     */
    ReversibleLossOperator traverseToNode(@NotNull N node, long flowReachingNode);

    /**
     * Reports that the traverse has finished a path walk, for finalization.
     *
     * @param destination             the active node the path terminated at.
     * @param flowReachingDestination the flow that reached the destination
     * @return the amount of flow that should be consumed, before walking the next path.
     */
    long finalizeAtDestination(@NotNull N destination, long flowReachingDestination);

    /**
     * Allows for reporting a smaller capacity along an edge than it actually has. Do not report a larger capacity
     * than the actual edge or things will break.
     * 
     * @param edge the edge to get capacity for.
     * @return a non-negative capacity that is less than or equal to the true capacity of the edge.
     */
    default long getFlowLimit(@NotNull AbstractNetFlowEdge edge) {
        return edge.getFlowLimit(this.getTestObject(), this.getGraphNet(), this.getQueryTick(), this.getSimulatorKey());
    }

    /**
     * Allows for consuming more than just the edge flow limits on a consumption event. Must always consume the correct
     * amount of edge flow or things will break.
     *
     * @param edge        the edge to consume along.
     * @param targetNode  the target node of the edge.
     * @param consumption the amount to consume from the edge's flow limit.
     */
    @MustBeInvokedByOverriders
    default void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode,
                                  long consumption) {
        edge.consumeFlowLimit(this.getTestObject(), this.getGraphNet(), consumption, this.getQueryTick(),
                this.getSimulatorKey());
    }
}
