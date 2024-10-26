package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class NaquadahRecipes {

    // Rough ratio of Naquadah Dust breakdown from this process:
    //
    // 6 NAQUADAH DUST:
    // |> 1 Enriched Naquadah
    // |> 1 Naquadria
    // |> 1 Titanium
    // |> 1 Sulfur
    // |> 0.5 Indium
    // |> 0.5 Trinium
    // |> 0.5 Phosphorus
    // |> 0.25 Gallium
    // |> 0.25 Barium

    public static void init(Consumer<FinishedRecipe> provider) {
        // FLUOROANTIMONIC ACID

        CHEMICAL_RECIPES.recipeBuilder("antimony_trioxide").EUt(VA[ULV]).duration(60)
                .inputItems(dust, Antimony, 2)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, AntimonyTrioxide, 5)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("antimony_trifluoride").EUt(VA[LV]).duration(60)
                .inputItems(dust, AntimonyTrioxide, 5)
                .inputFluids(HydrofluoricAcid.getFluid(6000))
                .outputItems(dust, AntimonyTrifluoride, 8)
                .outputFluids(Water.getFluid(3000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("fluoroantimonic_acid").EUt(VA[HV]).duration(300)
                .inputItems(dust, AntimonyTrifluoride, 4)
                .inputFluids(HydrofluoricAcid.getFluid(4000))
                .outputFluids(FluoroantimonicAcid.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .save(provider);

        // STARTING POINT

        LARGE_CHEMICAL_RECIPES.recipeBuilder("naquadah_separation").EUt(VA[LuV]).duration(600)
                .inputFluids(FluoroantimonicAcid.getFluid(1000))
                .inputItems(dust, Naquadah, 6)
                .outputFluids(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .outputFluids(ImpureNaquadriaSolution.getFluid(2000))
                .outputItems(dust, TitaniumTrifluoride, 4)
                .save(provider);

        // ENRICHED NAQUADAH PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder("impure_enriched_naquadah_solution_separation").EUt(VA[EV]).duration(400)
                .inputFluids(ImpureEnrichedNaquadahSolution.getFluid(2000))
                .outputItems(dust, TriniumSulfide)
                .outputItems(dust, AntimonyTrifluoride, 2)
                .outputFluids(EnrichedNaquadahSolution.getFluid(1000))
                .save(provider);

        MIXER_RECIPES.recipeBuilder("enriched_naquadah_solution_separation").EUt(VA[HV]).duration(100)
                .inputFluids(EnrichedNaquadahSolution.getFluid(1000))
                .inputFluids(SulfuricAcid.getFluid(2000))
                .outputFluids(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("acidic_enriched_naquadah_separation").EUt(VA[HV]).duration(100)
                .inputFluids(AcidicEnrichedNaquadahSolution.getFluid(3000))
                .outputFluids(EnrichedNaquadahWaste.getFluid(2000))
                .outputFluids(Fluorine.getFluid(500))
                .outputItems(dust, EnrichedNaquadahSulfate, 6) // Nq+SO4
                .save(provider);

        BLAST_RECIPES.recipeBuilder("enriched_naquadah_sulfate_separation").EUt(VA[IV]).duration(500)
                .blastFurnaceTemp(7000)
                .inputItems(dust, EnrichedNaquadahSulfate, 6)
                .inputFluids(Hydrogen.getFluid(2000))
                .outputItems(ingotHot, NaquadahEnriched)
                .outputFluids(SulfuricAcid.getFluid(1000))
                .save(provider);

        DISTILLATION_RECIPES.recipeBuilder("enriched_naquadah_waste_separation").EUt(VA[HV]).duration(300)
                .inputFluids(EnrichedNaquadahWaste.getFluid(2000))
                .chancedOutput(dust, BariumSulfide, 5000, 0)
                .outputFluids(SulfuricAcid.getFluid(500))
                .outputFluids(EnrichedNaquadahSolution.getFluid(250))
                .outputFluids(NaquadriaSolution.getFluid(100))
                .save(provider);

        // NAQUADRIA PROCESS

        CENTRIFUGE_RECIPES.recipeBuilder("impure_naquadria_solution_separation").EUt(VA[EV]).duration(400)
                .inputFluids(ImpureNaquadriaSolution.getFluid(2000))
                .outputItems(dust, IndiumPhosphide)
                .outputItems(dust, AntimonyTrifluoride, 2)
                .outputFluids(NaquadriaSolution.getFluid(1000))
                .save(provider);

        MIXER_RECIPES.recipeBuilder("naquadria_solution_separation").EUt(VA[HV]).duration(100)
                .inputFluids(NaquadriaSolution.getFluid(1000))
                .inputFluids(SulfuricAcid.getFluid(2000))
                .outputFluids(AcidicNaquadriaSolution.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("acidic_naquadria_solution_separation").EUt(VA[HV]).duration(100)
                .inputFluids(AcidicNaquadriaSolution.getFluid(3000))
                .outputFluids(NaquadriaWaste.getFluid(2000))
                .outputFluids(Fluorine.getFluid(500))
                .outputItems(dust, NaquadriaSulfate, 6)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("naquadria_sulfate_separation").EUt(VA[ZPM]).duration(600).blastFurnaceTemp(9000)
                .inputItems(dust, NaquadriaSulfate, 6)
                .inputFluids(Hydrogen.getFluid(2000))
                .outputItems(ingotHot, Naquadria)
                .outputFluids(SulfuricAcid.getFluid(1000))
                .save(provider);

        DISTILLATION_RECIPES.recipeBuilder("naquadria_waste_separation").EUt(VA[HV]).duration(300)
                .inputFluids(NaquadriaWaste.getFluid(2000))
                .chancedOutput(dust, GalliumSulfide, 5000, 0)
                .outputFluids(SulfuricAcid.getFluid(500))
                .outputFluids(NaquadriaSolution.getFluid(250))
                .outputFluids(EnrichedNaquadahSolution.getFluid(100))
                .save(provider);

        // TRINIUM

        BLAST_RECIPES.recipeBuilder("trinium_sulfide_separation").duration(750).EUt(VA[LuV])
                .blastFurnaceTemp(Trinium.getBlastTemperature())
                .inputItems(dust, TriniumSulfide, 2)
                .inputItems(dust, Zinc)
                .outputItems(ingotHot, Trinium)
                .outputItems(dust, ZincSulfide, 2)
                .save(provider);

        // BYPRODUCT PROCESSING

        // Titanium Trifluoride
        BLAST_RECIPES.recipeBuilder("titanium_trifluoride_separation").EUt(VA[HV]).duration(900).blastFurnaceTemp(1941)
                .inputItems(dust, TitaniumTrifluoride, 4)
                .inputFluids(Hydrogen.getFluid(3000))
                .outputItems(ingotHot, Titanium)
                .outputFluids(HydrofluoricAcid.getFluid(3000))
                .save(provider);

        // Indium Phosphide
        CHEMICAL_RECIPES.recipeBuilder("indium_phosphide_separation").duration(30).EUt(VA[ULV])
                .inputItems(dust, IndiumPhosphide, 2)
                .inputItems(dust, Calcium)
                .outputItems(dust, Indium)
                .outputItems(dust, CalciumPhosphide, 2)
                .save(provider);
    }
}
