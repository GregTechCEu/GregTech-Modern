package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.graphnet.logic.AbstractTransientLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicType;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FluidFlowLogic extends AbstractTransientLogicData<FluidFlowLogic> {

    public static final NetLogicType<FluidFlowLogic> TYPE = new NetLogicType<>(GTCEu.MOD_ID, "FluidFlow",
            FluidFlowLogic::new, new FluidFlowLogic());

    public static final int MEMORY_TICKS = 10;

    private final Long2ObjectOpenHashMap<List<FluidStack>> memory = new Long2ObjectOpenHashMap<>();
    private FluidStack last;

    @Override
    public @NotNull NetLogicType<FluidFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<List<FluidStack>> getMemory() {
        updateMemory(Platform.getMinecraftServer().getTickCount());
        return memory;
    }

    public @NotNull List<FluidStack> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, FluidStack flow) {
        updateMemory(tick);
        memory.computeIfAbsent(tick, k -> new ObjectArrayList<>()).add(flow);
        last = flow;
    }

    public FluidStack getLast() {
        return last;
    }

    private void updateMemory(long tick) {
        var iter = memory.long2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<List<FluidStack>> entry = iter.next();
            if (entry.getLongKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }
}
