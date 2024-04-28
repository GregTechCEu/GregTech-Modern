package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.List;

@SuppressWarnings({ "unused", "FieldCanBeLocal" })
public class GTToolTiers {

    private static Tier DURANIUM;
    private static Tier NEUTRONIUM;

    public static void init() {
        var netherite = new ResourceLocation("netherite");
        var duranium = GTCEu.id("duranium");
        var neutronium = GTCEu.id("neutronium");
        DURANIUM = new SimpleTier(CustomTags.INCORRECT_FOR_DURANIUM_TOOL, 8193, 14.0F, 12.0F, 33, () -> Ingredient.of(ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Duranium)));
        NEUTRONIUM = new SimpleTier(CustomTags.INCORRECT_FOR_NEUTRONIUM_TOOL, 65536, 180.0F, 100.0F, 33, () -> Ingredient.of(ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Neutronium)));
    }
}
