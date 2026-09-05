package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerializableTransformer implements ValueTransformer<INBTSerializable<Tag>> {

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value,
                            ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        return value.serializeNBT();
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
        currentVal.deserializeNBT(tag);
        return currentVal;
    }

    private static final String WRAPPED_TAG_KEY = "$$field$$";

    @Override
    public void writeToPacket(FriendlyByteBuf buf, INBTSerializable<Tag> value,
                              TransformerContext<INBTSerializable<Tag>> context) {
        Tag data = value.serializeNBT();
        if (data instanceof CompoundTag compoundTag) {
            buf.writeNbt(compoundTag);
        } else {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put(WRAPPED_TAG_KEY, data);
            buf.writeNbt(wrapper);
        }
    }

    @Override
    public @Nullable INBTSerializable<Tag> readFromPacket(FriendlyByteBuf buf,
                                                          TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        Tag read = buf.readNbt();
        if (read instanceof CompoundTag compound && compound.size() == 1 && compound.contains(WRAPPED_TAG_KEY)) {
            read = compound.get(WRAPPED_TAG_KEY);
        }
        if (currentVal == null) {
            GTCEu.LOGGER.warn(
                    "Sync: Deserialization of INBTSerializable objects requires an existing object, they cannot be instantiated purely from a client packet.");
            return null;
        }
        if (read != null) currentVal.deserializeNBT(read);
        return currentVal;
    }
}
