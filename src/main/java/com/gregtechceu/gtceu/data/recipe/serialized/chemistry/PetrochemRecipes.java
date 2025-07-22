package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PetrochemRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        moderatelyCrack(provider, Ethane, HydroCrackedEthane, SteamCrackedEthane);
        moderatelyCrack(provider, Ethylene, HydroCrackedEthylene, SteamCrackedEthylene);
        moderatelyCrack(provider, Propene, HydroCrackedPropene, SteamCrackedPropene);
        moderatelyCrack(provider, Propane, HydroCrackedPropane, SteamCrackedPropane);
        moderatelyCrack(provider, Butane, HydroCrackedButane, SteamCrackedButane);
        moderatelyCrack(provider, Butene, HydroCrackedButene, SteamCrackedButene);
        moderatelyCrack(provider, Butadiene, HydroCrackedButadiene, SteamCrackedButadiene);

        lightlyCrack(provider, HeavyFuel, LightlyHydroCrackedHeavyFuel, LightlySteamCrackedHeavyFuel);
        severelyCrack(provider, HeavyFuel, SeverelyHydroCrackedHeavyFuel, SeverelySteamCrackedHeavyFuel);
        lightlyCrack(provider, LightFuel, LightlyHydroCrackedLightFuel, LightlySteamCrackedLightFuel);
        severelyCrack(provider, LightFuel, SeverelyHydroCrackedLightFuel, SeverelySteamCrackedLightFuel);
        lightlyCrack(provider, Naphtha, LightlyHydroCrackedNaphtha, LightlySteamCrackedNaphtha);
        severelyCrack(provider, Naphtha, SeverelyHydroCrackedNaphtha, SeverelySteamCrackedNaphtha);
        lightlyCrack(provider, RefineryGas, LightlyHydroCrackedGas, LightlySteamCrackedGas);
        severelyCrack(provider, RefineryGas, SeverelyHydroCrackedGas, SeverelySteamCrackedGas);

        DISTILLATION_RECIPES.recipeBuilder("distill_oil")
                .inputFluids(Oil.getFluid(50))
                .outputFluids(SulfuricHeavyFuel.getFluid(15))
                .outputFluids(SulfuricLightFuel.getFluid(50))
                .outputFluids(SulfuricNaphtha.getFluid(20))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_light_oil")
                .inputFluids(OilLight.getFluid(150))
                .outputFluids(SulfuricHeavyFuel.getFluid(10))
                .outputFluids(SulfuricLightFuel.getFluid(20))
                .outputFluids(SulfuricNaphtha.getFluid(30))
                .outputFluids(SulfuricGas.getFluid(240))
                .duration(20).EUt(96).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_heavy_oil")
                .inputFluids(OilHeavy.getFluid(100))
                .outputFluids(SulfuricHeavyFuel.getFluid(250))
                .outputFluids(SulfuricLightFuel.getFluid(45))
                .outputFluids(SulfuricNaphtha.getFluid(15))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(288).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_raw_oil")
                .inputFluids(RawOil.getFluid(100))
                .outputFluids(SulfuricHeavyFuel.getFluid(10))
                .outputFluids(SulfuricLightFuel.getFluid(50))
                .outputFluids(SulfuricNaphtha.getFluid(150))
                .outputFluids(SulfuricGas.getFluid(60))
                .duration(20).EUt(96).save(provider);

        desulfurizationRecipes(provider);
        distillationRecipes(provider);
        distilleryRecipes(provider);
    }

    private static void desulfurizationRecipes(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("desulfurize_heavy_fuel")
                .inputFluids(SulfuricHeavyFuel.getFluid(8000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(HeavyFuel.getFluid(8000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("desulfurize_light_fuel")
                .inputFluids(SulfuricLightFuel.getFluid(12000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(LightFuel.getFluid(12000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("desulfurize_naphtha")
                .inputFluids(SulfuricNaphtha.getFluid(12000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(Naphtha.getFluid(12000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("desulfurize_refinery_gas")
                .inputFluids(SulfuricGas.getFluid(16000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("desulfurize_natural_gas")
                .inputFluids(NaturalGas.getFluid(16000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(RefineryGas.getFluid(16000))
                .duration(160).EUt(VA[LV]).save(provider);
    }

    private static void distillationRecipes(Consumer<FinishedRecipe> provider) {
        DISTILLATION_RECIPES.recipeBuilder("distill_refinery_gas")
                .inputFluids(RefineryGas.getFluid(1000))
                .outputFluids(Butane.getFluid(60))
                .outputFluids(Propane.getFluid(70))
                .outputFluids(Ethane.getFluid(100))
                .outputFluids(Methane.getFluid(750))
                .outputFluids(Helium.getFluid(20))
                .duration(240).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_ethane")
                .inputFluids(HydroCrackedEthane.getFluid(1000))
                .outputFluids(Methane.getFluid(2000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_ethane")
                .inputFluids(SteamCrackedEthane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_ethylene")
                .inputFluids(HydroCrackedEthylene.getFluid(1000))
                .outputFluids(Ethane.getFluid(1000))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_ethylene")
                .inputFluids(SteamCrackedEthylene.getFluid(1000))
                .outputItems(dust, Carbon)
                .outputFluids(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_propene")
                .inputFluids(HydroCrackedPropene.getFluid(1000))
                .outputFluids(Propane.getFluid(500))
                .outputFluids(Ethylene.getFluid(500))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_propene")
                .inputFluids(SteamCrackedPropene.getFluid(1000))
                .chancedOutput(dust, Carbon, 5000, 0)
                .outputFluids(Ethylene.getFluid(1000))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_propane")
                .inputFluids(HydroCrackedPropane.getFluid(1000))
                .outputFluids(Ethane.getFluid(1000))
                .outputFluids(Methane.getFluid(1000))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_propane")
                .inputFluids(SteamCrackedPropane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .outputFluids(Ethylene.getFluid(750))
                .outputFluids(Methane.getFluid(1250))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_butane")
                .inputFluids(HydroCrackedButane.getFluid(1000))
                .outputFluids(Propane.getFluid(750))
                .outputFluids(Ethane.getFluid(750))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_butane")
                .inputFluids(SteamCrackedButane.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(750))
                .outputFluids(Ethylene.getFluid(750))
                .outputFluids(Methane.getFluid(500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_butene")
                .inputFluids(HydroCrackedButene.getFluid(750))
                .outputFluids(Butane.getFluid(500))
                .outputFluids(Propene.getFluid(250))
                .outputFluids(Ethane.getFluid(250))
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_butene")
                .inputFluids(SteamCrackedButene.getFluid(1000))
                .chancedOutput(dust, Carbon, 2500, 0)
                .outputFluids(Propene.getFluid(250))
                .outputFluids(Ethylene.getFluid(1500))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_hydro_cracked_butadiene")
                .inputFluids(HydroCrackedButadiene.getFluid(1000))
                .outputFluids(Butene.getFluid(750))
                .outputFluids(Ethylene.getFluid(500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_steam_cracked_butadiene")
                .inputFluids(SteamCrackedButadiene.getFluid(1000))
                .chancedOutput(dust, Carbon, 5000, 0)
                .outputFluids(Propene.getFluid(125))
                .outputFluids(Ethylene.getFluid(250))
                .outputFluids(Methane.getFluid(1125))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_hydro_cracked_heavy_fuel")
                .inputFluids(LightlyHydroCrackedHeavyFuel.getFluid(1000))
                .outputFluids(LightFuel.getFluid(600))
                .outputFluids(Naphtha.getFluid(100))
                .outputFluids(Butane.getFluid(100))
                .outputFluids(Propane.getFluid(100))
                .outputFluids(Ethane.getFluid(75))
                .outputFluids(Methane.getFluid(75))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_hydro_cracked_heavy_fuel")
                .inputFluids(SeverelyHydroCrackedHeavyFuel.getFluid(1000))
                .outputFluids(LightFuel.getFluid(200))
                .outputFluids(Naphtha.getFluid(250))
                .outputFluids(Butane.getFluid(300))
                .outputFluids(Propane.getFluid(300))
                .outputFluids(Ethane.getFluid(175))
                .outputFluids(Methane.getFluid(175))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_steam_cracked_heavy_fuel")
                .inputFluids(LightlySteamCrackedHeavyFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_steam_cracked_heavy_fuel")
                .inputFluids(SeverelySteamCrackedHeavyFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_hydro_cracked_light_fuel")
                .inputFluids(LightlyHydroCrackedLightFuel.getFluid(1000))
                .outputFluids(Naphtha.getFluid(800))
                .outputFluids(Octane.getFluid(100))
                .outputFluids(Butane.getFluid(150))
                .outputFluids(Propane.getFluid(200))
                .outputFluids(Ethane.getFluid(125))
                .outputFluids(Methane.getFluid(125))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_hydro_cracked_light_fuel")
                .inputFluids(SeverelyHydroCrackedLightFuel.getFluid(1000))
                .outputFluids(Naphtha.getFluid(200))
                .outputFluids(Octane.getFluid(20))
                .outputFluids(Butane.getFluid(125))
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(1500))
                .outputFluids(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_steam_cracked_light_fuel")
                .inputFluids(LightlySteamCrackedLightFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_steam_cracked_light_fuel")
                .inputFluids(SeverelySteamCrackedLightFuel.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_hydro_cracked_naphtha")
                .inputFluids(LightlyHydroCrackedNaphtha.getFluid(1000))
                .outputFluids(Butane.getFluid(800))
                .outputFluids(Propane.getFluid(300))
                .outputFluids(Ethane.getFluid(250))
                .outputFluids(Methane.getFluid(250))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_hydro_cracked_naphtha")
                .inputFluids(SeverelyHydroCrackedNaphtha.getFluid(1000))
                .outputFluids(Butane.getFluid(125))
                .outputFluids(Propane.getFluid(125))
                .outputFluids(Ethane.getFluid(1500))
                .outputFluids(Methane.getFluid(1500))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_steam_cracked_naphtha")
                .inputFluids(LightlySteamCrackedNaphtha.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_steam_cracked_naphtha")
                .inputFluids(SeverelySteamCrackedNaphtha.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/3", 0)
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
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_hydro_cracked_gas")
                .inputFluids(LightlyHydroCrackedGas.getFluid(1000))
                .outputFluids(Methane.getFluid(1400))
                .outputFluids(Hydrogen.getFluid(1340))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_hydro_cracked_gas")
                .inputFluids(SeverelyHydroCrackedGas.getFluid(1000))
                .outputFluids(Methane.getFluid(1400))
                .outputFluids(Hydrogen.getFluid(4340))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_lightly_steam_cracked_gas")
                .inputFluids(LightlySteamCrackedGas.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .outputFluids(Propene.getFluid(45))
                .outputFluids(Ethane.getFluid(8))
                .outputFluids(Ethylene.getFluid(85))
                .outputFluids(Methane.getFluid(1026))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_severely_steam_cracked_gas")
                .inputFluids(SeverelySteamCrackedGas.getFluid(1000))
                .chancedOutput(dust, Carbon, "1/9", 0)
                .outputFluids(Propene.getFluid(8))
                .outputFluids(Ethane.getFluid(45))
                .outputFluids(Ethylene.getFluid(92))
                .outputFluids(Methane.getFluid(1018))
                .outputFluids(Helium.getFluid(20))
                .duration(120).EUt(VA[MV]).save(provider);
    }

    private static void distilleryRecipes(Consumer<FinishedRecipe> provider) {
        DISTILLERY_RECIPES.recipeBuilder("distill_toluene_to_light_fuel")
                .circuitMeta(1)
                .inputFluids(Toluene.getFluid(30))
                .outputFluids(LightFuel.getFluid(30))
                .duration(160).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder("distill_heavy_fuel_to_toluene")
                .circuitMeta(1)
                .inputFluids(HeavyFuel.getFluid(10))
                .outputFluids(Toluene.getFluid(4))
                .duration(16).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder("distill_heavy_fuel_to_benzene")
                .circuitMeta(2)
                .inputFluids(HeavyFuel.getFluid(10))
                .outputFluids(Benzene.getFluid(4))
                .duration(16).EUt(24).save(provider);

        DISTILLERY_RECIPES.recipeBuilder("distill_heavy_fuel_to_phenol")
                .circuitMeta(3)
                .inputFluids(HeavyFuel.getFluid(20))
                .outputFluids(Phenol.getFluid(5))
                .duration(32).EUt(24).save(provider);
    }

    private static void lightlyCrack(Consumer<FinishedRecipe> provider, Material raw, Material hydroCracked,
                                     Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder("lightly_hydro_crack_" + raw.getName())
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(80).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("lightly_hydro_crack_" + raw.getName())
                .circuitMeta(1)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(1000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(80).EUt(30).save(provider);

        CRACKING_RECIPES.recipeBuilder("lightly_steam_crack_" + raw.getName())
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(80).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("lightly_steam_crack_" + raw.getName())
                .circuitMeta(1)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(160).duration(VA[LV]).save(provider);
    }

    private static void moderatelyCrack(Consumer<FinishedRecipe> provider, Material raw, Material hydroCracked,
                                        Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder("hydro_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(120).EUt(180).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydro_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(60).EUt(VA[LV]).save(provider);

        CRACKING_RECIPES.recipeBuilder("steam_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(120).EUt(360).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("steam_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).save(provider);
    }

    private static void severelyCrack(Consumer<FinishedRecipe> provider, Material raw, Material hydroCracked,
                                      Material steamCracked) {
        CRACKING_RECIPES.recipeBuilder("severely_hydro_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(6000))
                .outputFluids(hydroCracked.getFluid(1000))
                .duration(160).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("severely_hydro_crack_" + raw.getName())
                .circuitMeta(2)
                .inputFluids(raw.getFluid(500))
                .inputFluids(Hydrogen.getFluid(3000))
                .outputFluids(hydroCracked.getFluid(250))
                .duration(160).EUt(VA[LV]).save(provider);

        CRACKING_RECIPES.recipeBuilder("severely_steam_crack_" + raw.getName())
                .circuitMeta(3)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(1000))
                .duration(160).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("severely_steam_crack_" + raw.getName())
                .circuitMeta(3)
                .inputFluids(raw.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(steamCracked.getFluid(500))
                .duration(240).EUt(VA[LV]).save(provider);
    }
}
