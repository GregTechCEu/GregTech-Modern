package com.gregtechceu.gtceu.api.graphnet.traverse.util;

public interface ReversibleLossOperator {

    ReversibleLossOperator IDENTITY = new ReversibleLossOperator() {

        @Override
        public double applyLoss(double value) {
            return value;
        }

        @Override
        public double undoLoss(double value) {
            return value;
        }
    };

    double applyLoss(double value);

    double undoLoss(double value);
}
