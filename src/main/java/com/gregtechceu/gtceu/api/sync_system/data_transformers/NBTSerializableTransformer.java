package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerializableTransformer implements ValueTransformer<INBTSerializable<CompoundTag>> {

    @Override
    public Tag serializeNBT(INBTSerializable<CompoundTag> value,
                            ValueTransformer.TransformerContext<INBTSerializable<CompoundTag>> context) {
        return value.serializeNBT(context.lookup());
    }

    @Override
    public @Nullable INBTSerializable<CompoundTag> deserializeNBT(Tag tag,
                                                          ValueTransformer.TransformerContext<INBTSerializable<CompoundTag>> context) {
        var currentVal = context.currentValue();
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from saved data.");
            return null;
        }
        currentVal.deserializeNBT(context.lookup(), (CompoundTag)TagCompatibilityFixer.stripLDLibPayloadWrapper(tag));
        return currentVal;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, INBTSerializable<CompoundTag> value, TransformerContext<INBTSerializable<CompoundTag>> context) {
        buf.writeNbt(value.serializeNBT(context.lookup()));
    }

    @Override
    public @Nullable INBTSerializable<CompoundTag> readFromPacket(FriendlyByteBuf buf, TransformerContext<INBTSerializable<CompoundTag>> context) {
        var currentVal = context.currentValue();
        CompoundTag data = buf.readNbt();
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from a client packet.");
            return null;
        }
        currentVal.deserializeNBT(context.lookup(), (CompoundTag)TagCompatibilityFixer.stripLDLibPayloadWrapper(data));
        return currentVal;
    }
}
