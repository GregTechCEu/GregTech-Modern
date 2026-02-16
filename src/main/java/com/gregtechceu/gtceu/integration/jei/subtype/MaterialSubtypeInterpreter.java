package com.gregtechceu.gtceu.integration.jei.subtype;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.jetbrains.annotations.Nullable;

public class MaterialSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    public static final MaterialSubtypeInterpreter INSTANCE = new MaterialSubtypeInterpreter();

    private MaterialSubtypeInterpreter() {}

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.getOrDefault(GTDataComponents.ITEM_MATERIAL, GTMaterials.NULL);
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        Material material = ingredient.getOrDefault(GTDataComponents.ITEM_MATERIAL, GTMaterials.NULL);
        if (material.isNull()) {
            return "";
        }
        return material.getUnlocalizedName();
    }
}
