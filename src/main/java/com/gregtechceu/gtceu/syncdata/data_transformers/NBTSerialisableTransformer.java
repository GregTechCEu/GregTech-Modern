package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class NBTSerialisableTransformer implements IValueTransformer<INBTSerializable<Tag>> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(INBTSerializable<Tag> value, boolean isSync, boolean isFullSync) {
        return value.serializeNBT();
    }

    @Override
    public INBTSerializable<Tag> deserializeNBT(Tag tag, INBTSerializable<Tag> currentVal, boolean isSync) {
        if (currentVal == null) return null;
        currentVal.deserializeNBT(tag);
        return currentVal;
    }
}
