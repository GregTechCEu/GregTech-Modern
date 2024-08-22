package com.gregtechceu.gtceu.api.graphnet;

import com.gregtechceu.gtceu.api.graphnet.alg.AlgorithmBuilder;
import com.gregtechceu.gtceu.api.graphnet.alg.NetAlgorithmWrapper;
import com.gregtechceu.gtceu.api.graphnet.alg.NetPathMapper;
import com.gregtechceu.gtceu.api.graphnet.alg.iter.IteratorFactory;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.graph.INetGraph;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

/**
 * The bridge between JGraphT graphs and graphnet abstractions.
 * Doesn't do any automatic linking, weighting, predicating, etc. Simply handles storing the JGraphT graph to disk,
 * interfacing with graph algorithms, and interacting with the JGraphT graph.
 */
public final class GraphNetBacker {

    private final IGraphNet backedNet;
    private final INetGraph pipeGraph;
    private final Object2ObjectOpenHashMap<Object, GraphVertex> vertexMap;
    private final NetAlgorithmWrapper[] netAlgorithms;

    public GraphNetBacker(IGraphNet backedNet, INetGraph graph,
                          AlgorithmBuilder @NotNull... algorithmBuilders) {
        this.backedNet = backedNet;
        this.pipeGraph = graph;
        this.netAlgorithms = new NetAlgorithmWrapper[algorithmBuilders.length];
        for (int i = 0; i < algorithmBuilders.length; i++) {
            this.netAlgorithms[i] = new NetAlgorithmWrapper(backedNet, algorithmBuilders[i],
                    backedNet.supportsPredication() || backedNet.usesDynamicWeights(i));
        }
        this.vertexMap = new Object2ObjectOpenHashMap<>();
    }

    public IGraphNet getBackedNet() {
        return backedNet;
    }

    public void addNode(NetNode node) {
        GraphVertex vertex = new GraphVertex(node);
        getGraph().addVertex(vertex);
        this.vertexMap.put(node.getEquivalencyData(), vertex);
        backedNet.setDirty();
    }

    @Nullable
    public NetNode getNode(Object equivalencyData) {
        GraphVertex vertex = this.vertexMap.get(equivalencyData);
        return vertex == null ? null : vertex.wrapped;
    }

    public boolean removeNode(@Nullable NetNode node) {
        if (node != null) {
            if (!this.getGraph().containsVertex(node.wrapper)) {
                // edge case -- the node's group most likely still has this node registered,
                // but the node doesn't actually exist in the graph.
                // no idea what causes this, but it happens.
                NetGroup group = node.getGroupUnsafe();
                if (group != null) group.removeNode(node);
            }
            if (!this.getGraph().edgesOf(node.wrapper).isEmpty()) this.invalidateAlgs();
            NetGroup group = node.getGroupUnsafe();
            if (group != null) {
                group.splitNode(node);
            } else this.removeVertex(node.wrapper);
            backedNet.setDirty();
            return true;
        } else return false;
    }

    @ApiStatus.Internal
    public void removeVertex(GraphVertex vertex) {
        if (this.getGraph().removeVertex(vertex)) {
            this.vertexMap.remove(vertex.wrapped.getEquivalencyData());
            vertex.wrapped.onRemove();
            backedNet.setDirty();
        }
    }

