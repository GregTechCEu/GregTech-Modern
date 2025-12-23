package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public class GTRecipeTypeTransformer implements IValueTransformer<GTRecipeType> {

    @Override
    public Tag serializeNBT(GTRecipeType value, ISyncManaged holder) {
        var tag = new CompoundTag();
        if (value == null) return tag;
        tag.putString("namespace", value.registryName.getNamespace());
        tag.putString("path", value.registryName.getPath());
        return tag;
    }

    @Override
    public GTRecipeType deserializeNBT(Tag tag, ISyncManaged holder, @Nullable GTRecipeType currentVal) {
        if (!(tag instanceof CompoundTag compound) || compound.isEmpty()) return null;
        String namespace = compound.getString("namespace");
        String path = compound.getString("path");
        return GTRegistries.RECIPE_TYPES.get(new ResourceLocation(namespace, path));
    }
}
