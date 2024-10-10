package com.gregtechceu.gtceu.api.graphnet.alg;

import com.gregtechceu.gtceu.api.graphnet.alg.iter.IteratorFactory;
import com.gregtechceu.gtceu.api.graphnet.graph.GraphVertex;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

public interface INetAlgorithm {

    <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                                NetPathMapper<Path> remapper);
}
