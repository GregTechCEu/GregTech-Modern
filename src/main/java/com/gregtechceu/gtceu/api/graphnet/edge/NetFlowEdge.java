package com.gregtechceu.gtceu.api.graphnet.edge;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class NetFlowEdge extends AbstractNetFlowEdge {

    private final int flowBufferTicks;
    private final int regenerationTime;

    /**
     * NetEdge that provides standard flow behavior handling
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowEdge(int flowBufferTicks) {
        this.flowBufferTicks = Math.max(flowBufferTicks, 1);
        this.regenerationTime = 1;
    }

    /**
     * NetEdge that provides standard flow behavior handling
     *
     * @param flowBufferMult   Determines maximum mult of 'buffer' flow capacity that can be built up along edges.
     *                         Allows for once-an-interval push/pull operations instead of needing them every unit of
     *                         time for maximum throughput.
     * @param regenerationTime Ticks required for flow to regenerate once. Allows slowing down the rate of regeneration.
     */
    public NetFlowEdge(int flowBufferMult, int regenerationTime) {
        this.flowBufferTicks = Math.max(flowBufferMult, 1);
        this.regenerationTime = Math.max(regenerationTime, 1);
    }

    @Override
    protected AbstractChannelsHolder getNewHolder(AbstractChannelsHolder prototype,
                                                  SimulatorKey simulator) {
        if (prototype instanceof ChannelsHolder holder) return new ChannelsHolder(holder, simulator);
        return new ChannelsHolder(simulator);
    }

    @Nullable
    private NetFlowEdge getInverse(IGraphNet graph) {
        if (getTarget() == null || getSource() == null) return null;
        NetEdge edge = graph.getEdge(getTarget(), getSource());
        if (edge instanceof NetFlowEdge i && i != this) {
            return i;
        }
        return null;
    }

    private final class ChannelsHolder extends AbstractChannelsHolder {

        private final Object2LongOpenHashMap<IPredicateTestObject> map;
        private long lastQueryTick;
        private boolean init;

        public ChannelsHolder(SimulatorKey simulator) {
            super(simulator);
            this.map = new Object2LongOpenHashMap<>(9);
        }

        public ChannelsHolder(ChannelsHolder prototype, SimulatorKey simulator) {
            super(simulator);
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        @Override
        boolean cannotSupportChannel(IPredicateTestObject channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            if (map.containsKey(channel)) return map.getLong(channel) <= 0;
            else return map.size() >= getChannelCount();
        }

        @Override
        long getFlowLimit(IPredicateTestObject channel, IGraphNet graph, long queryTick) {
            if (cannotSupportChannel(channel, queryTick)) return 0;
            long limit = map.getLong(channel);

            NetFlowEdge inverse = getInverse(graph);
            if (inverse != null) {
                if (inverse.cannotSupportChannel(channel, queryTick, getSimulator())) return 0;
                limit += inverse.getConsumedLimit(channel, queryTick, getSimulator());
            }

            return limit;
        }

        @Override
        long getConsumedLimit(IPredicateTestObject channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            long limit = map.defaultReturnValue();
            return limit - map.getLong(channel);
        }

        @Override
        void consumeFlowLimit(IPredicateTestObject channel, IGraphNet graph, long amount, long queryTick) {
            if (amount == 0) return;
            recalculateFlowLimits(queryTick);

            // check against reverse edge
            NetFlowEdge inverse = getInverse(graph);
            if (inverse != null) {
                long inverseConsumed = inverse.getConsumedLimit(channel, queryTick, getSimulator());
                if (inverseConsumed != 0) {
                    long toFreeUp = Math.min(inverseConsumed, amount);
                    inverse.consumeFlowLimit(channel, graph, -toFreeUp, queryTick, getSimulator());
                    if (toFreeUp == amount) return;
                    amount -= toFreeUp;
                }
            }

            long finalAmount = amount;
            map.compute(channel, (k, v) -> {
                long d = map.defaultReturnValue();
                if (v == null) v = d;
                v -= finalAmount;
                if (v >= d) return null;
                return v;
            });
        }

        @Override
        public void recalculateFlowLimits(long queryTick) {
            if (!this.init) {
                this.map.defaultReturnValue(getThroughput() * flowBufferTicks);
                this.init = true;
            }
            int regenerationUnits = (int) (queryTick - this.lastQueryTick) / regenerationTime;
            if (regenerationUnits < 0) {
                this.map.clear();
            } else if (regenerationUnits > 0) {
                List<Object> toRemove = new ObjectArrayList<>();
                this.map.replaceAll((k, v) -> {
                    v += (long) regenerationUnits * getThroughput();
                    if (v >= map.defaultReturnValue()) toRemove.add(k);
                    return v;
                });
                toRemove.forEach(this.map::removeLong);
                this.lastQueryTick += (long) regenerationUnits * regenerationTime;
            }
        }

        @Override
        Set<IPredicateTestObject> getActiveChannels(long queryTick) {
            recalculateFlowLimits(queryTick);
            return map.keySet();
        }
    }
}
