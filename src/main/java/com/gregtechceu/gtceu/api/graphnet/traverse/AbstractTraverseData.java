package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractTraverseData<N extends NetNode, P extends INetPath<N, ?>> implements ITraverseData<N, P> {

    private final IGraphNet net;
    private final IPredicateTestObject testObject;
    private final SimulatorKey simulator;
    private final long queryTick;

    public AbstractTraverseData(IGraphNet net, IPredicateTestObject testObject, SimulatorKey simulator,
                                long queryTick) {
        this.net = net;
        this.testObject = testObject;
        this.simulator = simulator;
        this.queryTick = queryTick;
    }

    @Override
    public IGraphNet getGraphNet() {
        return net;
    }

    @Override
    public IPredicateTestObject getTestObject() {
        return testObject;
    }

    @Override
    public @Nullable SimulatorKey getSimulatorKey() {
        return simulator;
    }

    @Override
    public long getQueryTick() {
        return queryTick;
    }
}
