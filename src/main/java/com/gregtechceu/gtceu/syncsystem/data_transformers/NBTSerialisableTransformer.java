package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class NBTSerialisableTransformer implements IValueTransformer<INBTSerializable<Tag>> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value, ISyncManaged holder) {
        return value.serializeNBT();
    }

    @Override
    public INBTSerializable<Tag> deserializeNBT(Tag tag, ISyncManaged holder, INBTSerializable<Tag> currentVal) {
        if (currentVal == null) return null;
        currentVal.deserializeNBT(tag);
        return currentVal;
    }
}
