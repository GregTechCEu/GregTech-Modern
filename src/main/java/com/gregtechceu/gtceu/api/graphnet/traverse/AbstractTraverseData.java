package com.gregtechceu.gtceu.api.graphnet.traverse;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.path.INetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTraverseData<N extends NetNode, P extends INetPath<N, ?>> implements ITraverseData<N, P> {

    @Getter
    private final IGraphNet graphNet;
    @Getter
    private final IPredicateTestObject testObject;
    @Nullable
    @Getter
    private final SimulatorKey simulatorKey;
    @Getter
    private final long queryTick;

    public AbstractTraverseData(IGraphNet graphNet, IPredicateTestObject testObject, SimulatorKey simulatorKey,
                                long queryTick) {
        this.graphNet = graphNet;
        this.testObject = testObject;
        this.simulatorKey = simulatorKey;
        this.queryTick = queryTick;
    }

    public boolean simulating() {
        return simulatorKey != null;
    }
}
