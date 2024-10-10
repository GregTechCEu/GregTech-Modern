package com.gregtechceu.gtceu.api.graphnet.graph;

import com.gregtechceu.gtceu.api.graphnet.NetNode;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class GraphVertex {

    public final @NotNull NetNode wrapped;

    public GraphVertex(@NotNull NetNode wrapped) {
        this.wrapped = wrapped;
        wrapped.wrapper = this;
    }

    public @NotNull NetNode getWrapped() {
        return wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphVertex graphVertex = (GraphVertex) o;
        return Objects.equals(wrapped, graphVertex.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }
}
