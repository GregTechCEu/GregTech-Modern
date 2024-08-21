package com.gregtechceu.gtceu.api.graphnet.edge;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class NetFlowSharedEdge extends AbstractNetFlowEdge {

    private final int flowBufferTicks;
    private final int regenerationTime;

    /**
     * NetEdge that provides flow behavior where the capacity along an edge is shared by all channels.
     *
     * @param flowBufferTicks Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                        for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                        throughput.
     */
    public NetFlowSharedEdge(int flowBufferTicks) {
        this.flowBufferTicks = Math.max(flowBufferTicks, 1);
        this.regenerationTime = 1;
    }

    /**
     * NetEdge that provides flow behavior where the capacity along an edge is shared by all channels.
     *
     * @param flowBufferTicks  Determines how many ticks of 'buffer' flow capacity can be built up along edges. Allows
     *                         for once-an-interval push/pull operations instead of needing them every tick for maximum
     *                         throughput.
     * @param regenerationTime Ticks required for flow to regenerate once. Allows slowing down the rate of regeneration.
     */
    public NetFlowSharedEdge(int flowBufferTicks, int regenerationTime) {
        this.flowBufferTicks = Math.max(flowBufferTicks, 1);
        this.regenerationTime = Math.max(regenerationTime, 1);
    }

    @Override
    protected AbstractChannelsHolder getNewHolder(AbstractChannelsHolder prototype,
                                                  SimulatorKey simulator) {
        if (prototype instanceof ChannelsHolder holder) return new ChannelsHolder(holder, simulator);
        return new ChannelsHolder(simulator);
    }

    @Nullable
    private NetFlowSharedEdge getInverse(IGraphNet graph) {
        if (getTarget() == null || getSource() == null) return null;
        NetEdge edge = graph.getEdge(getTarget(), getSource());
        if (edge instanceof NetFlowSharedEdge i && i != this) {
            return i;
        }
        return null;
    }

    private final class ChannelsHolder extends AbstractChannelsHolder {

        private long maxCapacity;
        private long sharedCapacity;
        private final Object2LongOpenHashMap<IPredicateTestObject> map;
        private long lastQueryTick;
        private boolean init;

        public ChannelsHolder(SimulatorKey simulator) {
            super(simulator);
            this.map = new Object2LongOpenHashMap<>(9);
            this.map.defaultReturnValue(0);
        }

        public ChannelsHolder(ChannelsHolder prototype, SimulatorKey simulator) {
            super(simulator);
            this.map = prototype.map.clone();
            this.lastQueryTick = prototype.lastQueryTick;
        }

        @Override
        public boolean cannotSupportChannel(IPredicateTestObject channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            if (sharedCapacity <= 0) return true;
            else return map.size() >= getChannelCount();
        }

        @Override
        long getFlowLimit(IPredicateTestObject channel, IGraphNet graph, long queryTick) {
            if (cannotSupportChannel(channel, queryTick)) return 0;

            NetFlowSharedEdge inverse = getInverse(graph);
            if (inverse != null) {
                if (inverse.cannotSupportChannel(channel, queryTick, getSimulator())) return 0;
                return sharedCapacity + inverse.getConsumedLimit(channel, queryTick, getSimulator());
            } else return sharedCapacity;
        }

        @Override
        long getConsumedLimit(IPredicateTestObject channel, long queryTick) {
            recalculateFlowLimits(queryTick);
            return map.getLong(channel);
        }

        @Override
        void consumeFlowLimit(IPredicateTestObject channel, IGraphNet graph, long amount, long queryTick) {
            if (amount == 0) return;
            recalculateFlowLimits(queryTick);

            // check against reverse edge
            NetFlowSharedEdge inverse = getInverse(graph);
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
                if (v == null) v = 0L;
                v += finalAmount;
                if (v <= 0) return null;
                return v;
            });
            sharedCapacity -= finalAmount;
            boundCapacity();
        }

        @Override
        public void recalculateFlowLimits(long queryTick) {
            if (!this.init) {
                this.maxCapacity = getThroughput() * flowBufferTicks;
                this.init = true;
            }
            int regenerationUnits = (int) (queryTick - this.lastQueryTick) / regenerationTime;
            if (regenerationUnits < 0) {
                this.map.clear();
            } else {
                List<Object> toRemove = new ObjectArrayList<>();
                long regenerationPer = Mth.ceil((double) regenerationUnits * getThroughput() / map.size());
                map.replaceAll((k, v) -> {
                    v -= regenerationPer;
                    if (v <= 0) toRemove.add(k);
                    return v;
                });
                sharedCapacity += regenerationPer * map.size();
                boundCapacity();
                toRemove.forEach(map::removeLong);
                this.lastQueryTick += (long) regenerationUnits * regenerationTime;
            }
        }

        private void boundCapacity() {
            if (this.sharedCapacity > this.maxCapacity) this.sharedCapacity = this.maxCapacity;
            else if (this.sharedCapacity < 0) this.sharedCapacity = 0;
        }

        @Override
        Set<IPredicateTestObject> getActiveChannels(long queryTick) {
            recalculateFlowLimits(queryTick);
            return map.keySet();
        }
    }
}
