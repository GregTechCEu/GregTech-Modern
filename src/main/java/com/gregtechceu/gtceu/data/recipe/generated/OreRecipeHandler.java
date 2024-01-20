package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                ore.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processOre(tagPrefix, material, property, provider));
            }
        }

        rawOre.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processRawOre(tagPrefix, material, property, provider));

        crushed.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedOre(tagPrefix, material, property, provider));
        crushedPurified.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedPurified(tagPrefix, material, property, provider));
        crushedRefined.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedCentrifuged(tagPrefix, material, property, provider));
        dustImpure.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processDirtyDust(tagPrefix, material, property, provider));
        dustPure.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processPureDust(tagPrefix, material, property, provider));
    }


    private static void processMetalSmelting(TagPrefix crushedPrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(ingot, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult)) {
                VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + crushedPrefix.name + "_" + material.getName() + "_to_ingot",
                        ChemicalHelper.getTag(crushedPrefix, material), ingotStack, 0.5f);
            }
        }
    }

    public static void processOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack ingotStack;
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);

        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }
        int oreMultiplier = TagPrefix.ORES.get(orePrefix).isDoubleDrops() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * oreMultiplier);

        String prefixString = orePrefix == ore ? "" : orePrefix.name + "_";
        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + prefixString + material.getName() + "_ore_to_raw_ore")
                    .inputItems(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.outputItems(GTUtil.copyAmount(oreMultiplier, ChemicalHelper.get(gem, material, crushedStack.getCount())));
            } else {
                builder.outputItems(GTUtil.copyAmount(oreMultiplier, crushedStack));
            }
            builder.save(provider);

            builder = MACERATOR_RECIPES.recipeBuilder("macerate_" + prefixString + material.getName() + "_ore_to_raw_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(GTUtil.copyAmount(2 * oreMultiplier, crushedStack))
                    .chancedOutput(byproductStack, 1400, 850)
                    .EUt(2)
                    .duration(400);

            Supplier<Material> outputDustMat = ORES.get(orePrefix).material();
            if (outputDustMat != null) {
                builder.outputItems(dust, outputDustMat.get());
            }

            builder.save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + oreMultiplier * 0.5f) * 0.5f - 0.05f) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider, "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }

    public static void processRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        int amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, ChemicalHelper.get(gem, material, crushedStack.getCount())));
            } else {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, crushedStack));
            }
            builder.save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(crushedStack)
                    .chancedOutput(crushedStack, 5000, 750)
                    .chancedOutput(crushedStack, 2500, 500)
                    .chancedOutput(crushedStack, 1250, 250)
                    .EUt(2)
                    .duration(400)
                    .save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }

        if (!ConfigHolder.INSTANCE.recipes.disableManualCompression) {
            VanillaRecipeHelper.addShapedRecipe(provider, "compress_" + material.getName() + "_to_ore_block",
                    ChemicalHelper.get(rawOreBlock, material),
                    "BBB", "BBB", "BBB",
                    'B', ChemicalHelper.getTag(rawOre, material));
            VanillaRecipeHelper.addShapelessRecipe(provider, "decompress_" + material.getName() + "_from_ore_block",
                    ChemicalHelper.get(rawOre, material, 9),
                    ChemicalHelper.getTag(rawOreBlock, material));
            COMPRESSOR_RECIPES.recipeBuilder("compress_" + material.getName() + "to_ore_block")
                    .inputItems(rawOre, material, 9)
                    .outputItems(rawOreBlock, material)
                    .duration(300).EUt(2).save(provider);
        }
    }

    public static void processCrushedOre(TagPrefix crushedPrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack impureDustStack = ChemicalHelper.get(dustImpure, material);
        Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(), Material.class);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushedPrefix, material)
                .outputItems(impureDustStack)
                .duration(10).EUt(16)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushedPrefix, material)
                .outputItems(impureDustStack)
                .duration(400).EUt(2)
                .chancedOutput(ChemicalHelper.get(dust, byproductMaterial, property.getByProductMultiplier()), 1400, 850)
                .save(provider);

        ItemStack crushedPurifiedOre = GTUtil.copy(
                ChemicalHelper.get(crushedPurified, material),
                ChemicalHelper.get(dust, material));
        ItemStack crushedCentrifugedOre = GTUtil.copy(
                ChemicalHelper.get(crushedRefined, material),
                ChemicalHelper.get(dust, material));

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
                .chancedOutput(TagPrefix.dust, byproductMaterial, 3333, 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .save(provider);

        ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_distilled")
                .inputItems(crushedPrefix, material)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.dust, byproductMaterial, 3333, 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .duration(200)
                .save(provider);

        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_crushed_ore_to_refined_ore")
                .inputItems(crushedPrefix, material)
                .outputItems(crushedCentrifugedOre)
                .chancedOutput(TagPrefix.dust, property.getOreByProduct(1, material), property.getByProductMultiplier(), 3333, 0)
                .outputItems(TagPrefix.dust, GTMaterials.Stone)
                .save(provider);

        if (property.getWashedIn().getKey() != null) {
            Material washingByproduct = GTUtil.selectItemInList(3, material, property.getOreByProducts(), Material.class);
            Pair<Material, Integer> washedInTuple = property.getWashedIn();
            CHEMICAL_BATH_RECIPES.recipeBuilder("bathe_" + material.getName() + "_crushed_ore_to_purified_ore")
                    .inputItems(crushedPrefix, material)
                    .inputFluids(washedInTuple.getKey().getFluid(washedInTuple.getValue()))
                    .outputItems(crushedPurifiedOre)
                    .chancedOutput(ChemicalHelper.get(dust, washingByproduct, property.getByProductMultiplier()), 7000, 580)
                    .chancedOutput(ChemicalHelper.get(dust, Stone), 4000, 650)
                    .duration(200).EUt(VA[LV])
                    .save(provider);
        }

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("crushed_ore_to_dust_%s", material.getName()),
                impureDustStack, 'h', new UnificationEntry(crushedPrefix, material));


        processMetalSmelting(crushedPrefix, material, property, provider);
    }

    public static void processCrushedCentrifuged(TagPrefix centrifugedPrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack dustStack = ChemicalHelper.get(dust, material);
        ItemStack byproductStack = ChemicalHelper.get(dust, GTUtil.selectItemInList(2,
                material, property.getOreByProducts(), Material.class), 1);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(centrifugedPrefix, material)
                .outputItems(dustStack)
                .duration(10).EUt(16)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(centrifugedPrefix, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400).EUt(2)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("centrifuged_ore_to_dust_%s", material.getName()), dustStack,
                'h', new UnificationEntry(centrifugedPrefix, material));

        processMetalSmelting(centrifugedPrefix, material, property, provider);
    }

    public static void processCrushedPurified(TagPrefix purifiedPrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedCentrifugedStack = ChemicalHelper.get(crushedRefined, material);
        ItemStack dustStack = ChemicalHelper.get(dustPure, material);
        Material byproductMaterial = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = ChemicalHelper.get(dust, byproductMaterial);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(purifiedPrefix, material)
                .outputItems(dustStack)
                .duration(10)
                .EUt(16)
                .save(provider);

        MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(purifiedPrefix, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(400).EUt(2)
                .save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, String.format("purified_ore_to_dust_%s", material.getName()), dustStack,
                'h', new UnificationEntry(purifiedPrefix, material));

        if (!crushedCentrifugedStack.isEmpty()) {
            THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_purified_ore_to_refined_ore")
                    .inputItems(purifiedPrefix, material)
                    .outputItems(crushedCentrifugedStack)
                    .chancedOutput(TagPrefix.dust, byproductMaterial, 3333, 0)
                    .save(provider);
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = ChemicalHelper.get(gemExquisite, material);
            ItemStack flawlessStack = ChemicalHelper.get(gemFlawless, material);
            ItemStack gemStack = ChemicalHelper.get(gem, material);
            ItemStack flawedStack = ChemicalHelper.get(gemFlawed, material);
            ItemStack chippedStack = ChemicalHelper.get(gemChipped, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                GTRecipeBuilder builder = SIFTER_RECIPES.recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
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
                GTRecipeBuilder builder = SIFTER_RECIPES.recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
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

    public static void processDirtyDust(TagPrefix dustPrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        Material byproduct = GTUtil.selectItemInList(
                0, material, property.getOreByProducts(), Material.class);

        GTRecipeBuilder builder = CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustPrefix, material)
                .outputItems(dustStack)
                .duration((int) (material.getMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.chancedOutput(TagPrefix.dust, byproduct, 1111, 0);
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

        //dust gains same amount of material as normal dust
        processMetalSmelting(dustPrefix, material, property, provider);
    }

    public static void processPureDust(TagPrefix purePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        Material byproductMaterial = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        if (property.getSeparatedInto() != null && !property.getSeparatedInto().isEmpty()) {
            List<Material> separatedMaterial = property.getSeparatedInto();
            TagPrefix prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0 && separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT))
                    ? nugget : dust;

            ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1), prefix == nugget ? 2 : 1);

            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("separate_" + material.getName() + "_pure_dust_to_dust")
                    .inputItems(purePrefix, material)
                    .outputItems(dustStack)
                    .chancedOutput(TagPrefix.dust, separatedMaterial.get(0), 1000, 250)
                    .chancedOutput(separatedStack2, prefix == TagPrefix.dust ? 500 : 2000, prefix == TagPrefix.dust ? 150 : 600)
                    .duration(200).EUt(24)
                    .save(provider);
        }

        CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(purePrefix, material)
                .outputItems(dustStack)
                .chancedOutput(TagPrefix.dust, byproductMaterial, 1111, 0)
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
        return !material.hasProperty(PropertyKey.BLAST);
    }


}
