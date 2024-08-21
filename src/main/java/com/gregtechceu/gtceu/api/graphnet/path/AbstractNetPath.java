package com.gregtechceu.gtceu.api.graphnet.path;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractNetPath<N extends NetNode, E extends NetEdge> implements INetPath<N, E> {

    protected final List<N> nodes;

    protected final List<E> edges;

    protected final double weight;

    protected NetLogicData unifiedNodeData;
    protected NetLogicData unifiedEdgeData;

    public AbstractNetPath(List<N> nodes, List<E> edges, double weight) {
        this.nodes = nodes;
        this.edges = edges;
        this.weight = weight;
    }

    @Override
    public List<N> getOrderedNodes() {
        return nodes;
    }

    @Override
    public List<E> getOrderedEdges() {
        return edges;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public NetLogicData getUnifiedNodeData() {
        if (unifiedNodeData == null) {
            if (nodes.size() == 1) {
                unifiedNodeData = nodes.get(0).getData();
            } else if (nodes.size() == 2) {
                unifiedNodeData = NetLogicData.union(nodes.get(0).getData(), nodes.get(1).getData());
            } else {
                unifiedNodeData = NetLogicData.union(nodes.get(0).getData(),
                        nodes.stream().skip(1).map(NetNode::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedNodeData;
    }

    @Override
    @Nullable
    public NetLogicData getUnifiedEdgeData() {
        if (unifiedEdgeData == null) {
            if (edges.size() == 0) {
                return null;
            } else if (edges.size() == 1) {
                unifiedEdgeData = edges.get(0).getData();
            } else if (edges.size() == 2) {
                unifiedEdgeData = NetLogicData.union(edges.get(0).getData(), edges.get(1).getData());
            } else {
                unifiedEdgeData = NetLogicData.union(edges.get(0).getData(),
                        edges.stream().skip(1).map(NetEdge::getData).toArray(NetLogicData[]::new));
            }
        }
        return unifiedEdgeData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNetPath<?, ?> that = (AbstractNetPath<?, ?>) o;
        return Double.compare(that.weight, weight) == 0 && Objects.equals(nodes, that.nodes) &&
                Objects.equals(edges, that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges, weight);
    }
}
