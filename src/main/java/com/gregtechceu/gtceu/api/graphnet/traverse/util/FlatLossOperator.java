package com.gregtechceu.gtceu.api.graphnet.traverse.util;

public class FlatLossOperator implements ReversibleLossOperator {

    private final double loss;

    public FlatLossOperator(double loss) {
        assert loss > 0;
        this.loss = loss;
    }

    @Override
    public double applyLoss(double value) {
        return value - loss;
    }

    @Override
    public double undoLoss(double value) {
        return value + loss;
    }
}
