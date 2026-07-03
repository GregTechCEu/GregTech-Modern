package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationPort;
import com.gregtechceu.gtceu.api.computation.ComputationPortPolicy;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ComputationPortTrait extends MachineTrait implements ComputationPort, Comparable<ComputationPortTrait> {

    private final ComputationPortPolicy policy;
    private final ComputationProducer producer;
    private final ComputationConsumer consumer;

    public ComputationPortTrait(MetaMachine machine, ComputationPortPolicy policy) {
        this(machine, policy, null, null);
    }

    public ComputationPortTrait(MetaMachine machine, ComputationPortPolicy policy,
                                ComputationProducer producer, ComputationConsumer consumer) {
        super(machine);
        this.policy = policy;
        this.producer = producer;
        this.consumer = consumer;
    }

    @Override
    public ComputationPortPolicy getComputationPortPolicy() {
        return policy;
    }

    @Override
    public Optional<ComputationProducer> getComputationProducer() {
        return Optional.ofNullable(producer);
    }

    @Override
    public Optional<ComputationConsumer> getComputationConsumer() {
        return Optional.ofNullable(consumer);
    }

    public BlockPos getPortPos() {
        return machine.getPos();
    }

    @Override
    public void onOpticalRouteChanged() {
        if (machine.getLevel() instanceof ServerLevel serverLevel) {
            ComputationNetworkManager.get(serverLevel).markPortTopologyDirty(this);
        }
    }

    @Override
    public void onMachineLoad() {
        if (machine.getLevel() instanceof ServerLevel serverLevel) {
            ComputationNetworkManager.get(serverLevel).registerPort(this);
        }
    }

    @Override
    public void onMachineUnLoad() {
        if (machine.getLevel() instanceof ServerLevel serverLevel) {
            ComputationNetworkManager.get(serverLevel).unregisterPort(this);
        }
    }

    @Override
    public int compareTo(@NotNull ComputationPortTrait o) {
        return (int) (machine.getPos().asLong() - o.machine.getPos().asLong());
    }
}
