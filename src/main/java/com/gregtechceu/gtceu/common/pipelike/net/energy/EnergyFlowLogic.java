package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends NetLogicEntry<EnergyFlowLogic, ByteTag> {

    public static final EnergyFlowLogic INSTANCE = new EnergyFlowLogic();

    private static final int MEMORY_TICKS = 10;

    private final Long2ObjectOpenHashMap<List<EnergyFlowData>> memory = new Long2ObjectOpenHashMap<>();

    protected EnergyFlowLogic() {
        super("EnergyFlow");
    }

    public @NotNull Long2ObjectOpenHashMap<List<EnergyFlowData>> getMemory() {
        return memory;
    }

    public @NotNull List<EnergyFlowData> getFlow(long tick) {
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, EnergyFlowData flow) {
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

    @Override
    public ByteTag serializeNBT() {
        return ByteTag.valueOf((byte) 0);
    }

    @Override
    public void deserializeNBT(ByteTag nbt) {}

    @Override
    public @NotNull EnergyFlowLogic getNew() {
        return new EnergyFlowLogic();
    }

    @Override
    public boolean shouldEncode() {
        return false;
    }

    @Override
    public void encode(FriendlyByteBuf buf, boolean fullChange) {}

    @Override
    public void decode(FriendlyByteBuf buf, boolean fullChange) {}
}
