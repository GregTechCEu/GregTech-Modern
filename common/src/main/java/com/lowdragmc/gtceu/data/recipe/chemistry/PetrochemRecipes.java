package com.lowdragmc.gtceu.data.recipe.chemistry;

import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.api.GTValues.*;

public class PetrochemRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        moderatelyCrack(Ethane, HydroCrackedEthane, SteamCrackedEthane, provider);
        moderatelyCrack(Ethylene, HydroCrackedEthylene, SteamCrackedEthylene, provider);
        moderatelyCrack(Propene, HydroCrackedPropene, SteamCrackedPropene, provider);
        moderatelyCrack(Propane, HydroCrackedPropane, SteamCrackedPropane, provider);
        moderatelyCrack(Butane, HydroCrackedButane, SteamCrackedButane, provider);
        moderatelyCrack(Butene, HydroCrackedButene, SteamCrackedButene, provider);
        moderatelyCrack(Butadiene, HydroCrackedButadiene, SteamCrackedButadiene, provider);

        lightlyCrack(HeavyFuel, LightlyHydroCrackedHeavyFuel, LightlySteamCrackedHeavyFuel, provider);
        severelyCrack(HeavyFuel, SeverelyHydroCrackedHeavyFuel, SeverelySteamCrackedHeavyFuel, provider);
        lightlyCrack(LightFuel, LightlyHydroCrackedLightFuel, LightlySteamCrackedLightFuel, provider);
        severelyCrack(LightFuel, SeverelyHydroCrackedLightFuel, SeverelySteamCrackedLightFuel, provider);
        lightlyCrack(Naphtha, LightlyHydroCrackedNaphtha, LightlySteamCrackedNaphtha, provider);
        severelyCrack(Naphtha, SeverelyHydroCrackedNaphtha, SeverelySteamCrackedNaphtha, provider);
        lightlyCrack(RefineryGas, LightlyHydroCrackedGas, LightlySteamCrackedGas, provider);
        severelyCrack(RefineryGas, SeverelyHydroCrackedGas, SeverelySteamCrackedGas, provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(Oil.getName())
                .inputFluids(Oil.getFluid(50))
                .outputFluids(SulfuricHeavyFuel.getFluid(15))
                .outputFluids(SulfuricLightFuel.getFluid(50))
                .outputFluids(SulfuricNaphtha.getFluid(20))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(96), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(OilLight.getName())
                .inputFluids(OilLight.getFluid(150))
                .outputFluids(SulfuricHeavyFuel.getFluid(10))
                .outputFluids(SulfuricLightFuel.getFluid(20))
                .outputFluids(SulfuricNaphtha.getFluid(30))
                .outputFluids(SulfuricGas.getFluid(240))
                .duration(20).EUt(96), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(OilHeavy.getName())
                .inputFluids(OilHeavy.getFluid(100))
                .outputFluids(SulfuricHeavyFuel.getFluid(250))
                .outputFluids(SulfuricLightFuel.getFluid(45))
                .outputFluids(SulfuricNaphtha.getFluid(15))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(288), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(RawOil.getName())
                .inputFluids(RawOil.getFluid(100))
                .outputFluids(SulfuricHeavyFuel.getFluid(15))
                .outputFluids(SulfuricLightFuel.getFluid(50))
                .outputFluids(SulfuricNaphtha.getFluid(20))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(96), provider);

        desulfurizationRecipes(provider);
        distillationRecipes(provider);
        distilleryRecipes(provider);
    }

    private static void desulfurizationRecipes(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder(SulfuricHeavyFuel.getName() + "_" + Hydrogen.getName())
                .inputFluids(SulfuricHeavyFuel.getFluid(8000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(HeavyFuel.getFluid(8000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(SulfuricLightFuel.getName() + "_" + Hydrogen.getName())
                .inputFluids(SulfuricLightFuel.getFluid(12000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(LightFuel.getFluid(12000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(SulfuricNaphtha.getName() + "_" + Hydrogen.getName())
                .inputFluids(SulfuricNaphtha.getFluid(12000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(Naphtha.getFluid(12000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(SulfuricGas.getName() + "_" + Hydrogen.getName())
                .inputFluids(SulfuricGas.getFluid(16000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(NaturalGas.getName() + "_" + Hydrogen.getName())
                .inputFluids(NaturalGas.getFluid(16000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).save(provider);
    }

    private static void distillationRecipes(Consumer<FinishedRecipe> provider) {

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(RefineryGas.getName())
                .inputFluids(RefineryGas.getFluid(1000))
                .outputFluids(Butane.getFluid(60))
                .outputFluids(Propane.getFluid(70))
                .outputFluids(Ethane.getFluid(100))
                .outputFluids(Methane.getFluid(750))
                .outputFluids(Helium.getFluid(20))
                .duration(240).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedEthane.getName())
                .inputFluids(HydroCrackedEthane.getFluid(1000))
                .outputFluids(Methane.getFluid(2000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedEthane.getName())
                .inputFluids(SteamCrackedEthane.getFluid(1000))
                .outputItems(dustSmall, Carbon)
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedEthylene.getName())
                .inputFluids(HydroCrackedEthylene.getFluid(1000))
                .outputFluids(Ethane.getFluid(1000))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedEthylene.getName())
                .inputFluids(SteamCrackedEthylene.getFluid(1000))
                .outputItems(dust, Carbon)
                .outputFluids(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedPropene.getName())
                .inputFluids(HydroCrackedPropene.getFluid(1000))
                .outputFluids(Propane.getFluid(500))
                .outputFluids(Ethylene.getFluid(500))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedPropene.getName())
                .inputFluids(SteamCrackedPropene.getFluid(1000))
                .outputItems(dustSmall, Carbon, 2)
                .outputFluids(Ethylene.getFluid(1000))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedPropane.getName())
                .inputFluids(HydroCrackedPropane.getFluid(1000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedPropane.getName())
                .inputFluids(SteamCrackedPropane.getFluid(1000))
                .outputItems(dustSmall, Carbon)
                .outputFluids(Ethylene.getFluid(750))
                .outputFluids(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedButane.getName())
                .inputFluids(HydroCrackedButane.getFluid(1000))
                .outputFluids(Propane.getFluid(750))
                .outputFluids(Ethane.getFluid(750))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedButane.getName())
                .inputFluids(SteamCrackedButane.getFluid(1000))
                .outputItems(dustSmall, Carbon)
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(750))
                .outputFluids(Ethylene.getFluid(750))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedButene.getName())
                .inputFluids(HydroCrackedButene.getFluid(750))
                .outputFluids(Butane.getFluid(500))
                .outputFluids(Propene.getFluid(250))
                .outputFluids(Ethane.getFluid(250))
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedButene.getName())
                .inputFluids(SteamCrackedButene.getFluid(1000))
                .outputItems(dustSmall, Carbon)
                .outputFluids(Propene.getFluid(250))
                .outputFluids(Ethylene.getFluid(1500))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(HydroCrackedButadiene.getName())
                .inputFluids(HydroCrackedButadiene.getFluid(1000))
                .outputFluids(Butene.getFluid(750))
                .outputFluids(Ethylene.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SteamCrackedButadiene.getName())
                .inputFluids(SteamCrackedButadiene.getFluid(1000))
                .outputItems(dustSmall, Carbon, 2)
                .outputFluids(Propene.getFluid(125))
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(1125))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlyHydroCrackedHeavyFuel.getName())
                .inputFluids(LightlyHydroCrackedHeavyFuel.getFluid(1000))
                .outputFluids(LightFuel.getFluid(600))
                .outputFluids(Naphtha.getFluid(100))
                .outputFluids(Butane.getFluid(100))
                .outputFluids(Propane.getFluid(100))
                .outputFluids(Ethane.getFluid(75))
                .outputFluids(Methane.getFluid(75))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelyHydroCrackedHeavyFuel.getName())
                .inputFluids(SeverelyHydroCrackedHeavyFuel.getFluid(1000))
                .outputFluids(LightFuel.getFluid(200))
                .outputFluids(Naphtha.getFluid(250))
                .outputFluids(Butane.getFluid(300))
                .outputFluids(Propane.getFluid(300))
                .outputFluids(Ethane.getFluid(175))
                .outputFluids(Methane.getFluid(175))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlySteamCrackedHeavyFuel.getName())
                .inputFluids(LightlySteamCrackedHeavyFuel.getFluid(1000))
                .outputItems(dustTiny, Carbon)
                .outputFluids(LightFuel.getFluid(300))
                .outputFluids(Naphtha.getFluid(50))
                .outputFluids(Toluene.getFluid(25))
                .outputFluids(Benzene.getFluid(125))
                .outputFluids(Butene.getFluid(25))
                .outputFluids(Butadiene.getFluid(15))
                .outputFluids(Propane.getFluid(3))
                .outputFluids(Propene.getFluid(30))
                .outputFluids(Ethane.getFluid(5))
                .outputFluids(Ethylene.getFluid(50))
                .outputFluids(Methane.getFluid(50))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelySteamCrackedHeavyFuel.getName())
                .inputFluids(SeverelySteamCrackedHeavyFuel.getFluid(1000))
                .outputItems(dustTiny, Carbon, 3)
                .outputFluids(LightFuel.getFluid(100))
                .outputFluids(Naphtha.getFluid(125))
                .outputFluids(Toluene.getFluid(80))
                .outputFluids(Benzene.getFluid(400))
                .outputFluids(Butene.getFluid(80))
                .outputFluids(Butadiene.getFluid(50))
                .outputFluids(Propane.getFluid(10))
                .outputFluids(Propene.getFluid(100))
                .outputFluids(Ethane.getFluid(15))
                .outputFluids(Ethylene.getFluid(150))
                .outputFluids(Methane.getFluid(150))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlyHydroCrackedLightFuel.getName())
                .inputFluids(LightlyHydroCrackedLightFuel.getFluid(1000))
                .outputFluids(Naphtha.getFluid(800))
                .outputFluids(Octane.getFluid(100))
                .outputFluids(Butane.getFluid(150))
                .outputFluids(Propane.getFluid(200))
                .outputFluids(Ethane.getFluid(125))
                .outputFluids(Methane.getFluid(125))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelyHydroCrackedLightFuel.getName())
                .inputFluids(SeverelyHydroCrackedLightFuel.getFluid(1000))
                .outputFluids(Naphtha.getFluid(200))
                .outputFluids(Octane.getFluid(20))
                .outputFluids(Butane.getFluid(125))
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(1500))
                .outputFluids(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlySteamCrackedLightFuel.getName())
                .inputFluids(LightlySteamCrackedLightFuel.getFluid(1000))
                .outputItems(dustTiny, Carbon)
                .outputFluids(HeavyFuel.getFluid(150))
                .outputFluids(Naphtha.getFluid(400))
                .outputFluids(Toluene.getFluid(40))
                .outputFluids(Benzene.getFluid(200))
                .outputFluids(Butene.getFluid(75))
                .outputFluids(Butadiene.getFluid(60))
                .outputFluids(Propane.getFluid(20))
                .outputFluids(Propene.getFluid(150))
                .outputFluids(Ethane.getFluid(10))
                .outputFluids(Ethylene.getFluid(50))
                .outputFluids(Methane.getFluid(50))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelySteamCrackedLightFuel.getName())
                .inputFluids(SeverelySteamCrackedLightFuel.getFluid(1000))
                .outputItems(dustTiny, Carbon, 3)
                .outputFluids(HeavyFuel.getFluid(50))
                .outputFluids(Naphtha.getFluid(100))
                .outputFluids(Toluene.getFluid(30))
                .outputFluids(Benzene.getFluid(150))
                .outputFluids(Butene.getFluid(65))
                .outputFluids(Butadiene.getFluid(50))
                .outputFluids(Propane.getFluid(50))
                .outputFluids(Propene.getFluid(250))
                .outputFluids(Ethane.getFluid(50))
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlyHydroCrackedNaphtha.getName())
                .inputFluids(LightlyHydroCrackedNaphtha.getFluid(1000))
                .outputFluids(Butane.getFluid(800))
                .outputFluids(Propane.getFluid(300))
                .outputFluids(Ethane.getFluid(250))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelyHydroCrackedNaphtha.getName())
                .inputFluids(SeverelyHydroCrackedNaphtha.getFluid(1000))
                .outputFluids(Butane.getFluid(125))
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(1500))
                .outputFluids(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlySteamCrackedNaphtha.getName())
                .inputFluids(LightlySteamCrackedNaphtha.getFluid(1000))
                .outputItems(dustTiny, Carbon)
                .outputFluids(HeavyFuel.getFluid(75))
                .outputFluids(LightFuel.getFluid(150))
                .outputFluids(Toluene.getFluid(40))
                .outputFluids(Benzene.getFluid(150))
                .outputFluids(Butene.getFluid(80))
                .outputFluids(Butadiene.getFluid(150))
                .outputFluids(Propane.getFluid(15))
                .outputFluids(Propene.getFluid(200))
                .outputFluids(Ethane.getFluid(35))
                .outputFluids(Ethylene.getFluid(200))
                .outputFluids(Methane.getFluid(200))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelySteamCrackedNaphtha.getName())
                .inputFluids(SeverelySteamCrackedNaphtha.getFluid(1000))
                .outputItems(dustTiny, Carbon, 3)
                .outputFluids(HeavyFuel.getFluid(25))
                .outputFluids(LightFuel.getFluid(50))
                .outputFluids(Toluene.getFluid(20))
                .outputFluids(Benzene.getFluid(100))
                .outputFluids(Butene.getFluid(50))
                .outputFluids(Butadiene.getFluid(50))
                .outputFluids(Propane.getFluid(15))
                .outputFluids(Propene.getFluid(300))
                .outputFluids(Ethane.getFluid(65))
                .outputFluids(Ethylene.getFluid(500))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlyHydroCrackedGas.getName())
                .inputFluids(LightlyHydroCrackedGas.getFluid(1000))
                .outputFluids(Methane.getFluid(1400))
                .outputFluids(Hydrogen.getFluid(1340))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelyHydroCrackedGas.getName())
                .inputFluids(SeverelyHydroCrackedGas.getFluid(1000))
                .outputFluids(Methane.getFluid(1400))
                .outputFluids(Hydrogen.getFluid(4340))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(LightlySteamCrackedGas.getName())
                .inputFluids(LightlySteamCrackedGas.getFluid(1000))
                .outputItems(dustTiny, Carbon)
                .outputFluids(Propene.getFluid(45))
                .outputFluids(Ethane.getFluid(8))
                .outputFluids(Ethylene.getFluid(85))
                .outputFluids(Methane.getFluid(1026))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]), provider);

        DistillationRecipes.genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder(SeverelySteamCrackedGas.getName())
                .inputFluids(SeverelySteamCrackedGas.getFluid(1000))
                .outputItems(dustTiny, Carbon)
                .outputFluids(Propene.getFluid(8))
                .outputFluids(Ethane.getFluid(45))
                .outputFluids(Ethylene.getFluid(92))
                .outputFluids(Methane.getFluid(1018))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]), provider);
    }

    private static void distilleryRecipes(Consumer<FinishedRecipe> provider) {
        DISTILLERY_RECIPES.recipeBuilder(Toluene.getName())
                .circuitMeta(1)
                .inputFluids(Toluene.getFluid(30))
                .outputFluids(LightFuel.getFluid(30))
                .duration(160).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder(HeavyFuel.getName() + ".0")
                .circuitMeta(1)
                .inputFluids(HeavyFuel.getFluid(10))
                .outputFluids(Toluene.getFluid(4))
                .duration(16).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder(HeavyFuel.getName() + ".1")
                .circuitMeta(2)
                .inputFluids(HeavyFuel.getFluid(10))
                .outputFluids(Benzene.getFluid(4))
                .duration(16).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder(HeavyFuel.getName() + ".2")
                .circuitMeta(3)
                .inputFluids(HeavyFuel.getFluid(20))
                .outputFluids(Phenol.getFluid(5))
                .duration(32).EUt(24).save(provider);
    }

    private static void lightlyCrack(Material raw, Material hydroCracked, Material steamCracked, Consumer<FinishedRecipe> provider) {
        var id = raw.getName() + "_" + hydroCracked.getName() + "_" + steamCracked.getName() + ".lightly.";
        CRACKING_RECIPES.recipeBuilder(id + 0)
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(80).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 1)
                .circuitMeta(1)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(1000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(80).EUt(30).save(provider);

        CRACKING_RECIPES.recipeBuilder(id + 2)
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(80).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 3)
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(160).duration(VA[LV]).save(provider);
    }

    private static void moderatelyCrack(Material raw, Material hydroCracked, Material steamCracked, Consumer<FinishedRecipe> provider) {
        var id = raw.getName() + "_" + hydroCracked.getName() + "_" + steamCracked.getName() + ".moderately.";
        CRACKING_RECIPES.recipeBuilder(id + 0)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(120).EUt(180).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 1)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(60).EUt(VA[LV]).save(provider);

        CRACKING_RECIPES.recipeBuilder(id + 2)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(120).EUt(360).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 3)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).save(provider);
    }

    private static void severelyCrack(Material raw, Material hydroCracked, Material steamCracked, Consumer<FinishedRecipe> provider) {
        var id = raw.getName() + "_" + hydroCracked.getName() + "_" + steamCracked.getName() + ".severely.";
        CRACKING_RECIPES.recipeBuilder(id + 0)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(6000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(160).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 1)
                .circuitMeta(2)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(3000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(160).EUt(VA[LV]).save(provider);

        CRACKING_RECIPES.recipeBuilder(id + 2)
                .circuitMeta(3)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(160).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(id + 3)
                .circuitMeta(3)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).save(provider);
    }
}
