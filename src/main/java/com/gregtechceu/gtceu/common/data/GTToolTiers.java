package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

@SuppressWarnings({ "unused", "FieldCanBeLocal" })
public class GTToolTiers {

    private static Tier DURANIUM;
    private static Tier NEUTRONIUM;

    public static void init() {
        var netherite = new ResourceLocation("netherite");
        var duranium = GTCEu.id("duranium");
        var neutronium = GTCEu.id("neutronium");
        DURANIUM = TierSortingRegistry.registerTier(
                new ForgeTier(5, 8193, 14.0F, 12.0F, 33, CustomTags.NEEDS_DURANIUM_TOOL,
                        () -> Ingredient.of(ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Duranium))),
                duranium,
                List.of(netherite),
                List.of(neutronium));
        NEUTRONIUM = TierSortingRegistry.registerTier(
                new ForgeTier(6, 65536, 180.0F, 100.0F, 33, CustomTags.NEEDS_NEUTRONIUM_TOOL,
                        () -> Ingredient.of(ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Neutronium))),
                neutronium,
                List.of(duranium),
                List.of());
    }
}
