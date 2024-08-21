package com.gregtechceu.gtceu.api.graphnet.alg;

import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import gregtech.api.util.function.TriFunction;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.GraphPath;

import java.util.List;
import java.util.function.Function;

public class NetPathMapper<Path extends INetPath<?, ?>> {

    private final Function<GraphVertex, Path> singlePathMapper;
    private final TriFunction<List<GraphVertex>, List<GraphEdge>, @NotNull Double, Path> fullConstructMapper;
    private final Function<GraphPath<GraphVertex, GraphEdge>, Path> graphPathMapper;

    public NetPathMapper(Function<GraphVertex, Path> singlePathMapper,
                         TriFunction<List<GraphVertex>, List<GraphEdge>, @NotNull Double, Path> fullConstructMapper,
                         Function<GraphPath<GraphVertex, GraphEdge>, Path> graphPathMapper) {
        this.singlePathMapper = singlePathMapper;
        this.fullConstructMapper = fullConstructMapper;
        this.graphPathMapper = graphPathMapper;
    }

    public Path map(GraphVertex graphVertex) {
        return singlePathMapper.apply(graphVertex);
    }

    public Path map(List<GraphVertex> vertices, List<GraphEdge> graphEdges, double weight) {
        return fullConstructMapper.apply(vertices, graphEdges, weight);
    }

    public Path map(GraphPath<GraphVertex, GraphEdge> graphPath) {
        return graphPathMapper.apply(graphPath);
    }
}
