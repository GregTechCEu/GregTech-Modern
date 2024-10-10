package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.AbstractTransientLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicType;

import com.lowdragmc.lowdraglib.Platform;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends AbstractTransientLogicData<EnergyFlowLogic> {

    public static final NetLogicType<EnergyFlowLogic> TYPE = new NetLogicType<>(GTCEu.MOD_ID, "EnergyFlow",
            EnergyFlowLogic::new, new EnergyFlowLogic());

    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();

    public static final int MEMORY_TICKS = 10;

    @NotNull
    private final Long2ObjectOpenHashMap<List<EnergyFlowData>> memory = new Long2ObjectOpenHashMap<>();

    @Override
    public @NotNull NetLogicType<EnergyFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<List<EnergyFlowData>> getMemory() {
        updateMemory(Platform.getMinecraftServer().getTickCount());
        return memory;
    }

    public @NotNull List<EnergyFlowData> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, EnergyFlowData flow) {
        averageVoltageCounter.increment(tick, flow.getEU());
        averageAmperageCounter.increment(tick, flow.amperage());

        updateMemory(tick);
        memory.computeIfAbsent(tick, k -> new ObjectArrayList<>()).add(flow);
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
}
