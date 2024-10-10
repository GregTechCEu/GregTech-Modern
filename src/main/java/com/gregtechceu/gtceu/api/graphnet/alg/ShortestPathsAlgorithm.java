package com.gregtechceu.gtceu.api.graphnet.alg;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.alg.iter.IteratorFactory;
import com.gregtechceu.gtceu.api.graphnet.alg.iter.SimpleIteratorFactories;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;
import org.jgrapht.util.ConcurrencyUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public final class ShortestPathsAlgorithm extends CHManyToManyShortestPaths<GraphVertex, GraphEdge>
                                          implements INetAlgorithm {

    private static final ThreadPoolExecutor BACKGROUND_EXECUTOR = ConcurrencyUtil.createThreadPoolExecutor(2);

    private final boolean recomputeEveryCall;

    public ShortestPathsAlgorithm(IGraphNet net, boolean recomputeEveryCall) {
        super(net.getGraph(), BACKGROUND_EXECUTOR);
        this.recomputeEveryCall = recomputeEveryCall;
    }

    @Override
    public <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                                       NetPathMapper<Path> remapper) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        // if the source has no group, it has no paths other than the path to itself.
        if (source.wrapped.getGroupUnsafe() == null) {
            Path path = remapper.map(source);
            return SimpleIteratorFactories.fromSingleton(path);
        }

        Set<GraphVertex> searchSpace = source.wrapped.getGroupSafe().getNodes().stream().filter(NetNode::isActive)
                .map(n -> n.wrapper).filter(node -> !source.equals(node) && graph.containsVertex(node))
                .collect(Collectors.toSet());
        Set<GraphVertex> singleton = Collections.singleton(source);
        if (recomputeEveryCall) {
            return (graph1, testObject, simulator, queryTick) -> {
                graph1.prepareForAlgorithmRun(testObject, simulator, queryTick);
                ManyToManyShortestPaths<GraphVertex, GraphEdge> manyToManyPaths = getManyToManyPaths(singleton,
                        searchSpace);
                return searchSpace.stream().map(node -> manyToManyPaths.getPath(source, node))
                        .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight)).iterator();
            };
        } else {
            ManyToManyShortestPaths<GraphVertex, GraphEdge> manyToManyPaths = getManyToManyPaths(singleton,
                    searchSpace);
            return SimpleIteratorFactories.fromIterable(searchSpace.stream()
                    .map(node -> manyToManyPaths.getPath(source, node))
                    .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight))
                    .collect(Collectors.toList()));
        }
    }
}
