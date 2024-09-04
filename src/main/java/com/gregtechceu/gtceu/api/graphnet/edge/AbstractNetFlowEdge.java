package com.gregtechceu.gtceu.api.graphnet.edge;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.logic.ChannelCountLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.ThroughputLogic;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class AbstractNetFlowEdge extends NetEdge {

    private final AbstractChannelsHolder channels;
    private final WeakHashMap<SimulatorKey, AbstractChannelsHolder> simulatedChannels;

    public AbstractNetFlowEdge() {
        this.channels = getNewHolder(null, null);
        this.simulatedChannels = new WeakHashMap<>(9);
    }

    @Override
    public double getDynamicWeight(IPredicateTestObject channel, IGraphNet net, @Nullable SimulatorKey simulator,
                                   long queryTick,
                                   double defaultWeight) {
        long flow = getFlowLimit(channel, net, queryTick, simulator);
        if (flow <= 0) {
            return defaultWeight * getThroughput() * Short.MAX_VALUE;
        } else return defaultWeight * getThroughput() / flow;
    }

    public boolean cannotSupportChannel(IPredicateTestObject channel, long queryTick,
                                        @Nullable SimulatorKey simulator) {
        if (!this.test(channel)) return false;
        else return getChannels(simulator).cannotSupportChannel(channel, queryTick);
    }

    protected AbstractChannelsHolder getChannels(@Nullable SimulatorKey simulator) {
        if (simulator == null) return this.channels;
        else {
            AbstractChannelsHolder channels = simulatedChannels.get(simulator);
            if (channels == null) {
                channels = getNewHolder(this.channels, simulator);
                simulatedChannels.put(simulator, channels);
            }
            return channels;
        }
    }

    protected int getChannelCount() {
        return getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE).getValue();
    }

    public long getThroughput() {
        return getData().getLogicEntryDefaultable(ThroughputLogic.TYPE).getValue();
    }

    public long getFlowLimit(IPredicateTestObject channel, IGraphNet graph, long queryTick,
                             @Nullable SimulatorKey simulator) {
        if (!this.test(channel)) return 0;
        else return getChannels(simulator).getFlowLimit(channel, graph, queryTick);
    }

    protected long getConsumedLimit(IPredicateTestObject channel, long queryTick, @Nullable SimulatorKey simulator) {
        if (!this.test(channel)) return 0;
        else return getChannels(simulator).getConsumedLimit(channel, queryTick);
    }

    public void consumeFlowLimit(IPredicateTestObject channel, IGraphNet graph, long amount, long queryTick,
                                 @Nullable SimulatorKey simulator) {
        if (this.test(channel)) {
            getChannels(simulator).consumeFlowLimit(channel, graph, amount, queryTick);
        }
    }

    public Set<IPredicateTestObject> getActiveChannels(@Nullable SimulatorKey simulator, long queryTick) {
        return getChannels(simulator).getActiveChannels(queryTick);
    }

    protected abstract AbstractChannelsHolder getNewHolder(AbstractChannelsHolder prototype,
                                                           SimulatorKey simulator);

    protected abstract static class AbstractChannelsHolder {

        private final WeakReference<SimulatorKey> simulator;

        public AbstractChannelsHolder(SimulatorKey simulator) {
            this.simulator = new WeakReference<>(simulator);
        }

        public SimulatorKey getSimulator() {
            return simulator.get();
        }

        abstract void recalculateFlowLimits(long queryTick);

        abstract boolean cannotSupportChannel(IPredicateTestObject channel, long queryTick);

        abstract long getFlowLimit(IPredicateTestObject channel, IGraphNet graph, long queryTick);

        abstract long getConsumedLimit(IPredicateTestObject channel, long queryTick);

        abstract void consumeFlowLimit(IPredicateTestObject channel, IGraphNet graph, long amount, long queryTick);

        abstract Set<IPredicateTestObject> getActiveChannels(long queryTick);
    }
}
