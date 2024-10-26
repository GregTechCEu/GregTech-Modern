package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.AIR_SCRUBBER_RECIPES;

public class AirScrubberRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        AIR_SCRUBBER_RECIPES.recipeBuilder("carbon_monoxide_poisoning")
                .circuitMeta(1)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .environmentalHazard(GTMedicalConditions.CARBON_MONOXIDE_POISONING)
                .duration(200).EUt(VHA[LV]).save(provider);

        /*
         * AIR_SCRUBBER_RECIPES.recipeBuilder("carcinogen")
         * .circuitMeta(2)
         * // TODO radioactive waste output
         * .environmentalHazard(GTMedicalConditions.CARCINOGEN)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("chemical_burns")
         * .circuitMeta(3)
         * .outputFluids(DilutedHydrochloricAcid.getFluid(500))
         * .outputFluids(DilutedSulfuricAcid.getFluid(750))
         * .environmentalHazard(GTMedicalConditions.CHEMICAL_BURNS)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("poison")
         * .circuitMeta(4)
         * .outputFluids(SulfurTrioxide.getFluid(1000))
         * .environmentalHazard(GTMedicalConditions.POISON)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("weak_poison")
         * .circuitMeta(5)
         * .outputFluids(NitricOxide.getFluid(1000))
         * .environmentalHazard(GTMedicalConditions.WEAK_POISON)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("irritant")
         * .circuitMeta(6)
         * .outputItems(dust, DarkAsh, 4)
         * .environmentalHazard(GTMedicalConditions.IRRITANT)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("nausea")
         * .circuitMeta(7)
         * .outputFluids(CarbonMonoxide.getFluid(50))
         * .environmentalHazard(GTMedicalConditions.NAUSEA)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("asbestosis")
         * .circuitMeta(8)
         * .outputItems(dust, Asbestos, 4)
         * .environmentalHazard(GTMedicalConditions.ASBESTOSIS)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("arsenicosis")
         * .circuitMeta(9)
         * .outputItems(dust, Arsenic, 4)
         * .environmentalHazard(GTMedicalConditions.ARSENICOSIS)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("silicosis")
         * .circuitMeta(10)
         * .outputItems(dust, SiliconDioxide, 4)
         * .environmentalHazard(GTMedicalConditions.SILICOSIS)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("berylliosis")
         * .circuitMeta(11)
         * .outputItems(dust, Beryllium, 4)
         * .environmentalHazard(GTMedicalConditions.BERYLLIOSIS)
         * .duration(200).EUt(VHA[LV]).save(provider);
         * 
         * AIR_SCRUBBER_RECIPES.recipeBuilder("methanol_poisoning")
         * .circuitMeta(12)
         * .outputFluids(Methanol.getFluid(1000))
         * .environmentalHazard(GTMedicalConditions.METHANOL_POISONING)
         * .duration(200).EUt(VHA[LV]).save(provider);
         */
    }
}
