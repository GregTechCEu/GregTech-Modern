package com.gregtechceu.gtceu.api.computation;

public interface ComputationConsumer {

    int getMinimumCWUt();

    int getRequestedCWUt();

    default void applyReceivedCWUt(int receivedCWUt) {}

    default void onComputationChanged() {}
}
