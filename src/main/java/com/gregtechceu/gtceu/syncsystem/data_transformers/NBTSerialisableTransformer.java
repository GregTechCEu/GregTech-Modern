package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NBTSerialisableTransformer implements ValueTransformer<INBTSerializable<Tag>> {

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value,
                            ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        return value.serializeNBT();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable INBTSerializable<Tag> deserializeNBT(Tag tag,
                                                          ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        try {
            if (currentVal == null) currentVal = (INBTSerializable<Tag>) context.clazz().getConstructor().newInstance();
        } catch (Exception e) {
            GTCEu.LOGGER.error("Sync: Failed to instantiate INBTSerializable class", e);
            return null;
        }
        currentVal.deserializeNBT(tag);
        return currentVal;
    }
}
