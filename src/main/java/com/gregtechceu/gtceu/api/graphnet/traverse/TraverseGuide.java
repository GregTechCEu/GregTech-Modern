package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

public class TraverseGuide<N extends NetNode, P extends INetPath<N, ?>, T extends ITraverseData<N, P>> {

    private final T data;
    private final Supplier<Iterator<P>> pathsSupplier;
    private final long flow;
    private final LongConsumer consumptionReport;

    public TraverseGuide(T data, Supplier<Iterator<P>> pathsSupplier, long flow,
                         @Nullable LongConsumer consumptionReport) {
        this.data = data;
        this.pathsSupplier = pathsSupplier;
        this.flow = flow;
        this.consumptionReport = consumptionReport;
    }

    public void reportConsumedFlow(long consumedFlow) {
        if (consumptionReport != null) consumptionReport.accept(consumedFlow);
    }

    public T getData() {
        return data;
    }

    public Supplier<Iterator<P>> getPathsSupplier() {
        return pathsSupplier;
    }

    public Iterator<P> getPaths() {
        return pathsSupplier.get();
    }

    public long getFlow() {
        return flow;
    }
}
