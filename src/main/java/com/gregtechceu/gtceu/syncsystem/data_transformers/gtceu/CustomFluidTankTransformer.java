package com.gregtechceu.gtceu.syncsystem.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.syncsystem.data_transformers.ValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.Nullable;

public class CustomFluidTankTransformer implements ValueTransformer<CustomFluidTank> {

    @Override
    public Tag serializeNBT(CustomFluidTank value, ValueTransformer.TransformerContext<CustomFluidTank> context) {
        return value.serializeNBT();
    }

    @Override
    public @Nullable CustomFluidTank deserializeNBT(Tag tag,
                                                    ValueTransformer.TransformerContext<CustomFluidTank> context) {
        var currentVal = context.currentValue();
        if (currentVal == null) return null;
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);

        currentVal.deserializeNBT((CompoundTag) ValueTransformer.stripLdlibWrapper(compoundTag));
        return currentVal;
    }
}
