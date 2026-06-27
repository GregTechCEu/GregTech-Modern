package com.gregtechceu.gtceu.api.computation;

public interface ComputationProducer {

    int getOfferedCWUt();

    default boolean canBridgeComputation() {
        return true;
    }

    default void applyProducedCWUt(int allocatedCWUt) {}
}
