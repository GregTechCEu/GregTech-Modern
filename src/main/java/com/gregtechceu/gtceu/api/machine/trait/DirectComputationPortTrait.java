package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationPortPolicy;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

public class DirectComputationPortTrait extends ComputationPortTrait {

    public DirectComputationPortTrait(MetaMachine machine, boolean acceptsOptical,
                                      ComputationProducer producer, ComputationConsumer consumer) {
        super(machine, acceptsOptical ? ComputationPortPolicy.OPTICAL_AND_ADJACENT :
                ComputationPortPolicy.ADJACENT_ONLY, producer, consumer);
    }
}
