package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.alg.NetPathMapper;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;
import com.gregtechceu.gtceu.api.graphnet.path.AbstractNetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FlowWorldPipeNetPath extends AbstractNetPath<WorldPipeNetNode, AbstractNetFlowEdge> {

    public static final NetPathMapper<FlowWorldPipeNetPath> MAPPER = new NetPathMapper<>(FlowWorldPipeNetPath::new,
            FlowWorldPipeNetPath::new, FlowWorldPipeNetPath::new);

    public FlowWorldPipeNetPath(GraphVertex vertex) {
        this(Collections.singletonList(vertex), Collections.emptyList(),
                vertex.wrapped.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue());
    }

    public FlowWorldPipeNetPath(List<GraphVertex> vertices, List<GraphEdge> edges, double weight) {
        super(vertices.stream().map(v -> (WorldPipeNetNode) v.wrapped).collect(Collectors.toList()),
                edges.stream().map(e -> (AbstractNetFlowEdge) e.wrapped).collect(Collectors.toList()), weight);
    }

    public FlowWorldPipeNetPath(GraphPath<GraphVertex, GraphEdge> path) {
        this(path.getVertexList(), path.getEdgeList(), path.getWeight());
    }

    public interface Provider {

        Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                @Nullable SimulatorKey simulator, long queryTick);
    }
}