    @Nullable
    public NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, double weight) {
        if (!NetGroup.isEdgeAllowed(source, target)) return null;
        GraphEdge graphEdge = getGraph().addEdge(source.wrapper, target.wrapper);
        if (graphEdge != null) {
            getGraph().setEdgeWeight(graphEdge, weight);
            NetGroup.mergeEdge(source, target);
            backedNet.setDirty();
        }
        return graphEdge == null ? null : graphEdge.wrapped;
    }

    @Nullable
    public NetEdge getEdge(@NotNull NetNode source, @NotNull NetNode target) {
        GraphEdge graphEdge = getGraph().getEdge(source.wrapper, target.wrapper);
        return graphEdge == null ? null : graphEdge.wrapped;
    }

    public boolean removeEdge(@NotNull NetNode source, NetNode target) {
        NetGroup group = source.getGroupUnsafe();
        if (group == null) {
            // weird since there should always be a group for two joined nodes, but whatever
            return removeEdge(source.wrapper, target.wrapper) != null;
        } else return group.splitEdge(source, target);
    }

    @ApiStatus.Internal
    public GraphEdge removeEdge(GraphVertex source, GraphVertex target) {
        GraphEdge edge = this.getGraph().removeEdge(source, target);
        if (edge != null) {
            backedNet.setDirty();
        }
        return edge;
    }

    @ApiStatus.Internal
    public boolean removeEdge(GraphEdge edge) {
        if (this.getGraph().removeEdge(edge)) {
            backedNet.setDirty();
            return true;
        }
        return false;
    }

    /**
     * Note - if an error is thrown with a stacktrace descending from this method,
     * most likely a bad remapper was passed in. <br>
     * This method should never be exposed outside the net this backer is backing due to this fragility.
     */
    public <Path extends INetPath<?, ?>> Iterator<Path> getPaths(@Nullable NetNode node, int algorithmID,
                                                                 @NotNull NetPathMapper<Path> remapper,
                                                                 IPredicateTestObject testObject,
                                                                 @Nullable SimulatorKey simulator, long queryTick) {
        if (node == null) return Collections.emptyIterator();
        this.getGraph().setupInternal(this, backedNet.usesDynamicWeights(algorithmID));

        Iterator<? extends INetPath<?, ?>> cache = node.getPathCache(testObject, simulator, queryTick);
        if (cache != null) return (Iterator<Path>) cache;

        IteratorFactory<Path> factory = this.netAlgorithms[algorithmID]
                .getPathsIterator(node.wrapper, remapper, testObject, simulator, queryTick);
        if (factory.cacheable()) {
            return (Iterator<Path>) (node.setPathCache(factory).getPathCache(testObject, simulator, queryTick));
        } else return factory.newIterator(getGraph(), testObject, simulator, queryTick);
    }

    public void invalidateAlg(int algorithmID) {
        this.netAlgorithms[algorithmID].invalidate();
    }

    public void invalidateAlgs() {
        for (NetAlgorithmWrapper netAlgorithm : this.netAlgorithms) {
            netAlgorithm.invalidate();
        }
    }

    public INetGraph getGraph() {
        return pipeGraph;
    }

    // PROPOSAL FOR ALTERNATIVE STORAGE MECHANISM TO REDUCE MEMORY COSTS
    // > Always loaded & nbt stored data:
    // map & weak map of group ids to groups. No references to group objects exist outside of this, only references to
    // grou ids.
    // (for pipenet) pipes store a reference to their group id
    // > Disk-stored data:
    // contents of groups, specifically their nodes and edges.
    // > Impl (for pipenet)
    // When a pipe is loaded, it goes fetch its group and tells it the pipe's chunk. This chunk is added to a *set* of
    // chunks that are 'loading' this group.
    // When a chunk is unloaded, it is removed from the set of 'loading' chunks for all groups.
    // When the set of 'loading' chunks for a group is empty, the group writes its data to disk and removes itself from
    // the map and the graph but not the weak map.
    // (proposal - create a graph impl that allows for weak references to vertices and edges, in order to remove need
    // for explicit removal of group from graph?)
    // When a pipe fetches its group, if the group is not found in the map, it then checks the weak map.
    // If found in the weak map, the pipe's chunk is added to the 'loading' chunks and a reference to the group is added
    // to the map and the contents are added to the graph.
    // If not found in the weak map, the group is instead read from disk and initialized.
    // > Benefits of this Impl
    // By only loading the (potentially) large number of edges and nodes into the graph that a group contains when that
    // group is needed,
    // the number of unnecessary references in the graphnet on, say, a large server is drastically reduced.
    // however, since there are necessarily more read/write actions to disk, the cpu load would increase in turn.

    public void readFromNBT(@NotNull CompoundTag nbt) {
        // construct map of node ids -> nodes, while building nodes to groups
        // construct edges using map
        Int2ObjectOpenHashMap<NetGroup> groupMap = new Int2ObjectOpenHashMap<>();
        ListTag vertices = nbt.getList("Vertices", Tag.TAG_COMPOUND);
        int vertexCount = vertices.size();
        Int2ObjectOpenHashMap<GraphVertex> vertexMap = new Int2ObjectOpenHashMap<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            CompoundTag tag = vertices.getCompound(i);
            NetNode node = this.backedNet.getNewNode();
            node.deserializeNBT(tag);
            if (tag.contains("GroupID")) {
                int id = tag.getInt("GroupID");
                NetGroup group = groupMap.get(id);
                if (group == null) {
                    group = new NetGroup(this.backedNet);
                    groupMap.put(id, group);
                }
                group.addNode(node);
            }
            GraphVertex vertex = new GraphVertex(node);
            this.getGraph().addVertex(vertex);
            vertexMap.put(i, vertex);
            this.vertexMap.put(node.getEquivalencyData(), vertex);
        }

        ListTag edges = nbt.getList("Edges", Tag.TAG_COMPOUND);
        int edgeCount = edges.size();
        for (int i = 0; i < edgeCount; i++) {
            CompoundTag tag = edges.getCompound(i);
            GraphEdge graphEdge = this.getGraph().addEdge(vertexMap.get(tag.getInt("SourceID")),
                    vertexMap.get(tag.getInt("TargetID")));
            this.getGraph().setEdgeWeight(graphEdge, tag.getDouble("Weight"));
            graphEdge.wrapped.deserializeNBT(tag);
        }
    }

    @Contract("_ -> param1")
    public @NotNull CompoundTag writeToNBT(CompoundTag compound) {
        // map of net groups -> autogenerated ids;
        // tag of autogenerated vertex ids -> node nbt & group id
        // tag of autogenerated edge ids -> edge nbt & source/target ids
        Object2IntOpenHashMap<NetGroup> groupMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<GraphVertex> vertexMap = new Object2IntOpenHashMap<>();
        int i = 0;
        int g = 0;
        ListTag vertices = new ListTag();
        for (GraphVertex graphVertex : this.getGraph().vertexSet()) {
            vertexMap.put(graphVertex, i);
            NetGroup group = graphVertex.wrapped.getGroupUnsafe();
            CompoundTag tag = graphVertex.wrapped.serializeNBT();
            if (group != null) {
                int groupID;
                if (!groupMap.containsKey(group)) {
                    groupMap.put(group, g);
                    groupID = g;
                    g++;
                } else groupID = groupMap.getInt(group);
                tag.putInt("GroupID", groupID);
            }
            vertices.add(tag);
            i++;
        }
        compound.put("Vertices", vertices);

        ListTag edges = new ListTag();
        for (GraphEdge graphEdge : this.getGraph().edgeSet()) {
            CompoundTag tag = graphEdge.wrapped.serializeNBT();
            tag.putInt("SourceID", vertexMap.getInt(graphEdge.getSource()));
            tag.putInt("TargetID", vertexMap.getInt(graphEdge.getTarget()));
            tag.putDouble("Weight", graphEdge.getWeight());
            edges.add(tag);
        }
        compound.put("Edges", edges);

        return compound;
    }
}
