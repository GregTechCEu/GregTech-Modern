package com.gregtechceu.gtceu.api.graphnet.graph;

import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;

public final class GraphEdge extends DefaultWeightedEdge {

    public final @NotNull NetEdge wrapped;

    public GraphEdge(@NotNull NetEdge wrapped) {
        this.wrapped = wrapped;
        wrapped.wrapper = this;
    }

    public @NotNull NetEdge getWrapped() {
        return wrapped;
    }

    @Override
    public GraphVertex getSource() {
        return (GraphVertex) super.getSource();
    }

    @Override
    public GraphVertex getTarget() {
        return (GraphVertex) super.getTarget();
    }

    /**
     * Use this very sparingly. It's significantly better to go through {@link org.jgrapht.Graph#getEdgeWeight(Object)}
     * instead, unless you are doing nbt serialization for example.
     * 
     * @return the edge weight.
     */
    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEdge graphEdge = (GraphEdge) o;
        return Objects.equals(wrapped, graphEdge.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }
}
