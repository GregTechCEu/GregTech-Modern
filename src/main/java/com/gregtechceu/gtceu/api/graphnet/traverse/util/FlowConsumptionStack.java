package com.gregtechceu.gtceu.api.graphnet.traverse.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.LongConsumer;

public class FlowConsumptionStack {

    private final List<LongConsumer> flowConsumers = new ObjectArrayList<>();

    private final List<ReversibleLossOperator> operators = new ObjectArrayList<>();

    private final ReversibleLossOperator initialOperator;

    public FlowConsumptionStack() {
        this.initialOperator = ReversibleLossOperator.IDENTITY;
    }

    public FlowConsumptionStack(ReversibleLossOperator initialOperator) {
        this.initialOperator = initialOperator;
    }

    public long applyLatestLossFunction(long value) {
        if (operators.isEmpty()) return (long) Math.floor(initialOperator.applyLoss(value));
        else return (long) Math.floor(operators.get(operators.size() - 1).applyLoss(value));
    }

    public void add(LongConsumer flowConsumer, ReversibleLossOperator postLoss) {
        flowConsumers.add(flowConsumer);
        operators.add(postLoss);
    }

    /**
     * Walks backwards along the loss operators and applies consumption to flow consumers.
     * 
     * @param endValue the target end value
     * @return the value that needs to be pushed into the start of the stack to achieve the end value.
     */
    public long consumeWithEndValue(long endValue) {
        double value = endValue;
        for (int i = operators.size() - 1; i >= 0; i--) {
            ReversibleLossOperator operator = operators.get(i);
            value = operator.undoLoss(value);
            flowConsumers.get(i).accept((long) Math.ceil(value));
        }
        return (long) Math.ceil(initialOperator.undoLoss(value));
    }
}
