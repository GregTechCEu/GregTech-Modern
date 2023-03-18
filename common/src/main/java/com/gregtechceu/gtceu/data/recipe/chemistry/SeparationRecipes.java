package com.gregtechceu.gtceu.data.recipe.chemistry;


import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.libs.GTRecipeTypes;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTItems.*;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;

public class SeparationRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Centrifuge
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(RefineryGas.getName())
                .inputFluids(RefineryGas.getFluid(8000))
                .outputFluids(Methane.getFluid(4000))
                .outputFluids(LPG.getFluid(4000))
                .duration(200).EUt(5).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Butane.getName())
                .inputFluids(Butane.getFluid(320))
                .outputFluids(LPG.getFluid(370))
                .duration(20).EUt(5).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Propane.getName())
                .inputFluids(Propane.getFluid(320))
                .outputFluids(LPG.getFluid(290))
                .duration(20).EUt(5).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(NitrationMixture.getName())
                .inputFluids(NitrationMixture.getFluid(2000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(SulfuricAcid.getFluid(1000))
                .duration(192).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_%s".formatted(ReinforcedEpoxyResin.getName()))
                .inputItems(dust, ReinforcedEpoxyResin)
                .outputItems(dust, Epoxy)
                .duration(24).EUt(5).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("ore_%s".formatted(Oilsands.getName()))
                .inputItems(TagPrefix.ore, Oilsands)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .outputFluids(Oil.getFluid(500))
                .duration(200).EUt(5).save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Items.NETHER_WART.getDescriptionId()).duration(144).EUt(5)
                .inputItems(Ingredient.of(Items.NETHER_WART))
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Blocks.BROWN_MUSHROOM.getDescriptionId()).duration(144).EUt(5)
                .inputItems(Ingredient.of(Blocks.BROWN_MUSHROOM))
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Blocks.RED_MUSHROOM.getDescriptionId()).duration(144).EUt(5)
                .inputItems(Ingredient.of(Blocks.RED_MUSHROOM))
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Items.MAGMA_CREAM.getDescriptionId()).duration(500).EUt(5)
                .inputItems(Ingredient.of(Items.MAGMA_CREAM))
                .outputItems(new ItemStack(Items.BLAZE_POWDER))
                .outputItems(new ItemStack(Items.SLIME_BALL))
                .save(provider);

        for (Item item : Registry.ITEM) {
            if (item.getFoodProperties() != null) {
                var foodProperties = item.getFoodProperties();
                var healAmount = foodProperties.getNutrition();
                var saturationModifier = foodProperties.getSaturationModifier();
                if (healAmount > 0) {
                    FluidStack outputStack = Methane.getFluid(Math.round(9 * healAmount * (1.0f + saturationModifier)));
                    GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("food_" + item.getDescriptionId()).duration(144).EUt(5)
                            .inputItems(item.getDefaultInstance())
                            .outputFluids(outputStack)
                            .save(provider);
                }
            }
        }

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(STICKY_RESIN.get().getDescriptionId()).duration(400).EUt(5)
                .inputItems(STICKY_RESIN.asStack())
                .outputItems(dust, RawRubber, 3)
                .chancedOutput(PLANT_BALL.asStack(), 1000, 850)
                .outputFluids(Glue.getFluid(100))
                .save(provider);

        //TODO
