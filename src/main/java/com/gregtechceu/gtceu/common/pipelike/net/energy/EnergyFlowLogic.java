package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;

import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntryType;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends NetLogicEntry<EnergyFlowLogic, ByteTag> {

    public static final NetLogicEntryType<EnergyFlowLogic> TYPE = new NetLogicEntryType<>("EnergyFlow", EnergyFlowLogic::new);

    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();

    private static final int MEMORY_TICKS = 10;

    @Getter
    @NotNull
    private final Long2ObjectOpenHashMap<List<EnergyFlowData>> memory = new Long2ObjectOpenHashMap<>();

    protected EnergyFlowLogic() {
        super(TYPE);
    }

    public @NotNull List<EnergyFlowData> getFlow(long tick) {
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, EnergyFlowData flow) {
        averageVoltageCounter.increment(tick, flow.getEU());
        averageAmperageCounter.increment(tick, flow.amperage());

        updateMemory(tick);
        memory.compute(tick, (k, v) -> {
            if (v == null) v = new ObjectArrayList<>();
            v.add(flow);
            return v;
        });
    }

    private void updateMemory(long tick) {
        var iter = memory.long2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<List<EnergyFlowData>> entry = iter.next();
            if (entry.getLongKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }

    public double getAverageAmperage(long currentTick) {
        return averageAmperageCounter.getAverage(currentTick);
    }

    public double getAverageVoltage(long currentTick) {
        return averageVoltageCounter.getAverage(currentTick);
    }

    @Override
    public ByteTag serializeNBT() {
        return ByteTag.valueOf((byte) 0);
    }

    @Override
    public void deserializeNBT(ByteTag nbt) {}

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {}

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {}
}
