package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MonitorGroupPayload extends ObjectTypedPayload<MonitorGroup> {

    @Override
    public @Nullable Tag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", payload.getName());
        List<Integer> list = new ArrayList<>();
        payload.getRelativePositions().forEach(pos -> {
            list.add(pos.getX());
            list.add(pos.getY());
            list.add(pos.getZ());
        });
        tag.putIntArray("positions", list);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            payload = new MonitorGroup(compoundTag.getString("name"));
            int[] arr = compoundTag.getIntArray("positions");
            for (int i = 0; i < arr.length / 3; i++) {
                payload.add(new BlockPos(arr[3 * i], arr[3 * i + 1], arr[3 * i + 2]));
            }
        }
    }
}
