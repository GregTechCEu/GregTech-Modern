package com.gregtechceu.gtceu.api.graphnet.alg;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface AlgorithmBuilder {

    @NotNull
    INetAlgorithm build(@NotNull IGraphNet net, boolean recomputeEveryCall);
}
