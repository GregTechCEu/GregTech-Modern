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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;

import com.mojang.datafixers.util.Pair;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.HIGH_SIFTER_OUTPUT;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class OreRecipeHandler {
    // Make sure to update OreByProduct jei page with any byproduct changes made here!

    public static void init(Consumer<FinishedRecipe> provider) {
        for (TagPrefix ore : ORES.keySet()) {
            if (ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes || ORES.get(ore).shouldDropAsItem()) {
                ore.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processOre);
            }
        }
        ORE.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processOreForgeHammer);

        RAW_ORE.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processRawOre);

        CRUSHED.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedOre);
        CRUSHED_PURIFIED.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedPurified);
        CRUSHED_REFINED.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedCentrifuged);
        DUST_IMPURE.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processDirtyDust);
        DUST_PURE.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processPureDust);
    }

    private static void processMetalSmelting(TagPrefix crushedPrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(INGOT, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult) &&
                    !crushedPrefix.isIgnored(material)) {
                VanillaRecipeHelper.addSmeltingRecipe(provider,
                        "smelt_" + crushedPrefix.name + "_" + material.getName() + "_to_ingot",
                        ChemicalHelper.getTag(crushedPrefix, material), ingotStack, 0.5f);
            }
        }
    }

    public static void processOreForgeHammer(TagPrefix orePrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(CRUSHED, material);
        int amountOfCrushedOre = property.getOreMultiplier();
        int oreTypeMultiplier = TagPrefix.ORES.get(orePrefix).isDoubleDrops() ? 2 : 1;
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        String prefixString = orePrefix == ORE ? "" : orePrefix.name + "_";
        GTRecipeBuilder builder = FORGE_HAMMER_RECIPES
                .recipeBuilder("hammer_" + prefixString + material.getName() + "_ore_to_raw_ore")
                .inputItems(orePrefix, material)
                .category(GTRecipeCategories.ORE_FORGING)
                .duration(10).EUt(16);
        if (material.hasProperty(PropertyKey.GEM) && !GEM.isIgnored(material)) {
            builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre * oreTypeMultiplier,
                    ChemicalHelper.get(GEM, material, crushedStack.getCount())));
        } else {
            builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre * oreTypeMultiplier, crushedStack));
        }
        builder.save(provider);
    }

    public static void processOre(TagPrefix orePrefix, Material material, OreProperty property,
                                  Consumer<FinishedRecipe> provider) {
        Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack ingotStack;
        ItemStack byproductStack = ChemicalHelper.get(GEM, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = ChemicalHelper.get(DUST, byproductMaterial);
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();
        ItemStack crushedStack = ChemicalHelper.get(CRUSHED, material);
        int amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(INGOT, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(GEM, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(DUST, smeltingMaterial);
        }
        int oreTypeMultiplier = TagPrefix.ORES.get(orePrefix).isDoubleDrops() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        String prefixString = orePrefix == ORE ? "" : orePrefix.name + "_";
        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = MACERATOR_RECIPES
                    .recipeBuilder("macerate_" + prefixString + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(IntersectionIngredient.of(Ingredient.of(orePrefix.getItemTags(material)[0]),
                            Ingredient.of(orePrefix.getItemParentTags()[0])))
                    .outputItems(GTUtil.copyAmount(amountOfCrushedOre * 2 * oreTypeMultiplier, crushedStack))
                    .chancedOutput(byproductStack, 1400, 850)
                    .EUt(2)
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .duration(400);

            for (MaterialStack secondaryMaterial : orePrefix.secondaryMaterials()) {
                if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 800);
                }
            }

            builder.save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + oreTypeMultiplier * 0.5f) * 0.5f - 0.05f) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider,
                    "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    IntersectionIngredient.of(Ingredient.of(orePrefix.getItemTags(material)[0]),
                            Ingredient.of(orePrefix.getItemParentTags()[0])),
                    ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider,
                    "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    IntersectionIngredient.of(Ingredient.of(orePrefix.getItemTags(material)[0]),
                            Ingredient.of(orePrefix.getItemParentTags()[0])),
                    ingotStack, xp);
        }
    }

    public static void processRawOre(TagPrefix orePrefix, Material material, OreProperty property,
                                     Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(CRUSHED, material,
                material.getProperty(PropertyKey.ORE).getOreMultiplier());
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(INGOT, smeltingMaterial,
                    material.getProperty(PropertyKey.ORE).getOreMultiplier());
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(GEM, smeltingMaterial,
                    material.getProperty(PropertyKey.ORE).getOreMultiplier());
        } else {
            ingotStack = ChemicalHelper.get(DUST, smeltingMaterial,
                    material.getProperty(PropertyKey.ORE).getOreMultiplier());
        }

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES
                    .recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .category(GTRecipeCategories.ORE_FORGING)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !GEM.isIgnored(material)) {
                builder.outputItems(ChemicalHelper.get(GEM, material, crushedStack.getCount()));
            } else {
                builder.outputItems(crushedStack.copy());
            }
            builder.save(provider);

            GTRecipeBuilder builder2 = MACERATOR_RECIPES
                    .recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(GTUtil.copyAmount(crushedStack.getCount() * 2, crushedStack))
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .EUt(2)
                    .duration(400);

            Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(),
                    Material.class);
            ItemStack byproductStack = ChemicalHelper.get(GEM, byproductMaterial);
            if (byproductStack.isEmpty()) {
                byproductStack = ChemicalHelper.get(DUST, byproductMaterial);
            }
            builder2.chancedOutput(byproductStack, 1000, 300);

            for (MaterialStack secondaryMaterial : ORE.secondaryMaterials()) {
                if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                    builder2.chancedOutput(dustStack, 500, 100);
                    break;
                }
            }
            builder2.save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace, or don't have smelting recipes
        // at all.
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider,
                    "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), GTUtil.copyAmount(ingotStack.getCount(), ingotStack),
                    xp);
            VanillaRecipeHelper.addBlastingRecipe(provider,
                    "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), GTUtil.copyAmount(ingotStack.getCount(), ingotStack),
                    xp);
        }

        if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
            VanillaRecipeHelper.addShapedRecipe(provider, "compress_" + material.getName() + "_to_ore_block",
                    ChemicalHelper.get(RAW_ORE_BLOCK, material),
                    "BBB", "BBB", "BBB",
                    'B', ChemicalHelper.getTag(RAW_ORE, material));
            VanillaRecipeHelper.addShapelessRecipe(provider, "decompress_" + material.getName() + "_from_ore_block",
                    ChemicalHelper.get(RAW_ORE, material, 9),
                    ChemicalHelper.getTag(RAW_ORE_BLOCK, material));
        }
        COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "_to_raw_ore_block")
                .inputItems(RAW_ORE, material, 9)
                .outputItems(RAW_ORE_BLOCK, material)
                .duration(300).EUt(2).save(provider);

        FORGE_HAMMER_RECIPES.recipeBuilder("decompress_" + material.getName() + "_to_raw_ore")
                .inputItems(RAW_ORE_BLOCK, material)
                .outputItems(RAW_ORE, material, 9)
                .category(GTRecipeCategories.ORE_FORGING)
                .duration(300).EUt(2).save(provider);
    }

    public static void processCrushedOre(TagPrefix crushedPrefix, Material material, OreProperty property,
                                         Consumer<FinishedRecipe> provider) {
        ItemStack impureDustStack = ChemicalHelper.get(DUST_IMPURE, material);
        Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(), Material.class);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushedPrefix, material)
                .outputItems(impureDustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushedPrefix, material)
                .outputItems(impureDustStack)
                .duration(400).EUt(2)
                .chancedOutput(ChemicalHelper.get(DUST, byproductMaterial, property.getByProductMultiplier()), 1400,
                        850)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        ItemStack crushedPurifiedOre = GTUtil.copy(
                ChemicalHelper.get(CRUSHED_PURIFIED, material),
                ChemicalHelper.get(DUST, material));
        ItemStack crushedCentrifugedOre = GTUtil.copy(
                ChemicalHelper.get(CRUSHED_REFINED, material),
                ChemicalHelper.get(DUST, material));

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_fast")
                .inputItems(crushedPrefix, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .duration(8).EUt(4).save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore")
                .inputItems(crushedPrefix, material)
                .inputFluids(Water.getFluid(1000))
                .circuitMeta(1)
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.DUST, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.DUST, GTMaterials.Stone)
                .save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_distilled")
                .inputItems(crushedPrefix, material)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.DUST, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.DUST, GTMaterials.Stone)
                .duration(200)
                .save(provider);

        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_crushed_ore_to_refined_ore")
                .inputItems(crushedPrefix, material)
                .outputItems(crushedCentrifugedOre)
                .chancedOutput(TagPrefix.DUST, property.getOreByProduct(1, material), property.getByProductMultiplier(),
                        "1/3", 0)
                .outputItems(TagPrefix.DUST, GTMaterials.Stone)
                .save(provider);

        if (property.getWashedIn().getFirst() != null) {
            Material washingByproduct = GTUtil.selectItemInList(3, material, property.getOreByProducts(),
                    Material.class);
            Pair<Material, Integer> washedInTuple = property.getWashedIn();
            CHEMICAL_BATH_RECIPES.recipeBuilder("bathe_" + material.getName() + "_crushed_ore_to_purified_ore")
                    .inputItems(crushedPrefix, material)
                    .inputFluids(washedInTuple.getFirst().getFluid(washedInTuple.getSecond()))
                    .outputItems(crushedPurifiedOre)
                    .chancedOutput(ChemicalHelper.get(DUST, washingByproduct, property.getByProductMultiplier()), 7000,
                            580)
                    .chancedOutput(ChemicalHelper.get(DUST, Stone), 4000, 650)
                    .duration(200).EUt(VA[LV])
                    .category(GTRecipeCategories.ORE_BATHING)
                    .save(provider);
        }

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("crushed_ore_to_dust_%s", material.getName()),
                impureDustStack, 'h', new MaterialEntry(crushedPrefix, material));

        processMetalSmelting(crushedPrefix, material, property, provider);
    }

    public static void processCrushedCentrifuged(TagPrefix centrifugedPrefix, Material material, OreProperty property,
                                                 Consumer<FinishedRecipe> provider) {
        ItemStack dustStack = ChemicalHelper.get(DUST, material);
        ItemStack byproductStack = ChemicalHelper.get(DUST, GTUtil.selectItemInList(2,
                material, property.getOreByProducts(), Material.class), 1);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(centrifugedPrefix, material)
                .outputItems(dustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(centrifugedPrefix, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400).EUt(2)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider,
                String.format("centrifuged_ore_to_dust_%s", material.getName()), dustStack,
                'h', new MaterialEntry(centrifugedPrefix, material));

        processMetalSmelting(centrifugedPrefix, material, property, provider);
    }

    public static void processCrushedPurified(TagPrefix purifiedPrefix, Material material, OreProperty property,
                                              Consumer<FinishedRecipe> provider) {
        ItemStack crushedCentrifugedStack = ChemicalHelper.get(CRUSHED_REFINED, material);
        ItemStack dustStack = ChemicalHelper.get(DUST_PURE, material);
        Material byproductMaterial = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = ChemicalHelper.get(DUST, byproductMaterial);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(purifiedPrefix, material)
                .outputItems(dustStack)
                .duration(10)
                .EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(purifiedPrefix, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400).EUt(2)
                .category(GTRecipeCategories.ORE_CRUSHING)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("purified_ore_to_dust_%s", material.getName()),
                dustStack,
                'h', new MaterialEntry(purifiedPrefix, material));

        if (!crushedCentrifugedStack.isEmpty()) {
            THERMAL_CENTRIFUGE_RECIPES
                    .recipeBuilder("centrifuge_" + material.getName() + "_purified_ore_to_refined_ore")
                    .inputItems(purifiedPrefix, material)
                    .outputItems(crushedCentrifugedStack)
                    .chancedOutput(TagPrefix.DUST, byproductMaterial, "1/3", 0)
                    .save(provider);
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = ChemicalHelper.get(GEM_EXQUISITE, material);
            ItemStack flawlessStack = ChemicalHelper.get(GEM_FLAWLESS, material);
            ItemStack gemStack = ChemicalHelper.get(GEM, material);
            ItemStack flawedStack = ChemicalHelper.get(GEM_FLAWED, material);
            ItemStack chippedStack = ChemicalHelper.get(GEM_CHIPPED, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                GTRecipeBuilder builder = SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(purifiedPrefix, material)
                        .chancedOutput(exquisiteStack, 500, 150)
                        .chancedOutput(flawlessStack, 1500, 200)
                        .chancedOutput(gemStack, 5000, 1000)
                        .chancedOutput(dustStack, 2500, 500)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2000, 500);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3000, 350);

                builder.save(provider);
            } else {
                GTRecipeBuilder builder = SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(purifiedPrefix, material)
                        .chancedOutput(exquisiteStack, 300, 100)
                        .chancedOutput(flawlessStack, 1000, 150)
                        .chancedOutput(gemStack, 3500, 500)
                        .chancedOutput(dustStack, 5000, 750)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2500, 300);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3500, 400);

                builder.save(provider);
            }
        }
        processMetalSmelting(purifiedPrefix, material, property, provider);
    }

    public static void processDirtyDust(TagPrefix dustPrefix, Material material, OreProperty property,
                                        Consumer<FinishedRecipe> provider) {
        ItemStack dustStack = ChemicalHelper.get(DUST, material);

        Material byproduct = GTUtil.selectItemInList(
                0, material, property.getOreByProducts(), Material.class);

        GTRecipeBuilder builder = CENTRIFUGE_RECIPES
                .recipeBuilder("centrifuge_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustPrefix, material)
                .outputItems(dustStack)
                .duration((int) (material.getMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.chancedOutput(TagPrefix.DUST, byproduct, "1/9", 0);
        } else {
            builder.outputFluids(byproduct.getFluid(L / 9));
        }

        builder.save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustPrefix, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        // dust gains same amount of material as normal dust
        processMetalSmelting(dustPrefix, material, property, provider);
    }

    public static void processPureDust(TagPrefix purePrefix, Material material, OreProperty property,
                                       Consumer<FinishedRecipe> provider) {
        Material byproductMaterial = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack dustStack = ChemicalHelper.get(DUST, material);

        if (property.getSeparatedInto() != null && !property.getSeparatedInto().isEmpty()) {
            List<Material> separatedMaterial = property.getSeparatedInto();
            TagPrefix prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0 &&
                    separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT)) ? NUGGET : DUST;

            ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1),
                    prefix == NUGGET ? 2 : 1);

            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("separate_" + material.getName() + "_pure_dust_to_dust")
                    .inputItems(purePrefix, material)
                    .outputItems(dustStack)
                    .chancedOutput(TagPrefix.DUST, separatedMaterial.get(0), 1000, 250)
                    .chancedOutput(separatedStack2, prefix == TagPrefix.DUST ? 500 : 2000,
                            prefix == TagPrefix.DUST ? 150 : 600)
                    .duration(200).EUt(24)
                    .save(provider);
        }

        CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(purePrefix, material)
                .outputItems(dustStack)
                .chancedOutput(TagPrefix.DUST, byproductMaterial, "1/9", 0)
                .duration(100)
                .EUt(5)
                .save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(purePrefix, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        processMetalSmelting(purePrefix, material, property, provider);
    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST) && !material.hasFlag(MaterialFlags.NO_ORE_SMELTING);
    }
}
