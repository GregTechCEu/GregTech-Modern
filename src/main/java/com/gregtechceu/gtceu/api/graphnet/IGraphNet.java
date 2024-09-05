package com.gregtechceu.gtceu.api.graphnet;

import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.graph.INetGraph;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Iterator;

public interface IGraphNet {

    /**
     * Controls whether dynamic weight lookup will be used.
     * Dynamic weight lookup can be more expensive, so this should not be enabled unless necessary.
     */
    default boolean usesDynamicWeights(int algorithmID) {
        return false;
    }

    /**
     * Controls whether predication of edges is allowed for this net. When false, path caching is improved by
     * skipping the recomputation required in order to compensate for predication.
     * 
     * @return whether predication should be allowed for this net.
     */
    default boolean supportsPredication() {
        return false;
    }

    /**
     * Adds a node to the graphnet.
     * 
     * @param node The node to add.
     */
    void addNode(@NotNull NetNode node);

    /**
     * Gets the net node with the given equivalency data, if one exists.
     * 
     * @param equivalencyData the equivalency data to match.
     * @return the matching net node, if one exists.
     */
    @Nullable
    NetNode getNode(@NotNull Object equivalencyData);

    /**
     * Removes a node from the graphnet.
     * 
     * @param node The node to remove.
     */
    void removeNode(@NotNull NetNode node);

    /**
     * Links two nodes by an edge.
     * 
     * @param source   Source node.
     * @param target   Target node.
     * @param bothWays If the graph is directional, passing in true will create both the forwards and backwards edge.
     * @return the created edge, if it was created. Returns null if bothWays is set to true.
     */
    @Nullable
    @Contract("_, _, false -> _; _, _, true -> null")
    NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays);

    /**
     * Returns the edge linking two nodes together, if one exists.
     * 
     * @param source Source node.
     * @param target Target node.
     * @return the linking edge, if one exists.
     */
    @Nullable
    NetEdge getEdge(@NotNull NetNode source, @NotNull NetNode target);

    /**
     * Removes the edge linking two nodes together, if one exists.
     * 
     * @param source   Source node.
     * @param target   Target node.
     * @param bothWays If the graph is directional, passing in true will remove both the forwards and backwards edge.
     */
    void removeEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays);

    /**
     * Gets the {@link INetGraph} backing this graphnet. This should NEVER be modified directly, but can be queried.
     * 
     * @return the backing net graph
     */
    @ApiStatus.Internal
    default INetGraph getGraph() {
        return getBacker().getGraph();
    }

    /**
     * Gets the {@link GraphNetBacker} backing this graphnet. This should NEVER be used except inside the graphnet impl.
     * 
     * @return the backing graphnet backer
     */
    @ApiStatus.Internal
    GraphNetBacker getBacker();

    /**
     * Get a blank group data for this graph. <br>
     * Make sure to override this if your NetGroups use data.
     *
     * @return The correct data variant.
     */
    @Nullable
    default AbstractGroupData getBlankGroupData() {
        return null;
    }

    /**
     * Get a default node data for this graph. Generally used for immediate nbt deserialization.
     * 
     * @return A default node data object.
     */
    @NotNull
    default NetLogicData getDefaultNodeData() {
        return new NetLogicData().setLogicEntry(WeightFactorLogic.TYPE.getNew().getWith(1));
    }

    /**
     * Returns whether a node exists in this graph.
     * 
     * @param node the node in question.
     * @return whether the node exists.
     */
    default boolean containsNode(NetNode node) {
        return getGraph().containsVertex(node.wrapper);
    }

    /**
     * Returns a breadth-first iterator through this graph, starting from the passed in node.
     * 
     * @param node the node to start from.
     * @return a breadth-first iterator through this graph.
     */
    @NotNull
    default Iterator<NetNode> breadthIterator(NetNode node) {
        return new Iterator<>() {

            private final BreadthFirstIterator<GraphVertex, GraphEdge> iterator = new BreadthFirstIterator<>(getGraph(),
                    node.wrapper);

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public NetNode next() {
                return iterator.next().wrapped;
            }
        };
    }

    /**
     * Used in {@link MultiNodeHelper} to determine if a node can be traversed, based on the nets that have been
     * recently traversed in the {@link MultiNodeHelper}.
     * 
     * @param net a recently traversed net
     * @return if node traversal should be blocked.
     */
    default boolean clashesWith(IGraphNet net) {
        return false;
    }

    /**
     * @return the class all registered nodes are expected to be children of.
     */
    Class<? extends NetNode> getNodeClass();

    /**
     * While this is crude, it does allow for avoiding generics literally everywhere.
     * The systems that make up a graphnet intertwine such that generics would be needed in basically every class.
     * Basically, instead of a bunch of generics everywhere, we just instate an honor system that crashes the game if
     * you violate it.
     */
    default void nodeClassCheck(NetNode node) {
        if (!(getNodeClass().isInstance(node)))
            throw new IllegalArgumentException("Cannot provide a " + this.getClass().getSimpleName() +
                    " with a " + node.getClass().getSimpleName() + " node!");
    }

    /**
     * @return a new node with no data, to be either nbt deserialized or initialized in some other way.
     */
    @NotNull
    NetNode getNewNode();

    /**
     * @return a new edge with no data, to be either nbt deserialized or initialized in some other way.
     */
    @NotNull
    default NetEdge getNewEdge() {
        return new NetEdge();
    }

    /**
     * Should only be used by the internal {@link GraphNetBacker} backing this graphnet.
     */
    @ApiStatus.Internal
    void setDirty();
}
