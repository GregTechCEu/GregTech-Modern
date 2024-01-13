package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.PLANT_BALL;
import static com.gregtechceu.gtceu.common.data.GTItems.STICKY_RESIN;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class SeparationRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Centrifuge
        CENTRIFUGE_RECIPES.recipeBuilder("refinery_gas_separation")
                .inputFluids(RefineryGas.getFluid(8000))
                .outputFluids(Methane.getFluid(4000))
                .outputFluids(LPG.getFluid(4000))
                .duration(200).EUt(5).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("butane_separation")
                .inputFluids(Butane.getFluid(320))
                .outputFluids(LPG.getFluid(370))
                .duration(20).EUt(5).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("propane_separation")
                .inputFluids(Propane.getFluid(320))
                .outputFluids(LPG.getFluid(290))
                .duration(20).EUt(5).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("nitration_mixture_separation")
                .inputFluids(NitrationMixture.getFluid(2000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(SulfuricAcid.getFluid(1000))
                .duration(192).EUt(VA[LV]).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("reinforced_epoxy_resin_separation")
                .inputItems(dust, ReinforcedEpoxyResin)
                .outputItems(dust, Epoxy)
                .duration(24).EUt(5).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("oilsands_ore_separation")
                .inputItems(ore, Oilsands)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .outputFluids(Oil.getFluid(2000))
                .duration(200).EUt(30).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("oilsands_dust_separation")
                .inputItems(dust, Oilsands)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .outputFluids(OilHeavy.getFluid(2000))
                .duration(200).EUt(30).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("nether_wart_separation").duration(144).EUt(5)
                .inputItems(Items.NETHER_WART)
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("brown_mushroom_separation").duration(144).EUt(5)
                .inputItems(Blocks.BROWN_MUSHROOM.asItem())
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("red_mushroom_separation").duration(144).EUt(5)
                .inputItems(Blocks.RED_MUSHROOM.asItem())
                .outputFluids(Methane.getFluid(18))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("magma_cream_separation").duration(500).EUt(5)
                .inputItems(Items.MAGMA_CREAM)
                .outputItems(Items.BLAZE_POWDER)
                .outputItems(Items.SLIME_BALL)
                .save(provider);

        // TODO Food -> methane stuff
        /*
        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if (item instanceof ItemFood) {
                ItemFood itemFood = (ItemFood) item;
                Collection<ItemStack> subItems = GTUtility.getAllSubItems(new ItemStack(item, 1, GTValues.W));
                for (ItemStack itemStack : subItems) {
                    int healAmount = itemFood.getHealAmount(itemStack);
                    float saturationModifier = itemFood.getSaturationModifier(itemStack);
                    if (healAmount > 0) {
                        FluidStack outputStack = Methane.getFluid(Math.round(9 * healAmount * (1.0f + saturationModifier)));

                        CENTRIFUGE_RECIPES.recipeBuilder().duration(144).EUt(5)
                                .inputItems(itemStack)
                                .outputFluids(outputStack)
                                .save(provider;
                    }
                }
            }
        }
        */

        CENTRIFUGE_RECIPES.recipeBuilder("sticky_resin_separation").duration(400).EUt(5)
                .inputItems(STICKY_RESIN)
                .outputItems(dust, RawRubber, 3)
                .chancedOutput(PLANT_BALL.asStack(), 1000, 850)
                .outputFluids(Glue.getFluid(100))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("rubber_log_separation").duration(200).EUt(20)
                .inputItems(GTBlocks.RUBBER_LOG.asStack())
                .chancedOutput(STICKY_RESIN.asStack(), 5000, 1200)
                .chancedOutput(PLANT_BALL.asStack(), 3750, 900)
                .chancedOutput(dust, Carbon, 2500, 600)
                .chancedOutput(dust, Wood, 2500, 700)
                .outputFluids(Methane.getFluid(60))
                .save(provider);

        // TODO Other kinds of dirt?
        CENTRIFUGE_RECIPES.recipeBuilder("dirt_separation").duration(250).EUt(VA[LV])
                .inputItems(Blocks.DIRT.asItem())
                .chancedOutput(PLANT_BALL.asStack(), 1250, 700)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 4000, 900)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("grass_block_separation").duration(250).EUt(VA[LV])
                .inputItems(Blocks.GRASS.asItem())
                .chancedOutput(PLANT_BALL.asStack(), 3000, 1200)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 5000, 900)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("mycelium_separation").duration(650).EUt(VA[LV])
                .inputItems(new ItemStack(Blocks.MYCELIUM))
                .chancedOutput(new ItemStack(Blocks.RED_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.BROWN_MUSHROOM), 2500, 900)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 1200)
                .chancedOutput(dustTiny, Clay, 5000, 900)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("ash_separation").duration(240).EUt(VA[LV])
                .inputItems(dust, Ash)
                .chancedOutput(dustSmall, Quicklime, 2, 9900, 0)
                .chancedOutput(dustSmall, Potash, 6400, 0)
                .chancedOutput(dustSmall, Magnesia, 6000, 0)
                .chancedOutput(dustTiny, PhosphorusPentoxide, 500, 0)
                .chancedOutput(dustTiny, SodaAsh, 5000, 0)
                .chancedOutput(dustTiny, Hematite, 2500, 0)
                .save(provider);


        CENTRIFUGE_RECIPES.recipeBuilder("dark_ash_separation").duration(250).EUt(6)
                .inputItems(dust, DarkAsh)
                .outputItems(dust, Ash)
                .outputItems(dust, Carbon)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("glowstone_separation").duration(488).EUt(80)
                .inputItems(dust, Glowstone)
                .outputItems(dustSmall, Redstone, 2)
                .outputItems(dustSmall, Gold, 2)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("coal_separation").duration(36).EUt(VA[LV])
                .inputItems(dust, Coal)
                .outputItems(dust, Carbon, 2)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("uranium_238_separation").duration(800).EUt(320)
                .inputItems(dust, Uranium238)
                .chancedOutput(dustTiny, Plutonium239, 200, 80)
                .chancedOutput(dustTiny, Uranium235, 2000, 350)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("plutonium_239_separation").duration(1600).EUt(320)
                .inputItems(dust, Plutonium239)
                .chancedOutput(dustTiny, Uranium238, 3000, 450)
                .chancedOutput(dust, Plutonium241, 2000, 300)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("endstone_separation").duration(320).EUt(20)
                .inputItems(dust, Endstone)
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 300)
                .chancedOutput(dustSmall, Tungstate, 1250, 450)
                .chancedOutput(dustTiny, Platinum, 625, 150)
                .outputFluids(Helium.getFluid(120))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("netherrack_separation").duration(160).EUt(20)
                .inputItems(dust, Netherrack)
                .chancedOutput(dustTiny, Redstone, 5625, 850)
                .chancedOutput(dustTiny, Gold, 625, 120)
                .chancedOutput(dustSmall, Sulfur, 9900, 100)
                .chancedOutput(dustTiny, Coal, 5625, 850)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("soul_sand_separation").duration(200).EUt(80)
                .inputItems(Blocks.SOUL_SAND.asItem())
                .chancedOutput(new ItemStack(Blocks.SAND), 9000, 130)
                .chancedOutput(dustSmall, Saltpeter, 8000, 480)
                .chancedOutput(dustTiny, Coal, 2000, 340)
                .outputFluids(Oil.getFluid(80))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("lava_separation").duration(80).EUt(80)
                .inputFluids(Lava.getFluid(100))
                .chancedOutput(dustSmall, SiliconDioxide, 5000, 320)
                .chancedOutput(dustSmall, Magnesia, 1000, 270)
                .chancedOutput(dustSmall, Quicklime, 1000, 270)
                .chancedOutput(nugget, Gold, 250, 80)
                .chancedOutput(dustSmall, Sapphire, 1250, 270)
                .chancedOutput(dustSmall, Tantalite, 500, 130)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("rare_earth_separation").duration(64).EUt(20)
                .inputItems(dust, RareEarth)
                .chancedOutput(dustSmall, Cadmium, 2500, 400)
                .chancedOutput(dustSmall, Neodymium, 2500, 400)
                .chancedOutput(dustSmall, Samarium, 2500, 400)
                .chancedOutput(dustSmall, Cerium, 2500, 400)
                .chancedOutput(dustSmall, Yttrium, 2500, 400)
                .chancedOutput(dustSmall, Lanthanum, 2500, 400)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("red_sand_separation").duration(50).EUt(VA[LV])
                .inputItems(Blocks.RED_SAND.asItem())
                .chancedOutput(dust, Iron, 5000, 500)
                .chancedOutput(dustTiny, Diamond, 100, 100)
                .chancedOutput(new ItemStack(Blocks.SAND), 5000, 5000)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("hydrogen_separation").duration(160).EUt(20)
                .inputFluids(Hydrogen.getFluid(160))
                .outputFluids(Deuterium.getFluid(40))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("deuterium_separation").duration(160).EUt(80)
                .inputFluids(Deuterium.getFluid(160))
                .outputFluids(Tritium.getFluid(40))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("helium_separation").duration(160).EUt(80)
                .inputFluids(Helium.getFluid(80))
                .outputFluids(Helium3.getFluid(5))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("air_separation").duration(1600).EUt(VA[ULV])
                .inputFluids(Air.getFluid(10000))
                .outputFluids(Nitrogen.getFluid(3900))
                .outputFluids(Oxygen.getFluid(1000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("nether_air_separation").duration(1600).EUt(VA[MV])
                .inputFluids(NetherAir.getFluid(10000))
                .outputFluids(CarbonMonoxide.getFluid(3900))
                .outputFluids(SulfurDioxide.getFluid(1000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("ender_air_separation").duration(1600).EUt(VA[HV])
                .inputFluids(EnderAir.getFluid(10000))
                .outputFluids(NitrogenDioxide.getFluid(3900))
                .outputFluids(Deuterium.getFluid(1000))
                .save(provider);

        // Stone Dust
        CENTRIFUGE_RECIPES.recipeBuilder("stone_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Stone)
                .outputItems(dustSmall, Quartzite)
                .outputItems(dustSmall, PotassiumFeldspar)
                .outputItems(dustTiny, Marble, 2)
                .outputItems(dustTiny, Biotite)
                .chancedOutput(dustTiny, MetalMixture, 7500, 750)
                .chancedOutput(dustTiny, Sodalite, 5000, 500)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("metal_mixture_separation").duration(1000).EUt(900)
                .inputItems(dust, MetalMixture)
                .outputItems(dustSmall, Hematite)
                .outputItems(dustSmall, Bauxite)
                .outputItems(dustTiny, Pyrolusite, 2)
                .outputItems(dustTiny, Barite)
                .chancedOutput(dustTiny, Chromite, 7500, 750)
                .chancedOutput(dustTiny, Ilmenite, 5000, 500)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("quartz_sand_separation").duration(60).EUt(VA[LV])
                .inputItems(dust, QuartzSand, 2)
                .outputItems(dust, Quartzite)
                .chancedOutput(dust, CertusQuartz, 2000, 200)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("red_alloy_separation").duration(900).EUt(VA[LV])
                .inputItems(dust, RedAlloy)
                .outputItems(dust, Redstone, 4)
                .outputItems(dust, Copper)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("blue_alloy_separation").duration(1200).EUt(VA[LV])
                .inputItems(dust, BlueAlloy)
                .outputItems(dust, Electrotine, 4)
                .outputItems(dust, Silver)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("electrotine_separation").duration(800).EUt(VA[LV])
                .inputItems(dust, Electrotine, 8)
                .outputItems(dust, Redstone)
                .outputItems(dust, Electrum)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("salt_water_separation").duration(51).EUt(VA[LV])
                .inputFluids(SaltWater.getFluid(1000))
                .outputItems(dust, Salt, 2)
                .outputFluids(Water.getFluid(1000))
                .save(provider);

        // Electrolyzer
        ELECTROLYZER_RECIPES.recipeBuilder("sodium_bisulfate_electrolysis")
                .inputItems(dust, SodiumBisulfate, 7)
                .outputFluids(SodiumPersulfate.getFluid(500))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(150).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("salt_water_electrolysis")
                .inputFluids(SaltWater.getFluid(1000))
                .outputItems(dust, SodiumHydroxide, 3)
                .outputFluids(Chlorine.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(720).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("sphalerite_electrolysis")
                .inputItems(dust, Sphalerite, 2)
                .outputItems(dust, Zinc)
                .outputItems(dust, Sulfur)
                .chancedOutput(dustSmall, Gallium, 2000, 1000)
                .duration(200).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("water_electrolysis")
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(1500).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("distilled_water_electrolysis")
                .inputFluids(DistilledWater.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(1500).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("bone_meal_electrolysis")
                .inputItems(Items.BONE_MEAL, 3)
                .outputItems(dust, Calcium)
                .duration(96).EUt(26).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("sand_electrolysis")
                .inputItems(Blocks.SAND.asItem(), 8)
                .outputItems(dust, SiliconDioxide)
                .duration(500).EUt(25).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("graphite_electrolysis")
                .inputItems(dust, Graphite)
                .outputItems(dust, Carbon, 4)
                .duration(100).EUt(60).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("acetic_acid_electrolysis")
                .inputFluids(AceticAcid.getFluid(2000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(2000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(512).EUt(60).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("chloromethane_electrolysis")
                .inputFluids(Chloromethane.getFluid(2000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(Chlorine.getFluid(2000))
                .duration(400).EUt(60).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("acetone_electrolysis")
                .inputFluids(Acetone.getFluid(2000))
                .outputItems(dust, Carbon, 3)
                .outputFluids(Propane.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .duration(480).EUt(60).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("butane_electrolysis")
                .inputFluids(Butane.getFluid(1000))
                .outputFluids(Butene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(240).EUt(VA[MV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("butene_electrolysis")
                .inputFluids(Butene.getFluid(1000))
                .outputFluids(Butadiene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(240).EUt(VA[MV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("propane_electrolysis")
                .inputFluids(Propane.getFluid(1000))
                .outputFluids(Propene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(640).EUt(VA[MV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("diamond_electrolysis")
                .inputItems(dust, Diamond)
                .outputItems(dust, Carbon, 64)
                .duration(768).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("trona_electrolysis")
                .inputItems(dust, Trona, 16)
                .outputItems(dust, SodaAsh, 6)
                .outputItems(dust, SodiumBicarbonate, 6)
                .outputFluids(Water.getFluid(2000))
                .duration(784).EUt(VA[LV] * 2L).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("bauxite_electrolysis")
                .inputItems(dust, Bauxite, 15)
                .outputItems(dust, Aluminium, 6)
                .outputItems(dust, Rutile)
                .outputFluids(Oxygen.getFluid(9000))
                .duration(270).EUt(VA[LV] * 2L).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("zeolite_electrolysis")
                .inputItems(dust, Zeolite, 41)
                .outputItems(dust, Sodium)
                .outputItems(dust, Calcium, 4)
                .outputItems(dust, Silicon, 27)
                .outputItems(dust, Aluminium, 9)
                .duration(656).EUt(VA[MV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("bentonite_electrolysis")
                .inputItems(dust, Bentonite, 30)
                .outputItems(dust, Sodium)
                .outputItems(dust, Magnesium, 6)
                .outputItems(dust, Silicon, 12)
                .outputFluids(Water.getFluid(5000))
                .outputFluids(Hydrogen.getFluid(6000))
                .duration(480).EUt(VA[MV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("tungstic_acid_electrolysis")
                .inputItems(dust, TungsticAcid, 7)
                .outputItems(dust, Tungsten)
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Oxygen.getFluid(4000))
                .duration(210).EUt(960).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("sodium_hydroxide_electrolysis")
                .inputItems(dust, SodiumHydroxide, 3)
                .outputItems(dust, Sodium)
                .outputFluids(Oxygen.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(150).EUt(60).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("sugar_electrolysis")
                .inputItems(dust, Sugar, 3)
                .outputItems(dust, Carbon)
                .outputFluids(Water.getFluid(1000))
                .duration(64).EUt(VA[LV]).save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("apatite_electrolysis")
                .inputItems(dust, Apatite, 9)
                .outputItems(dust, Calcium, 5)
                .outputItems(dust, Phosphorus, 3)
                .outputFluids(Chlorine.getFluid(1000))
                .duration(288).EUt(60).save(provider);

        // Thermal Centrifuge
        // todo whatever this recipe is
        //THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
        //        .inputItems(new ItemStack(Blocks.COBBLESTONE, 1, GTValues.W))
        //        .outputItems(dust, Stone)
        //        .duration(500).EUt(48).save(provider);

        // Extractor
        EXTRACTOR_RECIPES.recipeBuilder("monazite_extraction")
                .inputItems(dust, Monazite)
                .outputItems(dustSmall, RareEarth)
                .outputFluids(Helium.getFluid(200))
                .duration(64).EUt(64).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("seed_oil_from_tag_seeds")
                .duration(32).EUt(2)
                .inputItems(CustomTags.SEEDS)
                .outputFluids(SeedOil.getFluid(10))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("seed_oil_from_beetroot").duration(32).EUt(2)
                .inputItems(new ItemStack(Items.BEETROOT_SEEDS))
                .outputFluids(SeedOil.getFluid(10))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("seed_oil_from_melon").duration(32).EUt(2)
                .inputItems(new ItemStack(Items.MELON_SEEDS, 1))
                .outputFluids(SeedOil.getFluid(3))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("seed_oil_from_pumpkin").duration(32).EUt(2)
                .inputItems(new ItemStack(Items.PUMPKIN_SEEDS, 1))
                .outputFluids(SeedOil.getFluid(6))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("fish_oil_from_cod").duration(16).EUt(4)
                .inputItems(Items.COD)
                .outputFluids(FishOil.getFluid(40))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("fish_oil_from_salmon").duration(16).EUt(4)
                .inputItems(Items.SALMON)
                .outputFluids(FishOil.getFluid(60))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("fish_oil_from_tropical_fish").duration(16).EUt(4)
                .inputItems(Items.TROPICAL_FISH)
                .outputFluids(FishOil.getFluid(70))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("fish_oil_from_pufferfish").duration(16).EUt(4)
                .inputItems(Items.PUFFERFISH)
                .outputFluids(FishOil.getFluid(30))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("quartzite_extraction").duration(600).EUt(28)
                .inputItems(dust, Quartzite)
                .outputFluids(Glass.getFluid(L / 2))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("charcoal_extraction").duration(128).EUt(4)
                .inputItems(Items.CHARCOAL)
                .outputFluids(WoodTar.getFluid(100))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("wood_dust_extraction").duration(16).EUt(4)
                .inputItems(dust, Wood)
                .chancedOutput(PLANT_BALL.asStack(), 200, 30)
                .outputFluids(Creosote.getFluid(5))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("snowball_extraction").duration(32).EUt(4)
                .inputItems(Items.SNOWBALL)
                .outputFluids(Water.getFluid(250))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("snow_block_extraction").duration(128).EUt(4)
                .inputItems(Blocks.SNOW.asItem())
                .outputFluids(Water.getFluid(1000))
                .save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("bricks_extraction")
                .inputItems(Blocks.BRICKS.asItem())
                .outputItems(Items.BRICK, 4)
                .duration(300).EUt(2).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("clay_extraction")
                .inputItems(Blocks.CLAY.asItem())
                .outputItems(Items.CLAY_BALL, 4)
                .duration(300).EUt(2).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("nether_bricks_extraction")
                .inputItems(Blocks.NETHER_BRICKS.asItem())
                .outputItems(Items.NETHER_BRICK, 4)
                .duration(300).EUt(2).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("bookshelf_extraction")
                .inputItems(Blocks.BOOKSHELF.asItem())
                .outputItems(Items.BOOK, 3)
                .duration(300).EUt(2).save(provider);
    }
}
