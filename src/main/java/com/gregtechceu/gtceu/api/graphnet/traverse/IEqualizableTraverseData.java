package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

import org.jetbrains.annotations.NotNull;

public interface IEqualizableTraverseData<N extends NetNode, P extends INetPath<N, ?>> extends ITraverseData<N, P> {

    int getDestinationsAtNode(@NotNull N node);

    /**
     * Whether a path should be skipped before running the collection process on it.
     * The return of {@link ITraverseData#prepareForPathWalk(INetPath, long)} will be ignored during traversal.
     */
    boolean shouldSkipPath(@NotNull P path);

    /**
     * Should return how much flow the destination with the smallest maximum allowed flow among destinations at
     * this node requires.
     */
    long getMaxFlowToLeastDestination(@NotNull N destination);

    /**
     * Called in preference to {@link ITraverseData#finalizeAtDestination(NetNode, long)} to provide the equalization.
     */
    long finalizeAtDestination(@NotNull N node, long flowReachingNode, int expectedDestinations);

    /**
     * @deprecated use {@link #finalizeAtDestination(NetNode, long, int)} instead.
     */
    @Override
    @Deprecated
    long finalizeAtDestination(@NotNull N destination, long flowReachingDestination);
}
