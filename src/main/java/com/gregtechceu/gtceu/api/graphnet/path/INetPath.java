package com.gregtechceu.gtceu.api.graphnet.path;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.ToleranceDoubleComparator;

import java.util.List;

public interface INetPath<N extends NetNode, T extends NetEdge> {

    ToleranceDoubleComparator WEIGHT_COMPARATOR = new ToleranceDoubleComparator();

    List<N> getOrderedNodes();

    default N getSourceNode() {
        return getOrderedNodes().get(0);
    }

    default N getTargetNode() {
        List<N> nodes = getOrderedNodes();
        return nodes.get(nodes.size() - 1);
    }

    List<T> getOrderedEdges();

    double getWeight();

    default boolean matches(@NotNull INetPath<?, ?> other) {
        return WEIGHT_COMPARATOR.compare(getWeight(), other.getWeight()) == 0 &&
                getOrderedNodes().equals(other.getOrderedNodes()) && getOrderedEdges().equals(other.getOrderedEdges());
    }

    NetLogicData getUnifiedNodeData();

    @Nullable
    NetLogicData getUnifiedEdgeData();
}
