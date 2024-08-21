package com.gregtechceu.gtceu.api.graphnet.pipenet.traverse;

import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraftforge.common.capabilities.Capability;

import org.apache.commons.lang3.mutable.MutableByte;
import org.jetbrains.annotations.Nullable;

public class SimpleTileRoundRobinData<T> extends AbstractTileRoundRobinData {

    private final Capability<T> cap;

    public SimpleTileRoundRobinData(Capability<T> capability) {
        this.cap = capability;
    }

    @Override
    public boolean hasNextInternalDestination(WorldPipeNetNode node, @Nullable SimulatorKey simulator) {
        MutableByte pointer = getPointer(simulator);
        byte val = pointer.byteValue();
        progressToNextInternalDestination(node, simulator);
        boolean hasNext = !pointerFinished(pointer);
        pointer.setValue(val);
        return hasNext;
    }

    @Override
    public void progressToNextInternalDestination(WorldPipeNetNode node, @Nullable SimulatorKey simulator) {
        MutableByte pointer = getPointer(simulator);
        pointer.increment();
        while (!pointerFinished(pointer) && !hasCapabilityAtPointer(cap, node, simulator)) {
            pointer.increment();
        }
    }

    public @Nullable T getAtPointer(WorldPipeNetNode node, @Nullable SimulatorKey simulator) {
        return getCapabilityAtPointer(cap, node, simulator);
    }
}
