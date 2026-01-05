package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class MaterialTransformer implements IValueTransformer<Material> {

    @Override
    public Tag serializeNBT(Material currentValue, ISyncManaged holder) {
        return StringTag.valueOf(currentValue.getResourceLocation().toString());
    }

    @Override
    public Material deserializeNBT(Tag tag, ISyncManaged holder, Material currentValue) {
        return GTCEuAPI.materialManager.getMaterial(tag.getAsString());
    }
}
