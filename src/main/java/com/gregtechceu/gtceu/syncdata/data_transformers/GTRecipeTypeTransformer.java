package com.gregtechceu.gtceu.syncdata.data_transformers;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class GTRecipeTypeTransformer implements IValueTransformer<GTRecipeType> {

    @Override
    public Tag serializeNBT(GTRecipeType value, boolean isSync, boolean fullSync) {
        var tag = new CompoundTag();
        tag.putString("namespace", value.registryName.getNamespace());
        tag.putString("path", value.registryName.getPath());
        return tag;
    }

    @Override
    public GTRecipeType deserializeNBT(Tag tag, @Nullable GTRecipeType currentVal, boolean isSync) {
        if (!(tag instanceof CompoundTag compound)) return null;
        String namespace = compound.getString("namespace");
        String path = compound.getString("path");
        return GTRegistries.RECIPE_TYPES.get(new ResourceLocation(namespace, path));
    }
}
