package com.gregtechceu.gtceu.syncdata;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class MonitorGroupPayload extends ObjectTypedPayload<MonitorGroup> {

    @Override
    public @Nullable Tag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", payload.getName());
        ListTag list = new ListTag();
        payload.getMonitorPositions().forEach(pos -> {
            list.add(NbtUtils.writeBlockPos(pos));
        });
        if (payload.getTargetRaw() != null) {
            tag.put("targetPos", NbtUtils.writeBlockPos(payload.getTargetRaw()));
            if (payload.getTargetCoverSide() != null) {
                tag.putString("targetSide", payload.getTargetCoverSide().getSerializedName());
            }
        }
        tag.put("positions", list);
        tag.putInt("dataSlot", payload.getDataSlot());
        tag.put("items", payload.getItemStackHandler().serializeNBT(provider));
        tag.put("placeholderSlots", payload.getPlaceholderSlotsHandler().serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(Tag tag, HolderLookup.Provider provider) {
        if (tag instanceof CompoundTag compoundTag) {
            CustomItemStackHandler handler = new CustomItemStackHandler(),
                    placeholderSlotsHandler = new CustomItemStackHandler();
            handler.deserializeNBT(provider, compoundTag.getCompound("items"));
            placeholderSlotsHandler.deserializeNBT(provider, compoundTag.getCompound("placeholderSlots"));
            payload = new MonitorGroup(compoundTag.getString("name"), handler, placeholderSlotsHandler);
            ListTag list = compoundTag.getList("positions", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                payload.add(NbtUtils.readBlockPos(list.getCompound(i), "pos").orElse(BlockPos.ZERO));
            }
            if (compoundTag.contains("targetPos", Tag.TAG_COMPOUND)) {
                payload.setTarget(NbtUtils.readBlockPos(compoundTag, "targetPos").orElse(BlockPos.ZERO));
                if (compoundTag.contains("targetSide", Tag.TAG_STRING)) {
                    payload.setTargetCoverSide(Direction.byName(compoundTag.getString("targetSide")));
                }
                if (compoundTag.contains("dataSlot", Tag.TAG_INT)) {
                    payload.setDataSlot(compoundTag.getInt("dataSlot"));
                }
            }
        }
    }
}
