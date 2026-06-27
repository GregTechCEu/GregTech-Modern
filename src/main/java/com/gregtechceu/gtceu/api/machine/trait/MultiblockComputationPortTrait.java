package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationPortPolicy;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import java.util.Optional;

public class MultiblockComputationPortTrait extends ComputationPortTrait {

    private final boolean producerPort;
    private final boolean consumerPort;

    public MultiblockComputationPortTrait(MetaMachine machine, boolean producerPort, boolean consumerPort) {
        super(machine, ComputationPortPolicy.OPTICAL_ONLY);
        this.producerPort = producerPort;
        this.consumerPort = consumerPort;
        this.capabilityValidator = side -> side == null || side == machine.getFrontFacing();
    }

    @Override
    public Optional<ComputationProducer> getComputationProducer() {
        return producerPort ? findProducer() : Optional.empty();
    }

    @Override
    public Optional<ComputationConsumer> getComputationConsumer() {
        return consumerPort ? findConsumer() : Optional.empty();
    }

    private Optional<ComputationProducer> findProducer() {
        if (!(machine instanceof IMultiPart part) || !part.isFormed()) return Optional.empty();
        for (IMultiController controller : part.getControllers()) {
            if (controller instanceof ComputationProducer producer) {
                return Optional.of(producer);
            }
            for(var trait: controller.self().getTraits())
            {
                if(trait instanceof ComputationProducer producer) {
                    return Optional.of(producer);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ComputationConsumer> findConsumer() {
        if (!(machine instanceof IMultiPart part) || !part.isFormed()) return Optional.empty();
        for (IMultiController controller : part.getControllers()) {
            if (controller instanceof ComputationConsumer consumer) {
                return Optional.of(consumer);
            }
            for(var trait: controller.self().getTraits())
            {
                if(trait instanceof ComputationConsumer producer) {
                    return Optional.of(producer);
                }
            }
        }
        return Optional.empty();
    }
}
