package com.gregtechceu.gtceu.api.graphnet.traverse.util;

public class CompositeLossOperator implements ReversibleLossOperator {

    private final ReversibleLossOperator first;
    private final ReversibleLossOperator second;

    public CompositeLossOperator(ReversibleLossOperator first, ReversibleLossOperator second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public double applyLoss(double value) {
        return second.applyLoss(first.applyLoss(value));
    }

    @Override
    public double undoLoss(double value) {
        return first.undoLoss(second.undoLoss(value));
    }
}
