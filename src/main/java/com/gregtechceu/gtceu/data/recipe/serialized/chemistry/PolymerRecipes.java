package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PolymerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        polyethyleneProcess(provider);
        polyvinylChlorideProcess(provider);
        ptfeProcess(provider);
        epoxyProcess(provider);
        styreneButadieneProcess(provider);
        polybenzimidazoleProcess(provider);
        polycaprolactamProcess(provider);
    }

    private static void polyethyleneProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("ethylene_from_ethanol")
                .inputFluids(SulfuricAcid.getFluid(1000))
                .inputFluids(Ethanol.getFluid(1000))
                .outputFluids(Ethylene.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .duration(1200).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ethylene_from_glycerol")
                .inputFluids(Glycerol.getFluid(1000))
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(Ethylene.getFluid(2000))
                .outputFluids(Oxygen.getFluid(5000))
                .duration(400).EUt(200).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyethylene_from_air")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(1000))
                .inputFluids(Ethylene.getFluid(L))
                .outputFluids(Polyethylene.getFluid(L))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyethylene_from_oxygen")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(Ethylene.getFluid(L))
                .outputFluids(Polyethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("polyethylene_from_tetrachloride_air")
                .circuitMeta(3)
                .inputFluids(Air.getFluid(7500))
                .inputFluids(Ethylene.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(Polyethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("polyethylene_from_tetrachloride_oxygen")
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(7500))
                .inputFluids(Ethylene.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(Polyethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).save(provider);
    }

    private static void polyvinylChlorideProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("vinyl_chloride_from_hydrochloric")
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(VinylChloride.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("vinyl_chloride_from_chlorine")
                .circuitMeta(1)
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(VinylChloride.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("vinyl_chloride_from_ethane")
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Ethane.getFluid(1000))
                .outputFluids(VinylChloride.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(3000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyvinyl_chloride_from_air")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(1000))
                .inputFluids(VinylChloride.getFluid(L))
                .outputFluids(PolyvinylChloride.getFluid(L))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyvinyl_chloride_from_oxygen")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(VinylChloride.getFluid(L))
                .outputFluids(PolyvinylChloride.getFluid(216))
                .duration(160).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("polyvinyl_chloride_from_tetrachloride_air")
                .circuitMeta(2)
                .inputFluids(Air.getFluid(7500))
                .inputFluids(VinylChloride.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylChloride.getFluid(3240))
                .duration(800).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("polyvinyl_chloride_from_tetrachloride_oxygen")
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7500))
                .inputFluids(VinylChloride.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylChloride.getFluid(4320))
                .duration(800).EUt(VA[LV]).save(provider);
    }

    private static void ptfeProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("chloroform")
                .circuitMeta(1)
                .inputFluids(Chlorine.getFluid(6000))
                .inputFluids(Methane.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(3000))
                .outputFluids(Chloroform.getFluid(1000))
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("tetrafluoroethylene_from_chloroform")
                .inputFluids(Chloroform.getFluid(2000))
                .inputFluids(HydrofluoricAcid.getFluid(4000))
                .outputFluids(HydrochloricAcid.getFluid(6000))
                .outputFluids(Tetrafluoroethylene.getFluid(1000))
                .duration(480).EUt(240).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("tetrafluoroethylene_from_methane")
                .circuitMeta(24)
                .inputFluids(HydrofluoricAcid.getFluid(4000))
                .inputFluids(Methane.getFluid(2000))
                .inputFluids(Chlorine.getFluid(12000))
                .outputFluids(Tetrafluoroethylene.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(12000))
                .duration(540).EUt(VA[IV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ptfe_from_air")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(1000))
                .inputFluids(Tetrafluoroethylene.getFluid(L))
                .outputFluids(Polytetrafluoroethylene.getFluid(L))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ptfe_from_oxygen")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(Tetrafluoroethylene.getFluid(L))
                .outputFluids(Polytetrafluoroethylene.getFluid(216))
                .duration(160).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("ptfe_from_tetrachloride_air")
                .circuitMeta(2)
                .inputFluids(Air.getFluid(7500))
                .inputFluids(Tetrafluoroethylene.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(Polytetrafluoroethylene.getFluid(3240))
                .duration(800).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("ptfe_from_tetrachloride_oxygen")
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7500))
                .inputFluids(Tetrafluoroethylene.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(Polytetrafluoroethylene.getFluid(4320))
                .duration(800).EUt(VA[LV]).save(provider);
    }

    private static void epoxyProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("glycerol_from_seed_oil_methanol")
                .inputItems(dustTiny, SodiumHydroxide)
                .inputFluids(SeedOil.getFluid(6000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(Glycerol.getFluid(1000))
                .outputFluids(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glycerol_from_seed_oil_ethanol")
                .inputItems(dustTiny, SodiumHydroxide)
                .inputFluids(SeedOil.getFluid(6000))
                .inputFluids(Ethanol.getFluid(1000))
                .outputFluids(Glycerol.getFluid(1000))
                .outputFluids(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glycerol_from_fish_oil_methanol")
                .inputItems(dustTiny, SodiumHydroxide)
                .inputFluids(FishOil.getFluid(6000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(Glycerol.getFluid(1000))
                .outputFluids(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glycerol_from_fish_oil_ethanol")
                .inputItems(dustTiny, SodiumHydroxide)
                .inputFluids(FishOil.getFluid(6000))
                .inputFluids(Ethanol.getFluid(1000))
                .outputFluids(Glycerol.getFluid(1000))
                .outputFluids(BioDiesel.getFluid(6000))
                .duration(600).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("glycerol_from_seed_oil_methanol_9")
                .inputItems(dust, SodiumHydroxide)
                .inputFluids(SeedOil.getFluid(54000))
                .inputFluids(Methanol.getFluid(9000))
                .outputFluids(Glycerol.getFluid(9000))
                .outputFluids(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("glycerol_from_seed_oil_ethanol_9")
                .inputItems(dust, SodiumHydroxide)
                .inputFluids(SeedOil.getFluid(54000))
                .inputFluids(Ethanol.getFluid(9000))
                .outputFluids(Glycerol.getFluid(9000))
                .outputFluids(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("glycerol_from_fish_oil_methanol_9")
                .inputItems(dust, SodiumHydroxide)
                .inputFluids(FishOil.getFluid(54000))
                .inputFluids(Methanol.getFluid(9000))
                .outputFluids(Glycerol.getFluid(9000))
                .outputFluids(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("glycerol_from_fish_oil_ethanol_9")
                .inputItems(dust, SodiumHydroxide)
                .inputFluids(FishOil.getFluid(54000))
                .inputFluids(Ethanol.getFluid(9000))
                .outputFluids(Glycerol.getFluid(9000))
                .outputFluids(BioDiesel.getFluid(54000))
                .duration(5400).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("allyl_chloride")
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(Chlorine.getFluid(2000))
                .circuitMeta(1)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(AllylChloride.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("epichlorohydrin_from_glycerol")
                .inputFluids(Glycerol.getFluid(1000))
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .outputFluids(Epichlorohydrin.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("epichlorohydrin_from_allyl_chloride")
                .inputItems(dust, SodiumHydroxide, 3)
                .inputFluids(AllylChloride.getFluid(1000))
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .outputFluids(Epichlorohydrin.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("epichlorohydrin_shortcut_water")
                .circuitMeta(23)
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .inputItems(dust, SodiumHydroxide, 3)
                .outputFluids(Epichlorohydrin.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(640).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("epichlorohydrin_shortcut_hypochlorous")
                .circuitMeta(24)
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .inputItems(dust, SodiumHydroxide, 3)
                .outputFluids(Epichlorohydrin.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(640).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("phenol_from_cumene")
                .inputFluids(Oxygen.getFluid(2000))
                .inputFluids(Cumene.getFluid(1000))
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(Acetone.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("bisphenol_a")
                .circuitMeta(1)
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .inputFluids(Acetone.getFluid(1000))
                .inputFluids(Phenol.getFluid(2000))
                .outputFluids(BisphenolA.getFluid(1000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("epoxy_from_bisphenol_a")
                .inputItems(dust, SodiumHydroxide, 3)
                .inputFluids(Epichlorohydrin.getFluid(1000))
                .inputFluids(BisphenolA.getFluid(1000))
                .outputFluids(Epoxy.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(200).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("epoxy_shortcut")
                .circuitMeta(24)
                .inputFluids(Epichlorohydrin.getFluid(1000))
                .inputFluids(Phenol.getFluid(2000))
                .inputFluids(Acetone.getFluid(1000))
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .inputItems(dust, SodiumHydroxide, 3)
                .outputFluids(Epoxy.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);
    }

    private static void styreneButadieneProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("styrene_from_benzene")
                .inputFluids(Ethylene.getFluid(1000))
                .inputFluids(Benzene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Styrene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("styrene_from_ethylbenzene")
                .inputFluids(Ethylbenzene.getFluid(1000))
                .outputFluids(Styrene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .duration(30).EUt(VA[LV])
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("raw_sbr_from_air")
                .inputFluids(Butadiene.getFluid(3000))
                .inputFluids(Styrene.getFluid(1000))
                .inputFluids(Air.getFluid(15000))
                .outputItems(dust, RawStyreneButadieneRubber, 27)
                .duration(480).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("raw_sbr_from_oxygen")
                .inputFluids(Butadiene.getFluid(3000))
                .inputFluids(Styrene.getFluid(1000))
                .inputFluids(Oxygen.getFluid(15000))
                .outputItems(dust, RawStyreneButadieneRubber, 41)
                .duration(480).EUt(240).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("styrene_butadiene_rubber")
                .inputItems(dust, RawStyreneButadieneRubber, 9)
                .inputItems(dust, Sulfur)
                .outputFluids(StyreneButadieneRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).save(provider);
    }

    private static void polybenzimidazoleProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("polybenzimidazole").EUt(VA[IV]).duration(100)
                .inputFluids(Diaminobenzidine.getFluid(1000))
                .inputFluids(DiphenylIsophtalate.getFluid(1000))
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(Polybenzimidazole.getFluid(1008))
                .save(provider);

        // 3,3-Diaminobenzidine
        LARGE_CHEMICAL_RECIPES.recipeBuilder("diaminobenzidine").EUt(VA[IV]).duration(100)
                .inputFluids(Dichlorobenzidine.getFluid(1000))
                .inputFluids(Ammonia.getFluid(2000))
                .notConsumable(dust, Zinc)
                .outputFluids(Diaminobenzidine.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dichlorobenzidine").EUt(VA[EV]).duration(200)
                .inputItems(dustTiny, Copper)
                .inputFluids(Nitrochlorobenzene.getFluid(2000))
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Dichlorobenzidine.getFluid(1000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("dichlorobenzidine_9").EUt(VA[EV]).duration(1800)
                .inputItems(dust, Copper)
                .inputFluids(Nitrochlorobenzene.getFluid(18000))
                .inputFluids(Hydrogen.getFluid(18000))
                .outputFluids(Dichlorobenzidine.getFluid(9000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nitrochlorobenzene").EUt(VA[HV]).duration(100)
                .inputFluids(NitrationMixture.getFluid(2000))
                .inputFluids(Chlorobenzene.getFluid(1000))
                .outputFluids(Nitrochlorobenzene.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("chlorobenzene").EUt(VA[LV]).duration(240)
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Benzene.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Chlorobenzene.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .save(provider);

        // Diphenyl Isophthalate
        LARGE_CHEMICAL_RECIPES.recipeBuilder("diphenyl_isophtalate").EUt(VA[IV]).duration(100)
                .inputFluids(Phenol.getFluid(2000))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .inputFluids(PhthalicAcid.getFluid(1000))
                .outputFluids(DiphenylIsophtalate.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phthalic_acid_from_dimethylbenzene").EUt(VA[EV]).duration(100)
                .inputItems(dustTiny, PotassiumDichromate)
                .inputFluids(Dimethylbenzene.getFluid(1000))
                .inputFluids(Oxygen.getFluid(2000))
                .outputFluids(PhthalicAcid.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phthalic_acid_from_dimethylbenzene_9").EUt(VA[EV]).duration(900)
                .inputItems(dust, PotassiumDichromate)
                .inputFluids(Dimethylbenzene.getFluid(9000))
                .inputFluids(Oxygen.getFluid(18000))
                .outputFluids(PhthalicAcid.getFluid(9000))
                .outputFluids(Water.getFluid(18000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phthalic_acid_from_naphthalene").EUt(VA[LV]).duration(125)
                .inputFluids(Naphthalene.getFluid(2000))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .inputItems(dustTiny, Potassium)
                .outputFluids(PhthalicAcid.getFluid(2500))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phthalic_acid_from_naphthalene_9").EUt(VA[LV]).duration(1125)
                .inputFluids(Naphthalene.getFluid(18000))
                .inputFluids(SulfuricAcid.getFluid(9000))
                .inputItems(dust, Potassium)
                .outputFluids(PhthalicAcid.getFluid(22500))
                .outputFluids(HydrogenSulfide.getFluid(9000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("dimethylbenzene").EUt(VA[MV]).duration(4000)
                .inputFluids(Methane.getFluid(2000))
                .inputFluids(Benzene.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Dimethylbenzene.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("potassium_dichromate").EUt(VA[HV]).duration(100)
                .inputItems(dust, Saltpeter, 10)
                .inputItems(dust, ChromiumTrioxide, 8)
                .outputItems(dust, PotassiumDichromate, 11)
                .outputFluids(NitrogenDioxide.getFluid(2000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("chromium_trioxide").EUt(60).duration(100)
                .inputItems(dust, Chromium)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, ChromiumTrioxide, 4)
                .save(provider);
    }

    public static void polycaprolactamProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("cyclohexane").EUt(VA[HV]).duration(400)
                .notConsumable(dust, Nickel)
                .inputFluids(Benzene.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(6000))
                .outputFluids(Cyclohexane.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nitrosyl_chloride").EUt(VA[LV]).duration(100)
                .inputFluids(Chlorine.getFluid(1000))
                .inputFluids(NitricOxide.getFluid(1000))
                .outputFluids(NitrosylChloride.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("cyclohexanone_oxime").EUt(VA[MV]).duration(100)
                .inputFluids(Cyclohexane.getFluid(1000))
                .inputFluids(NitrosylChloride.getFluid(1000))
                .outputItems(dust, CyclohexanoneOxime, 19)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("caprolactam").EUt(VA[HV]).duration(200)
                .inputItems(dust, CyclohexanoneOxime, 19)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, Caprolactam, 19)
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .save(provider);

        BLAST_RECIPES.recipeBuilder("polycaprolactam").EUt(VA[MV]).duration(150).blastFurnaceTemp(533)
                .inputItems(dust, Caprolactam, 1)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(ingot, Polycaprolactam, 1)
                .save(provider);
    }
}
