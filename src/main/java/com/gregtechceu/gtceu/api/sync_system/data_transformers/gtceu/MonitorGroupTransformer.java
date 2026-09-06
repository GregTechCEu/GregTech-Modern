package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MonitorGroupTransformer implements ValueTransformer<MonitorGroup> {

    @Override
    public Tag serializeNBT(MonitorGroup value, ValueTransformer.TransformerContext<MonitorGroup> context) {
        return MonitorGroup.CODEC.encodeStart(context.nbtOps(), value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public @Nullable MonitorGroup deserializeNBT(Tag tag, ValueTransformer.TransformerContext<MonitorGroup> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);

        // Backwards compat

        var positions = compoundTag.contains("positions", Tag.TAG_LIST) ?
                compoundTag.getList("positions", Tag.TAG_COMPOUND) : null;
        var placeholderItems = compoundTag.contains("placeholderSlots", Tag.TAG_COMPOUND) ?
                compoundTag.getCompound("placeholderSlots") : null;
        var targetPos = compoundTag.contains("targetPos", Tag.TAG_COMPOUND) ? compoundTag.getCompound("targetPos") :
                null;
        var items = compoundTag.contains("items", Tag.TAG_COMPOUND) ? compoundTag.getCompound("items") : null;

        if (positions != null && !compoundTag.contains("monitorPositions")) {
            List<BlockPos> posList = new ArrayList<>();

            for (int i = 0; i < positions.size(); i++) {
                CompoundTag posTag = positions.getCompound(i);
                posList.add(NbtUtils.readBlockPos(posTag));
            }

            compoundTag.put("monitorPositions", BlockPos.CODEC.listOf().encodeStart(context.nbtOps(), posList)
                    .getOrThrow(false, GTCEu.LOGGER::error));
        }

        if (placeholderItems != null && !compoundTag.contains("placeholderItems")) {
            compoundTag.put("placeholderItems", Objects.requireNonNull(placeholderItems.get("Items")));
            compoundTag.remove("placeholderSlots");
        }

        if (targetPos != null) {
            BlockPos pos = NbtUtils.readBlockPos(targetPos);
            compoundTag.put("targetPos",
                    BlockPos.CODEC.encodeStart(context.nbtOps(), pos).getOrThrow(false, GTCEu.LOGGER::error));
        }

        if (items != null) {
            compoundTag.put("items", Objects.requireNonNull(items.get("Items")));
        }

        return MonitorGroup.CODEC.parse(context.nbtOps(), tag).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, MonitorGroup value, TransformerContext<MonitorGroup> context) {
        buf.writeNbt((CompoundTag) serializeNBT(value, context));
    }

    @Override
    public @Nullable MonitorGroup readFromPacket(FriendlyByteBuf buf, TransformerContext<MonitorGroup> context) {
        return deserializeNBT(Objects.requireNonNull(buf.readNbt()), context);
    }
}
