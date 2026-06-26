package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;

import org.jetbrains.annotations.Nullable;

public class MonitorGroupTransformer implements ValueTransformer<MonitorGroup> {

    @Override
    public CompoundTag serializeNBT(MonitorGroup value, ValueTransformer.TransformerContext<MonitorGroup> context) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", value.getName());
        ListTag list = new ListTag();
        value.getMonitorPositions().forEach(pos -> list.add(NbtUtils.writeBlockPos(pos)));
        if (value.getTargetRaw() != null) {
            tag.put("targetPos", NbtUtils.writeBlockPos(value.getTargetRaw()));
            if (value.getTargetCoverSide() != null) {
                tag.putString("targetSide", value.getTargetCoverSide().getSerializedName());
            }
        }
        tag.put("positions", list);
        tag.putInt("dataSlot", value.getDataSlot());
        tag.put("items", value.getItemStackHandler().serializeNBT(context.lookup()));
        tag.put("placeholderSlots", value.getPlaceholderSlotsHandler().serializeNBT(context.lookup()));
        return tag;
    }

    @Override
    public @Nullable MonitorGroup deserializeNBT(Tag tag, ValueTransformer.TransformerContext<MonitorGroup> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
        CustomItemStackHandler handler = new CustomItemStackHandler(),
                placeholderSlotsHandler = new CustomItemStackHandler();
        handler.deserializeNBT(context.lookup(), compoundTag.getCompound("items"));
        placeholderSlotsHandler.deserializeNBT(context.lookup(), compoundTag.getCompound("placeholderSlots"));
        var group = new MonitorGroup(compoundTag.getString("name"), handler, placeholderSlotsHandler);
        ListTag list = compoundTag.getList("positions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            int[] aint = list.getIntArray(i);
            if (aint.length != 3) continue;
            group.add(new BlockPos(aint[0], aint[1], aint[2]));
        }
        if (compoundTag.contains("targetPos", Tag.TAG_COMPOUND)) {
            group.setTarget(NbtUtils.readBlockPos(compoundTag, "targetPos").orElse(BlockPos.ZERO));
            if (compoundTag.contains("targetSide", Tag.TAG_STRING)) {
                group.setTargetCoverSide(Direction.byName(compoundTag.getString("targetSide")));
            }
            if (compoundTag.contains("dataSlot", Tag.TAG_INT)) {
                group.setDataSlot(compoundTag.getInt("dataSlot"));
            }
        }
        return group;
    }
}
