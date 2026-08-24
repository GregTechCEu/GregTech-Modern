package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerializableTransformer implements ValueTransformer<INBTSerializable<Tag>> {

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value,
                            ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        return value.serializeNBT(context.lookup());
    }

    @Override
    public @Nullable INBTSerializable<Tag> deserializeNBT(Tag tag,
                                                          ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from saved data.");
            return null;
        }
        currentVal.deserializeNBT(context.lookup(), tag);
        return currentVal;
    }


    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf, INBTSerializable<Tag> value,
                              TransformerContext<INBTSerializable<Tag>> context) {
        buf.writeNbt(value.serializeNBT(context.lookup()));
    }

    @Override
    public @Nullable INBTSerializable<Tag> readFromPacket(RegistryFriendlyByteBuf buf,
                                                          TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        Tag read = buf.readNbt();
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from a client packet.");
            return null;
        }
        if (read != null) currentVal.deserializeNBT(context.lookup(), read);
        return currentVal;
    }
}
