package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class CustomFluidTankTransformer implements IValueTransformer<CustomFluidTank> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeNBT(CustomFluidTank value, ISyncManaged holder) {
        return value.serializeNBT();
    }

    @Override
    public CustomFluidTank deserializeNBT(Tag tag, ISyncManaged holder, @Nullable CustomFluidTank currentVal) {
        if (currentVal == null) return null;
        if (tag instanceof CompoundTag compoundTag) {

            // LDLib compat
            if (compoundTag.contains("p") && compoundTag.contains("t")) {
                currentVal.deserializeNBT(compoundTag.getCompound("p"));
            } else {
                currentVal.deserializeNBT(compoundTag);
            }
        }
        return currentVal;
    }
}
