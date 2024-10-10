package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

public interface IRoundRobinTraverseData<T extends IRoundRobinData<N>, N extends NetNode, P extends INetPath<N, ?>>
                                        extends ITraverseData<N, P> {

    /**
     * The traversal cache must be cached and persistent between traversals,
     * but not modified by anything external to {@link TraverseHelpers}.
     * The traversal cache is (hopefully) modified deterministically between simulated and nonsimulated transfers, but
     * remember that <b>modification during simulation must not be reflected on the cache used for nonsimulation.</b>
     * The easiest way to accomplish this is to provide a cloned {@link ArrayDeque} during simulated transfers.
     *
     * @return the traversal cache.
     */
    @NotNull
    Object2ObjectLinkedOpenHashMap<Object, T> getTraversalCache();

    /**
     * Whether a path should be skipped before checking it against the round robin cache.
     * The return of {@link ITraverseData#prepareForPathWalk(INetPath, long)} will be ignored during traversal.
     */
    boolean shouldSkipPath(@NotNull P path);

    /**
     * @return The {@link IRoundRobinData} for the particular destination. Will be mutated; should then be referenced
     *         in {@link ITraverseData#finalizeAtDestination(NetNode, long)} to do proper round robin within the
     *         destination.
     */
    @NotNull
    T createRRData(@NotNull N destination);

    /**
     * Called in preference to {@link ITraverseData#finalizeAtDestination(NetNode, long)} to provide the round robin
     * data for the destination.
     */
    long finalizeAtDestination(@NotNull T data, @NotNull N destination, long flowReachingDestination);

    /**
     * @deprecated use {@link #finalizeAtDestination(IRoundRobinData, NetNode, long)} instead.
     */
    @Override
    @Deprecated
    long finalizeAtDestination(@NotNull N destination, long flowReachingDestination);
}
