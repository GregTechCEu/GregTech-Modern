package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.DUST;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.AceticAcid;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class AntidoteRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        paracetamolProcess(provider);
        potassiumHydroxideProcess(provider);
        prussianBlueProcess(provider);
        dtpaProcess(provider);
    }

    private static void paracetamolProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("acetic_anhydride")
                .inputFluids(Ethenone.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .outputFluids(AceticAnhydride.getFluid(1000))
                .duration(200).EUt(VH[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("aminophenol")
                .inputFluids(Phenol.getFluid(1000))
                .inputFluids(NitrationMixture.getFluid(1000))
                .notConsumable(DUST, Iron)
                .outputFluids(AminoPhenol.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .duration(300).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("paracetamol")
                .inputFluids(AceticAnhydride.getFluid(1000))
                .inputFluids(AminoPhenol.getFluid(1000))
                .outputItems(DUST, Paracetamol, 1)
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);
    }

    private static void potassiumHydroxideProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("potassium_hydroxide")
                .inputItems(DUST, RockSalt, 2)
                .inputFluids(Water.getFluid(1000))
                .outputItems(DUST, PotassiumHydroxide, 3)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(100).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("potassium_iodide")
                .inputItems(DUST, PotassiumHydroxide, 3)
                .inputItems(DUST, Iodine, 1)
                .outputItems(DUST, PotassiumIodide, 1)
                .outputFluids(Oxygen.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(1000))
                .duration(100).EUt(VA[MV]).save(provider);
    }

    private static void prussianBlueProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("calcium_hydroxide")
                .inputItems(DUST, Quicklime, 2)
                .inputFluids(Water.getFluid(1000))
                .outputItems(DUST, CalciumHydroxide, 3)
                .duration(100).EUt(VHA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("formic_acid")
                .inputFluids(CarbonDioxide.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputFluids(FormicAcid.getFluid(1000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ammonium_formate")
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(FormicAcid.getFluid(1000))
                .outputFluids(AmmoniumFormate.getFluid(1000))
                .duration(100).EUt(VA[MV]).save(provider);

        FLUID_HEATER_RECIPES.recipeBuilder("formamide")
                .inputFluids(AmmoniumFormate.getFluid(100))
                .outputFluids(Formamide.getFluid(100))
                .duration(16).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("potassium_cyanide")
                .inputItems(DUST, PotassiumHydroxide, 3)
                .inputFluids(Formamide.getFluid(1000))
                .outputItems(DUST, PotassiumCyanide, 3)
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VHA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydrogen_cyanide")
                .inputFluids(Methane.getFluid(1000))
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(Oxygen.getFluid(3000))
                .notConsumable(DUST, Platinum)
                .outputFluids(HydrogenCyanide.getFluid(1000))
                .outputFluids(Water.getFluid(3000))
                .duration(100).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("potassium_carbonate")
                .inputItems(DUST, PotassiumHydroxide, 6)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(DUST, PotassiumCarbonate, 6)
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VHA[MV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("calcium_ferrocyanide")
                .inputFluids(HydrogenCyanide.getFluid(6000))
                .inputFluids(Iron2Chloride.getFluid(1000))
                .inputFluids(Water.getFluid(7000))
                .inputItems(DUST, CalciumHydroxide, 10)
                .outputItems(DUST, CalciumFerrocyanide, 15)
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .duration(300).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("potassium_ferrocyanide")
                .inputItems(DUST, CalciumFerrocyanide, 15)
                .inputItems(DUST, RockSalt, 8)
                .outputItems(DUST, PotassiumFerrocyanide, 17)
                .outputItems(DUST, CalciumHydroxide, 10)
                .outputFluids(HydrochloricAcid.getFluid(4000))
                .outputFluids(Water.getFluid(4000))
                .duration(300).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("prussian_blue")
                .inputItems(DUST, PotassiumFerrocyanide, 51)
                .inputFluids(Iron3Chloride.getFluid(4000))
                .outputItems(DUST, PrussianBlue, 1)
                .outputItems(DUST, RockSalt, 6)
                .duration(500).EUt(VA[HV]).save(provider);
    }

    public static void dtpaProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("dichloroethane")
                .circuitMeta(2)
                .inputFluids(Ethylene.getFluid(1000))
                .inputFluids(Chlorine.getFluid(2000))
                .notConsumableFluid(Iron3Chloride.getFluid(100))
                .outputFluids(Dichloroethane.getFluid(1000))
                .duration(100).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("diethylenetriamine")
                .inputFluids(Dichloroethane.getFluid(2000))
                .inputFluids(Ammonia.getFluid(3000))
                .outputFluids(Diethylenetriamine.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(4000))
                .duration(100).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("formaldehyde")
                .inputFluids(Methanol.getFluid(1000))
                .inputFluids(Oxygen.getFluid(1000))
                .notConsumable(DUST, Silver)
                .outputFluids(Formaldehyde.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(100).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glycolonitrile")
                .inputFluids(Formaldehyde.getFluid(1000))
                .inputFluids(HydrogenCyanide.getFluid(1000))
                .outputFluids(Glycolonitrile.getFluid(1000))
                .duration(100).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("diethylenetriamine_pentaacetonitrile")
                .inputFluids(Diethylenetriamine.getFluid(1000))
                .inputFluids(Glycolonitrile.getFluid(5000))
                .outputFluids(DiethylenetriaminePentaacetonitrile.getFluid(1000))
                .outputFluids(Water.getFluid(5000))
                .duration(100).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("diethylenetriaminepentaacetic_acid")
                .inputItems(DUST, SodiumHydroxide, 15)
                .inputFluids(DiethylenetriaminePentaacetonitrile.getFluid(1000))
                .inputFluids(Oxygen.getFluid(15000))
                .outputItems(DUST, DiethylenetriaminepentaaceticAcid, 1)
                .outputItems(DUST, SodiumNitrite, 20)
                .duration(100).EUt(VA[EV]).save(provider);
    }
}
