package com.gregtechceu.gtceu.api.computation;

import java.util.Optional;

public interface ComputationPort {

    ComputationPortPolicy getComputationPortPolicy();

    default Optional<ComputationProducer> getComputationProducer() {
        return Optional.empty();
    }

    default Optional<ComputationConsumer> getComputationConsumer() {
        return Optional.empty();
    }

    default void onOpticalRouteChanged() {}
}
