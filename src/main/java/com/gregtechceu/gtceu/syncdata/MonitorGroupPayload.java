package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        if (payload.getTargetRaw() != null) {
            tag.putInt("targetX", payload.getTargetRaw().getX());
            tag.putInt("targetY", payload.getTargetRaw().getY());
            tag.putInt("targetZ", payload.getTargetRaw().getZ());
            if (payload.getTargetCoverSide() != null) {
                tag.putString("targetSide", payload.getTargetCoverSide().getName());
            }
        }
        tag.putIntArray("positions", list);
        tag.putInt("dataSlot", payload.getDataSlot());
        tag.put("items", payload.getItemStackHandler().serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            CustomItemStackHandler handler = new CustomItemStackHandler();
            handler.deserializeNBT(compoundTag.getCompound("items"));
            payload = new MonitorGroup(compoundTag.getString("name"), handler);
            int[] arr = compoundTag.getIntArray("positions");
            for (int i = 0; i < arr.length / 3; i++) {
                payload.add(new BlockPos(arr[3 * i], arr[3 * i + 1], arr[3 * i + 2]));
            }
            if (compoundTag.contains("targetX")) {
                payload.setTarget(new BlockPos(
                        compoundTag.getInt("targetX"),
                        compoundTag.getInt("targetY"),
                        compoundTag.getInt("targetZ")));
                if (compoundTag.contains("targetSide"))
                    payload.setTargetCoverSide(Direction.byName(compoundTag.getString("targetSide")));
                if (compoundTag.contains("dataSlot"))
                    payload.setDataSlot(compoundTag.getInt("dataSlot"));
            }
        }
    }
}
