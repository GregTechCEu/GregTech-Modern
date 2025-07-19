package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class MaterialTransformer implements IValueTransformer<Material> {

    @Override
    public Tag serializeNBT(Material currentValue, boolean isSync, boolean isFullSync) {
        return StringTag.valueOf(currentValue.getResourceLocation().toString());
    }

    @Override
    public Material deserializeNBT(Tag tag, Material currentValue, boolean isSync) {
        return GTCEuAPI.materialManager.getMaterial(tag.getAsString());
    }
}
