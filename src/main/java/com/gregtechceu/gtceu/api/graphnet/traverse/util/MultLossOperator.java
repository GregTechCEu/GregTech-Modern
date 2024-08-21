package com.gregtechceu.gtceu.api.graphnet.traverse.util;

public class MultLossOperator implements ReversibleLossOperator {

    public static final MultLossOperator[] TENTHS = new MultLossOperator[10];

    public static final MultLossOperator[] EIGHTHS = new MultLossOperator[8];

    static {
        for (int i = 1; i < 10; i++) {
            TENTHS[i] = new MultLossOperator(0.1d * i);
        }
        for (int i = 1; i < 8; i++) {
            EIGHTHS[i] = new MultLossOperator(0.125d * i);
        }
    }

    private final double mult;

    public MultLossOperator(double mult) {
        assert mult > 0 && mult <= 1;
        this.mult = mult;
    }

    @Override
    public double applyLoss(double value) {
        return value * mult;
    }

    @Override
    public double undoLoss(double value) {
        return value / mult;
    }
}
