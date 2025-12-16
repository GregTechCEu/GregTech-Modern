package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.HIGH_SIFTER_OUTPUT;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public final class OreRecipeHandler {
    // Make sure to update OreByProduct jei page with any byproduct changes made here!

    private OreRecipeHandler() {}

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        OreProperty property = material.getProperty(PropertyKey.ORE);
        if (property == null) {
            return;
        }

        for (TagPrefix ore : ORES.keySet()) {
            processOre(provider, ore, property, material);
        }

        processRawOre(provider, property, material);
        processCrushedOre(provider, property, material);
        processCrushedPurified(provider, property, material);
        processCrushedCentrifuged(provider, property, material);
        processDirtyDust(provider, property, material);
        processPureDust(provider, property, material);
    }

    private static void processMetalSmelting(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
                                             @NotNull TagPrefix prefix, @NotNull Material material) {
        Material smeltingResult = property.getDirectSmeltResult().isNull() ? material : property.getDirectSmeltResult();
        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(ingot, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult) && !prefix.isIgnored(material)) {
                String name = "smelt_" + prefix.name + "_" + material.getName() + "_to_ingot";
                TagKey<Item> tag = ChemicalHelper.getTag(prefix, material);

                VanillaRecipeHelper.addSmeltingRecipe(provider, name, tag, ingotStack, 0.5f);
                VanillaRecipeHelper.addBlastingRecipe(provider, name, tag, ingotStack, 0.5f);
            }
        }
    }

    private static void processOre(@NotNull Consumer<FinishedRecipe> provider, @NotNull TagPrefix orePrefix,
                                   @NotNull OreProperty property, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(orePrefix)) {
            return;
        }

        var inputStack = ChemicalHelper.get(orePrefix, material);

        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        }

        Material smeltingMaterial = property.getDirectSmeltResult().isNull() ? material :
                property.getDirectSmeltResult();
        ItemStack ingotStack;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }

        int oreTypeMultiplier = TagPrefix.ORES.get(orePrefix).isDoubleDrops() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);

        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        String prefixString = orePrefix == ore ? "" : orePrefix.name + "_";
        if (!crushedStack.isEmpty()) {
            int crushedCount = property.getOreMultiplier() * oreTypeMultiplier;
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES
                    .recipeBuilder("hammer_" + prefixString + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(inputStack)
                    .EUt(16)
                    .duration(10)
                    .category(GTRecipeCategories.ORE_FORGING);
            if (material.hasProperty(PropertyKey.GEM) && !ChemicalHelper.get(gem, material).isEmpty()) {
                builder.outputItems(ChemicalHelper.get(gem, material).copyWithCount(crushedCount));
            } else {
                builder.outputItems(crushedStack.copyWithCount(crushedCount));
            }
            builder.save(provider);

            builder = MACERATOR_RECIPES
                    .recipeBuilder("macerate_" + prefixString + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(inputStack)
                    .outputItems(crushedStack.copyWithCount(crushedCount * 2))
                    .chancedOutput(byproductStack, 1400, 0)
                    .EUt(2)
                    .duration(400)
                    .category(GTRecipeCategories.ORE_CRUSHING);

            for (MaterialStack secondaryMaterial : orePrefix.secondaryMaterials()) {
                if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 0);
                }
            }

            builder.save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + oreTypeMultiplier * 0.5f) * 0.5f - 0.05f) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider,
                    "smelt_" + prefixString + material.getName() + "_ore_to_ingot", inputStack,
                    ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider,
                    "smelt_" + prefixString + material.getName() + "_ore_to_ingot", inputStack,
                    ingotStack, xp);
        }
    }

    private static void processRawOre(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
                                      @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(rawOre)) {
            return;
        }

        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        }

        Material smeltingMaterial = property.getDirectSmeltResult().isNull() ? material :
                property.getDirectSmeltResult();
        ItemStack ingotStack;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }

        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier());

        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES
                    .recipeBuilder("hammer_raw_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(rawOre, material)
                    .category(GTRecipeCategories.ORE_FORGING)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !ChemicalHelper.get(gem, material).isEmpty()) {
                builder.outputItems(ChemicalHelper.get(gem, material, crushedStack.getCount())
                        .copyWithCount(property.getOreMultiplier()));
            } else {
                builder.outputItems(crushedStack.copyWithCount(property.getOreMultiplier()));
            }
            builder.save(provider);

            builder = MACERATOR_RECIPES
                    .recipeBuilder("macerate_raw_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(rawOre, material)
                    .outputItems(crushedStack.copyWithCount(property.getOreMultiplier() * 2))
                    .chancedOutput(byproductStack, 1400, 0)
                    .EUt(2)
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .duration(400);

            builder.save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !rawOre.isIgnored(material)) {
            float xp = Math.round(((1 + 0.5f) * 0.5f - 0.05f) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider,
                    "smelt_raw_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(rawOre, material),
                    ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider,
                    "smelt_raw_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(rawOre, material),
                    ingotStack, xp);
        }

        if (!ConfigHolder.INSTANCE.recipes.disableManualCompression && !rawOre.isIgnored(material)) {
            VanillaRecipeHelper.addShapedRecipe(provider, "compress_" + material.getName() + "_to_ore_block",
                    ChemicalHelper.get(rawOreBlock, material),
                    "BBB", "BBB", "BBB",
                    'B', ChemicalHelper.getTag(rawOre, material));
            VanillaRecipeHelper.addShapelessRecipe(provider, "decompress_" + material.getName() + "_from_ore_block",
                    ChemicalHelper.get(rawOre, material, 9),
                    ChemicalHelper.getTag(rawOreBlock, material));
        }

        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_to_raw_ore_block")
                .inputItems(rawOre, material, 9)
                .outputItems(rawOreBlock, material)
                .duration(300).EUt(2).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("decompress_" + material.getName() + "_to_raw_ore")
                .inputItems(rawOreBlock, material)
                .outputItems(rawOre, material, 9)
                .category(GTRecipeCategories.ORE_FORGING)
                .duration(300).EUt(2).save(provider);
    }

    private static void processCrushedOre(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
                                          @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushed)) {
            return;
        }

        ItemStack impureDustStack = ChemicalHelper.get(dustImpure, material);
        Material byproductMaterial = property.getOreByProduct(0, material);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushed, material)
                .outputItems(impureDustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushed, material)
                .outputItems(impureDustStack)
                .duration(400).EUt(2)
                .chancedOutput(ChemicalHelper.get(dust, byproductMaterial, property.getByProductMultiplier()), 1400,
                        0)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        ItemStack crushedPurifiedOre = GTUtil.copyFirst(
                ChemicalHelper.get(crushedPurified, material),
                ChemicalHelper.get(dust, material));
        ItemStack crushedCentrifugedOre = GTUtil.copyFirst(
                ChemicalHelper.get(crushedRefined, material),
                ChemicalHelper.get(dust, material));

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_fast")
                .inputItems(crushed, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .duration(8).EUt(4).save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore")
                .inputItems(crushed, material)
                .inputFluids(Water.getFluid(1000))
                .circuitMeta(1)
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_distilled")
                .inputItems(crushed, material)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .duration(200)
                .save(provider);

        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_crushed_ore_to_refined_ore")
                .inputItems(crushed, material)
                .outputItems(crushedCentrifugedOre)
                .chancedOutput(TagPrefix.dust, property.getOreByProduct(1, material), property.getByProductMultiplier(),
                        "1/3", 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .save(provider);

        if (!property.getWashedIn().first().isNull()) {
            Material washingByproduct = property.getOreByProduct(3, material);
            ObjectIntPair<Material> washedInTuple = property.getWashedIn();
            CHEMICAL_BATH_RECIPES.recipeBuilder("bathe_" + material.getName() + "_crushed_ore_to_purified_ore")
                    .inputItems(crushed, material)
                    .inputFluids(washedInTuple.first().getFluid(washedInTuple.secondInt()))
                    .outputItems(crushedPurifiedOre)
                    .chancedOutput(ChemicalHelper.get(dust, washingByproduct, property.getByProductMultiplier()), 7000,
                            0)
                    .chancedOutput(ChemicalHelper.get(dust, Stone), 4000, 0)
                    .duration(200).EUt(VA[LV])
                    .category(GTRecipeCategories.ORE_BATHING)
                    .save(provider);
        }

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("crushed_ore_to_dust_%s", material.getName()),
                impureDustStack, 'h', new MaterialEntry(crushed, material));

        processMetalSmelting(provider, property, crushed, material);
    }

    private static void processCrushedCentrifuged(@NotNull Consumer<FinishedRecipe> provider,
                                                  @NotNull OreProperty property, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushedRefined)) {
            return;
        }

        ItemStack dustStack = ChemicalHelper.get(dust, material);
        ItemStack byproductStack = ChemicalHelper.get(dust, property.getOreByProduct(2, material), 1);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(crushedRefined, material)
                .outputItems(dustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(crushedRefined, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 0)
                .duration(400).EUt(2)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider,
                String.format("centrifuged_ore_to_dust_%s", material.getName()), dustStack,
                'h', new MaterialEntry(crushedRefined, material));

        processMetalSmelting(provider, property, crushedRefined, material);
    }

    private static void processCrushedPurified(@NotNull Consumer<FinishedRecipe> provider,
                                               @NotNull OreProperty property,
                                               @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushedPurified)) {
            return;
        }

        ItemStack crushedCentrifugedStack = ChemicalHelper.get(crushedRefined, material);
        ItemStack dustStack = ChemicalHelper.get(dustPure, material);
        Material byproductMaterial = property.getOreByProduct(1, material);
        ItemStack byproductStack = ChemicalHelper.get(dust, byproductMaterial);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(crushedPurified, material)
                .outputItems(dustStack)
                .duration(10)
                .EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(crushedPurified, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 0)
                .duration(400).EUt(2)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("purified_ore_to_dust_%s", material.getName()),
                dustStack,
                'h', new MaterialEntry(crushedPurified, material));

        if (!crushedCentrifugedStack.isEmpty()) {
            THERMAL_CENTRIFUGE_RECIPES
                    .recipeBuilder("centrifuge_" + material.getName() + "_purified_ore_to_refined_ore")
                    .inputItems(crushedPurified, material)
                    .outputItems(crushedCentrifugedStack)
                    .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                    .save(provider);
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = ChemicalHelper.get(gemExquisite, material);
            ItemStack flawlessStack = ChemicalHelper.get(gemFlawless, material);
            ItemStack gemStack = ChemicalHelper.get(gem, material);
            ItemStack flawedStack = ChemicalHelper.get(gemFlawed, material);
            ItemStack chippedStack = ChemicalHelper.get(gemChipped, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                GTRecipeBuilder builder = SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(crushedPurified, material)
                        .chancedOutput(exquisiteStack, 500, 0)
                        .chancedOutput(flawlessStack, 1500, 0)
                        .chancedOutput(gemStack, 5000, 0)
                        .chancedOutput(dustStack, 2500, 0)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2000, 0);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3000, 0);

                builder.save(provider);
            } else {
                GTRecipeBuilder builder = SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(crushedPurified, material)
                        .chancedOutput(exquisiteStack, 300, 0)
                        .chancedOutput(flawlessStack, 1000, 0)
                        .chancedOutput(gemStack, 3500, 0)
                        .chancedOutput(dustStack, 5000, 0)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2500, 0);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3500, 0);

                builder.save(provider);
            }
        }
        processMetalSmelting(provider, property, crushedPurified, material);
    }

    private static void processDirtyDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
                                         @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustImpure)) {
            return;
        }

        ItemStack dustStack = ChemicalHelper.get(dust, material);
        Material byproduct = property.getOreByProduct(0, material);

        GTRecipeBuilder builder = CENTRIFUGE_RECIPES
                .recipeBuilder("centrifuge_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustImpure, material)
                .outputItems(dustStack)
                .duration((int) (material.getMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.chancedOutput(TagPrefix.dust, byproduct, "1/9", 0);
        } else {
            builder.outputFluids(byproduct.getFluid(L / 9));
        }

        builder.save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustImpure, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        // dust gains same amount of material as normal dust
        processMetalSmelting(provider, property, dustImpure, material);
    }

    private static void processPureDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
                                        @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustPure)) {
            return;
        }

        Material byproductMaterial = property.getOreByProduct(1, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        if (property.getSeparatedInto() != null && !property.getSeparatedInto().isEmpty()) {
            List<Material> separatedMaterial = property.getSeparatedInto();
            TagPrefix prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0 &&
                    separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT)) ? nugget : dust;

            ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1),
                    prefix == nugget ? 2 : 1);

            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("separate_" + material.getName() + "_pure_dust_to_dust")
                    .inputItems(dustPure, material)
                    .outputItems(dustStack)
                    .chancedOutput(TagPrefix.dust, separatedMaterial.get(0), 1000, 0)
                    .chancedOutput(separatedStack2, prefix == TagPrefix.dust ? 500 : 2000, 0)
                    .duration(200).EUt(24)
                    .save(provider);
        }

        CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(dustPure, material)
                .outputItems(dustStack)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/9", 0)
                .duration(100)
                .EUt(5)
                .save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(dustPure, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        processMetalSmelting(provider, property, dustPure, material);
    }

    private static boolean doesMaterialUseNormalFurnace(@NotNull Material material) {
        return !material.hasProperty(PropertyKey.BLAST) && !material.hasFlag(MaterialFlags.NO_ORE_SMELTING);
    }
}
