package com.gregtechceu.gtceu.api.graphnet.traverse.util;

public class CompleteLossOperator implements ReversibleLossOperator {

    public static final CompleteLossOperator INSTANCE = new CompleteLossOperator();

    private CompleteLossOperator() {}

    @Override
    public double applyLoss(double value) {
        return 0;
    }

    @Override
    public double undoLoss(double value) {
        return 0;
    }
}
