package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;

public class BrineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        bromineProcess(provider);
        iodineProcess(provider);
    }

    private static void bromineProcess(Consumer<FinishedRecipe> provider) {
        EVAPORATION_RECIPES.recipeBuilder("brine_evaporation")
                .inputFluids(SaltWater.getFluid(20000))
                .outputFluids(RawBrine.getFluid(1000))
                .duration(1000).EUt(VA[HV]).save(provider);
        FLUID_HEATER_RECIPES.recipeBuilder("brine_heating")
                .inputFluids(RawBrine.getFluid(1000))
                .outputFluids(HotBrine.getFluid(1000))
                .duration(12000).EUt(VA[HV]).save(provider);

        // Main chain
        CHEMICAL_RECIPES.recipeBuilder("brine_chlorination")
                .inputFluids(HotBrine.getFluid(1000))
                .inputFluids(Chlorine.getFluid(1000))
                .outputFluids(HotChlorinatedBrominatedBrine.getFluid(2000))
                .duration(100).EUt(VA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("brine_filtration")
                .inputFluids(HotChlorinatedBrominatedBrine.getFluid(1000))
                .inputFluids(Chlorine.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(HotAlkalineDebrominatedBrine.getFluid(1000))
                .outputFluids(BrominatedChlorineVapor.getFluid(2000))
                .duration(300).EUt(VA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("brominated_chlorine_vapor_condensation")
                .inputFluids(BrominatedChlorineVapor.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputFluids(AcidicBromineSolution.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(200).EUt(VA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("bromine_vapor_concentration")
                .inputFluids(AcidicBromineSolution.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(ConcentratedBromineSolution.getFluid(1000))
                .outputFluids(AcidicBromineExhaust.getFluid(1000))
                .duration(100).EUt(VA[HV]).save(provider);
        DISTILLATION_RECIPES.recipeBuilder("bromine_distillation")
                .inputFluids(ConcentratedBromineSolution.getFluid(1000))
                .outputFluids(Chlorine.getFluid(500))
                .outputFluids(Bromine.getFluid(1000))
                .duration(500).EUt(VA[HV]).save(provider);

        // byproduct loop
        CHEMICAL_RECIPES.recipeBuilder("brine_neutralization")
                .inputFluids(HotAlkalineDebrominatedBrine.getFluid(3000))
                .inputItems(dust, Potassium, 1)
                .outputFluids(HotDebrominatedBrine.getFluid(2000))
                .outputItems(dust, RockSalt, 2)
                .duration(100).EUt(VA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("debrominated_brine_raw_brine_mixing")
                .inputFluids(RawBrine.getFluid(1000))
                .inputFluids(HotDebrominatedBrine.getFluid(1000))
                .outputFluids(HotBrine.getFluid(1000))
                .outputFluids(DebrominatedBrine.getFluid(1000))
                .duration(200).EUt(VA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("acidic_bromine_exhaust_heating")
                .inputFluids(AcidicBromineExhaust.getFluid(1000))
                .inputFluids(HotBrine.getFluid(1000))
                .outputFluids(HotChlorinatedBrominatedBrine.getFluid(1000))
                .outputFluids(Steam.getFluid(3000))
                .duration(100).EUt(VA[HV]).save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("debrominated_brine_decomposition")
                .inputFluids(DebrominatedBrine.getFluid(2000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(60).EUt(VA[MV]);
    }

    public static void iodineProcess(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("brine_acidification")
                .inputFluids(HotBrine.getFluid(2000))
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(HotAlkalineDebrominatedBrine.getFluid(2000))
                .outputFluids(HydrogenIodide.getFluid(1000))
                .duration(100).EUt(VHA[HV]).save(provider);
        CHEMICAL_RECIPES.recipeBuilder("iodine")
                .inputFluids(HydrogenIodide.getFluid(2000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(dust, Iodine, 1)
                .outputFluids(Water.getFluid(1000))
                .duration(1000).EUt(VHA[HV]).save(provider);
    }
}
