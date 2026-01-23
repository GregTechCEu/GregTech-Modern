package com.gregtechceu.gtceu.syncsystem.data_transformers;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class NBTSerialisableTransformer extends ValueTransformer<INBTSerializable<Tag>> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value, ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        return value.serializeNBT();
    }

    @Override
    public INBTSerializable<Tag> deserializeNBT(Tag tag, ValueTransformer.TransformerContext<INBTSerializable<Tag>> context) {
        var currentVal = context.currentValue();
        if (currentVal == null) return null;
        currentVal.deserializeNBT(tag);
        return currentVal;
    }
}
