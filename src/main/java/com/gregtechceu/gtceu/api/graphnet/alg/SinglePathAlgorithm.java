package com.gregtechceu.gtceu.api.graphnet.alg;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.alg.iter.IteratorFactory;
import com.gregtechceu.gtceu.api.graphnet.alg.iter.SimpleIteratorFactories;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Iterator;
import java.util.List;

public final class SinglePathAlgorithm implements INetAlgorithm {

    private final IGraphNet net;
    private final boolean recomputeEveryCall;

    public SinglePathAlgorithm(IGraphNet pipenet, boolean recomputeEveryCall) {
        this.net = pipenet;
        this.recomputeEveryCall = recomputeEveryCall;
    }

    @Override
    public <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                                       NetPathMapper<Path> remapper) {
        if (recomputeEveryCall) {
            return (graph, testObject, simulator, queryTick) -> {
                graph.prepareForAlgorithmRun(testObject, simulator, queryTick);
                List<GraphEdge> graphEdges = new ObjectArrayList<>();
                List<GraphVertex> nodes = new ObjectArrayList<>();
                Results results = compute(source, nodes, graphEdges);
                if (!results.valid()) return SimpleIteratorFactories.getSingletonIterator(null);
                if (graphEdges.isEmpty()) return SimpleIteratorFactories.getSingletonIterator(remapper.map(source));
                return SimpleIteratorFactories.getSingletonIterator(remapper.map(nodes, graphEdges, results.weight()));
            };
        } else {
            List<GraphEdge> graphEdges = new ObjectArrayList<>();
            List<GraphVertex> nodes = new ObjectArrayList<>();
            Results results = compute(source, nodes, graphEdges);
            if (!results.valid()) return SimpleIteratorFactories.emptyFactory();
            if (graphEdges.isEmpty()) return SimpleIteratorFactories.fromSingleton(remapper.map(source));
            return SimpleIteratorFactories.fromSingleton(remapper.map(nodes, graphEdges, results.weight()));
        }
    }

    private Results compute(GraphVertex source,
                            List<GraphVertex> nodes, List<GraphEdge> graphEdges) {
        nodes.add(source);
        GraphVertex lastNode = null;
        GraphVertex node = source;
        GraphEdge graphEdge;
        double sumWeight = 0;
        boolean valid = true;
        while (valid) {
            Iterator<GraphEdge> i = this.net.getGraph().outgoingEdgesOf(node).iterator();
            if (!i.hasNext()) break; // we've reached the end, exit the loop while still valid
            graphEdge = i.next();
            // if we are directed, we know that the target is the target.
            // if we aren't directed, we need to see if the graphEdge's target is secretly the source
            boolean reversedEdge = !this.net.getGraph().isDirected() && graphEdge.getTarget() == node;
            if ((!reversedEdge && graphEdge.getTarget() == lastNode) ||
                    (reversedEdge && graphEdge.getSource() == lastNode)) {
                // current edge points to a previous node, either get the other edge or exit safely.
                if (i.hasNext()) {
                    graphEdge = i.next();
                    reversedEdge = !this.net.getGraph().isDirected() && graphEdge.getTarget() == node;
                    // we know that the new edge cannot point to the previous node
                } else break; // we've reached the end, exit the loop while still valid
            } else if (i.hasNext()) i.next(); // remove the second edge, if it exists.
            if (i.hasNext()) valid = false; // third graphEdge detected - that's an invalid group
            lastNode = node;
            node = reversedEdge ? graphEdge.getSource() : graphEdge.getTarget();
            graphEdges.add(graphEdge);
            nodes.add(node);
            sumWeight += getWeight(graphEdge);
        }
        return new Results(sumWeight, valid);
    }

    private double getWeight(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeWeight(graphEdge);
    }

    private record Results(double weight, boolean valid) {}
}