//        CENTRIFUGE_RECIPES.recipeBuilder().duration(200).EUt(20)
//                .inputItems(Ingredient.of(MetaBlocks.RUBBER_LOG))
//                .chancedOutput(STICKY_RESIN, 5000, 1200)
//                .chancedOutput(PLANT_BALL, 3750, 900)
//                .chancedOutput(dust, Carbon, 2500, 600)
//                .chancedOutput(dust, Wood, 2500, 700)
//                .outputFluids(Methane.getFluid(60))
//                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dirt").duration(250).EUt(GTValues.VA[GTValues.LV])
                .inputItems(Ingredient.of(Blocks.DIRT))
                .chancedOutput(PLANT_BALL.asStack(), 1250, 700)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(ChemicalHelper.get(dustTiny, Clay), 4000, 900)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("grass").duration(250).EUt(GTValues.VA[GTValues.LV])
                .inputItems(Ingredient.of(Blocks.GRASS))
                .chancedOutput(PLANT_BALL.asStack(), 3000, 1200)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(ChemicalHelper.get(dustTiny, Clay), 5000, 900)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("mycelium").duration(650).EUt(GTValues.VA[GTValues.LV])
                .inputItems(Ingredient.of(Blocks.MYCELIUM))
                .chancedOutput(new ItemStack(Blocks.RED_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.BROWN_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(ChemicalHelper.get(dustTiny, Clay), 5000, 900)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_ash").duration(240).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, Ash)
                .chancedOutput(ChemicalHelper.get(dustSmall, Quicklime, 2), 9900, 0)
                .chancedOutput(ChemicalHelper.get(dustSmall, Potash), 6400, 0)
                .chancedOutput(ChemicalHelper.get(dustSmall, Magnesia), 6000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, PhosphorusPentoxide), 500, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, SodaAsh), 5000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, BandedIron), 2500, 0)
                .save(provider);


        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_dark_ash").duration(250).EUt(6)
                .inputItems(dust, DarkAsh)
                .outputItems(dust, Ash)
                .outputItems(dust, Carbon)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_glowstone").duration(488).EUt(80)
                .inputItems(dust, Glowstone)
                .outputItems(dustSmall, Redstone, 2)
                .outputItems(dustSmall, Gold, 2)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_coal").duration(36).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, Coal)
                .outputItems(dust, Carbon, 2)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_u238").duration(800).EUt(320)
                .inputItems(dust, Uranium238)
                .chancedOutput(dustTiny, Plutonium239, 200, 80)
                .chancedOutput(dustTiny, Uranium235, 2000, 350)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_p239").duration(1600).EUt(320)
                .inputItems(dust, Plutonium239)
                .chancedOutput(dustTiny, Uranium238, 3000, 450)
                .chancedOutput(dust, Plutonium241, 2000, 300)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_endstone").duration(320).EUt(20)
                .inputItems(dust, Endstone)
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 300)
                .chancedOutput(dustSmall, Tungstate, 1250, 450)
                .chancedOutput(dustTiny, Platinum, 625, 150)
                .outputFluids(Helium.getFluid(120))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_netherrack").duration(160).EUt(20)
                .inputItems(dust, Netherrack)
                .chancedOutput(dustTiny, Redstone, 5625, 850)
                .chancedOutput(dustTiny, Gold, 625, 120)
                .chancedOutput(dustSmall, Sulfur, 9900, 100)
                .chancedOutput(dustTiny, Coal, 5625, 850)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("soul_sand").duration(200).EUt(80)
                .inputItems(Ingredient.of(Blocks.SOUL_SAND))
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 130)
                .chancedOutput(dustSmall, Saltpeter, 8000, 480)
                .chancedOutput(dustTiny, Coal, 2000, 340)
                .outputFluids(Oil.getFluid(80))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("lava").duration(80).EUt(80)
                .inputFluids(Lava.getFluid(100))
                .chancedOutput(dustSmall, SiliconDioxide, 5000, 320)
                .chancedOutput(dustSmall, Magnesia, 1000, 270)
                .chancedOutput(dustSmall, Quicklime, 1000, 270)
                .chancedOutput(nugget, Gold, 250, 80)
                .chancedOutput(dustSmall, Sapphire, 1250, 270)
                .chancedOutput(dustSmall, Tantalite, 500, 130)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_rare_earch").duration(64).EUt(20)
                .inputItems(dust, RareEarth)
                .chancedOutput(dustSmall, Cadmium, 2500, 400)
                .chancedOutput(dustSmall, Neodymium, 2500, 400)
                .chancedOutput(dustSmall, Samarium, 2500, 400)
                .chancedOutput(dustSmall, Cerium, 2500, 400)
                .chancedOutput(dustSmall, Yttrium, 2500, 400)
                .chancedOutput(dustSmall, Lanthanum, 2500, 400)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("sand_block").duration(50).EUt(GTValues.VA[GTValues.LV])
                .inputItems(new ItemStack(Blocks.SAND, 8))
                .chancedOutput(dust, Iron, 5000, 500)
                .chancedOutput(dustTiny, Diamond, 100, 100)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Hydrogen.getName()).duration(160).EUt(20)
                .inputFluids(Hydrogen.getFluid(160))
                .outputFluids(Deuterium.getFluid(40))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Deuterium.getName()).duration(160).EUt(80)
                .inputFluids(Deuterium.getFluid(160))
                .outputFluids(Tritium.getFluid(40))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Helium.getName()).duration(160).EUt(80)
                .inputFluids(Helium.getFluid(80))
                .outputFluids(Helium3.getFluid(5))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(Air.getName()).duration(1600).EUt(GTValues.VA[GTValues.ULV])
                .inputFluids(Air.getFluid(10000))
                .outputFluids(Nitrogen.getFluid(3900))
                .outputFluids(Oxygen.getFluid(1000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(NetherAir.getName()).duration(1600).EUt(GTValues.VA[GTValues.MV])
                .inputFluids(NetherAir.getFluid(10000))
                .outputFluids(CarbonMonoxide.getFluid(3900))
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(EnderAir.getName()).duration(1600).EUt(GTValues.VA[GTValues.HV])
                .inputFluids(EnderAir.getFluid(10000))
                .outputFluids(NitrogenDioxide.getFluid(3900))
                .outputFluids(Deuterium.getFluid(1000))
                .save(provider);

        // Stone Dust
        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_stone").duration(480).EUt(GTValues.VA[GTValues.MV])
                .inputItems(dust, Stone)
                .outputItems(dustSmall, Quartzite)
                .outputItems(dustSmall, PotassiumFeldspar)
                .outputItems(dustTiny, Marble, 2)
                .outputItems(dustTiny, Biotite)
                .chancedOutput(dustTiny, MetalMixture, 7500, 750)
                .chancedOutput(dustTiny, Sodalite, 5000, 500)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_meta_mixture").duration(1000).EUt(900)
                .inputItems(dust, MetalMixture)
                .outputItems(dustSmall, BandedIron)
                .outputItems(dustSmall, Bauxite)
                .outputItems(dustTiny, Pyrolusite, 2)
                .outputItems(dustTiny, Barite)
                .chancedOutput(dustTiny, Chromite, 7500, 750)
                .chancedOutput(dustTiny, Ilmenite, 5000, 500)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_oil_sands").duration(200).EUt(5)
                .inputItems(dust, Oilsands)
                .outputFluids(Oil.getFluid(1000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_quartz_sand").duration(60).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, QuartzSand, 2)
                .outputItems(dust, Quartzite)
                .chancedOutput(dust, CertusQuartz, 2000, 200)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_red_alloy").duration(900).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, RedAlloy)
                .outputItems(dust, Redstone, 4)
                .outputItems(dust, Copper)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_blue_alloy").duration(1200).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, BlueAlloy)
                .outputItems(dust, Electrotine, 4)
                .outputItems(dust, Silver)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("dust_electrotine").duration(800).EUt(GTValues.VA[GTValues.LV])
                .inputItems(dust, Electrotine, 8)
                .outputItems(dust, Redstone)
                .outputItems(dust, Electrum)
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder("salt_water").duration(51).EUt(GTValues.VA[GTValues.LV])
                .inputFluids(SaltWater.getFluid(1000))
                .outputItems(dust, Salt, 2)
                .outputFluids(Water.getFluid(1000))
                .save(provider);

        // Electrolyzer
        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_sodium_bisulfate")
                .inputItems(dust, SodiumBisulfate, 7)
                .outputFluids(SodiumPersulfate.getFluid(500))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(150).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("salt_water")
                .inputFluids(SaltWater.getFluid(1000))
                .outputItems(dust, SodiumHydroxide, 3)
                .outputFluids(Chlorine.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(720).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_sphalerite")
                .inputItems(dust, Sphalerite, 2)
                .outputItems(dust, Zinc)
                .outputItems(dust, Sulfur)
                .chancedOutput(dustSmall, Gallium, 2000, 1000)
                .duration(200).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("water")
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(1500).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("distilled_water")
                .inputFluids(DistilledWater.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(1500).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dyes")
                .inputItems(Ingredient.of(TagUtil.createItemTag("dyes")))
                .outputItems(dust, Calcium)
                .duration(96).EUt(26).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("sand_block")
                .inputItems(Ingredient.of(Blocks.SAND))
                .outputItems(dust, SiliconDioxide)
                .duration(500).EUt(25).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_graphite")
                .inputItems(dust, Graphite)
                .outputItems(dust, Carbon, 4)
                .duration(100).EUt(60).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(AceticAcid.getName())
                .inputFluids(AceticAcid.getFluid(2000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(2000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(512).EUt(60).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(Chloromethane.getName())
                .inputFluids(Chloromethane.getFluid(2000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(Chlorine.getFluid(2000))
                .duration(400).EUt(60).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(Acetone.getName())
                .inputFluids(Acetone.getFluid(2000))
                .outputItems(dust, Carbon, 3)
                .outputFluids(Propane.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .duration(480).EUt(60).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(Butane.getName())
                .inputFluids(Butane.getFluid(1000))
                .outputFluids(Butene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(240).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(Butene.getName())
                .inputFluids(Butene.getFluid(1000))
                .outputFluids(Butadiene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(240).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(Propane.getName())
                .inputFluids(Propane.getFluid(1000))
                .outputFluids(Propene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(640).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_diamond")
                .inputItems(dust, Diamond)
                .outputItems(dust, Carbon, 64)
                .duration(768).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_trona")
                .inputItems(dust, Trona, 16)
                .outputItems(dust, SodaAsh, 6)
                .outputItems(dust, SodiumBicarbonate, 6)
                .outputFluids(Water.getFluid(2000))
                .duration(784).EUt(GTValues.VA[GTValues.LV] * 2L).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_bauxite")
                .inputItems(dust, Bauxite, 15)
                .outputItems(dust, Aluminium, 6)
                .outputItems(dust, Rutile)
                .outputFluids(Oxygen.getFluid(9000))
                .duration(270).EUt(GTValues.VA[GTValues.LV] * 2L).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_zeolite")
                .inputItems(dust, Zeolite, 41)
                .outputItems(dust, Sodium)
                .outputItems(dust, Calcium, 4)
                .outputItems(dust, Silicon, 27)
                .outputItems(dust, Aluminium, 9)
                .duration(656).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_bentonite")
                .inputItems(dust, Bentonite, 30)
                .outputItems(dust, Sodium)
                .outputItems(dust, Magnesium, 6)
                .outputItems(dust, Silicon, 12)
                .outputFluids(Water.getFluid(5000))
                .outputFluids(Hydrogen.getFluid(6000))
                .duration(480).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_tungstic_acid")
                .inputItems(dust, TungsticAcid, 7)
                .outputItems(dust, Tungsten)
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(4000))
                .duration(210).EUt(960).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_sodium_hydroxide")
                .inputItems(dust, SodiumHydroxide, 3)
                .outputItems(dust, Sodium)
                .outputFluids(Oxygen.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(150).EUt(60).save(provider);

        GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder("dust_sugar")
                .inputItems(dust, Sugar, 3)
                .outputItems(dust, Carbon)
                .outputFluids(Water.getFluid(1000))
                .duration(64).EUt(GTValues.VA[GTValues.LV]).save(provider);

        // Thermal Centrifuge
        GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("cobblestone")
                .inputItems(Ingredient.of(Blocks.COBBLESTONE))
                .outputItems(dust, Stone)
                .duration(500).EUt(48).save(provider);

        // Extractor
        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("dust_monazite")
                .inputItems(dust, Monazite)
                .outputItems(dustSmall, RareEarth)
                .outputFluids(Helium.getFluid(200))
                .duration(64).EUt(64).save(provider);

        // TODO Seed Oil
//        List<Tuple<ItemStack, Integer>> seedEntries = GTUtil.getGrassSeedEntries();
//        for (Tuple<ItemStack, Integer> seedEntry : seedEntries) {
//            EXTRACTOR_RECIPES.recipeBuilder()
//                    .duration(32).EUt(2)
//                    .inputItems(seedEntry.getFirst())
//                    .outputFluids(SeedOil.getFluid(10))
//                    .save(provider);
//        }

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("seed_oil.0").duration(32).EUt(2)
                .inputItems(Ingredient.of(Items.BEETROOT_SEEDS))
                .outputFluids(SeedOil.getFluid(10))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("seed_oil.1").duration(32).EUt(2)
                .inputItems(Ingredient.of(Items.MELON_SEEDS))
                .outputFluids(SeedOil.getFluid(3))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("seed_oil.2").duration(32).EUt(2)
                .inputItems(Ingredient.of(Items.PUMPKIN_SEEDS))
                .outputFluids(SeedOil.getFluid(6))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("fish_oil").duration(16).EUt(4)
                .inputItems(Ingredient.of(ItemTags.FISHES))
                .outputFluids(FishOil.getFluid(40))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("dust_quartzite").duration(600).EUt(28)
                .inputItems(dust, Quartzite)
                .outputFluids(Glass.getFluid(GTValues.L / 2))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("coal").duration(128).EUt(4)
                .inputItems(Ingredient.of(Items.COAL))
                .outputFluids(WoodTar.getFluid(100))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("dust_wool").duration(16).EUt(4)
                .inputItems(dust, Wood)
                .chancedOutput(PLANT_BALL.asStack(), 200, 30)
                .outputFluids(Creosote.getFluid(5))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("snowball").duration(32).EUt(4)
                .inputItems(Ingredient.of(Items.SNOWBALL))
                .outputFluids(Water.getFluid(250))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("snow").duration(128).EUt(4)
                .inputItems(Ingredient.of(Blocks.SNOW))
                .outputFluids(Water.getFluid(1000))
                .save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("bricks")
                .inputItems(Ingredient.of(Blocks.BRICKS))
                .outputItems(new ItemStack(Items.BRICK, 4))
                .duration(300).EUt(2).save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("clay")
                .inputItems(Ingredient.of(Blocks.CLAY))
                .outputItems(new ItemStack(Items.CLAY_BALL, 4))
                .duration(300).EUt(2).save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("nether_bricks")
                .inputItems(Ingredient.of(Blocks.NETHER_BRICKS))
                .outputItems(new ItemStack(Items.NETHER_BRICK, 4))
                .duration(300).EUt(2).save(provider);

        GTRecipeTypes.EXTRACTOR_RECIPES.recipeBuilder("bookshelf")
                .inputItems(Ingredient.of(Blocks.BOOKSHELF))
                .outputItems(new ItemStack(Items.BOOK, 3))
                .duration(300).EUt(2).save(provider);

    }
}
