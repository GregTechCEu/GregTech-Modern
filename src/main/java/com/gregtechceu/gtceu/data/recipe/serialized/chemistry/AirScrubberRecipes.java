package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.AIR_SCRUBBER_RECIPES;

public class AirScrubberRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        AIR_SCRUBBER_RECIPES.recipeBuilder("chemical_burns")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .outputFluids(DilutedSulfuricAcid.getFluid(2000))
                .environmentalHazard(GTMedicalConditions.CHEMICAL_BURNS)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("poison")
                .circuitMeta(2)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(SulfurTrioxide.getFluid(2000))
                .environmentalHazard(GTMedicalConditions.POISON)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("weak_poison")
                .circuitMeta(3)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(NitricOxide.getFluid(2000))
                .environmentalHazard(GTMedicalConditions.WEAK_POISON)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("irritant")
                .circuitMeta(4)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, DarkAsh, 8)
                .environmentalHazard(GTMedicalConditions.IRRITANT)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("nausea")
                .circuitMeta(5)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(CarbonMonoxide.getFluid(50))
                .environmentalHazard(GTMedicalConditions.NAUSEA)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("carcinogen")
                .circuitMeta(6)
                .inputFluids(Air.getFluid(10000))
                // TODO radioactive waste output
                .environmentalHazard(GTMedicalConditions.CARCINOGEN)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("asbestosis")
                .circuitMeta(7)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, Asbestos, 8)
                .environmentalHazard(GTMedicalConditions.ASBESTOSIS)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("arsenicosis")
                .circuitMeta(8)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, Arsenic, 8)
                .environmentalHazard(GTMedicalConditions.ARSENICOSIS)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("silicosis")
                .circuitMeta(9)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, SiliconDioxide, 8)
                .environmentalHazard(GTMedicalConditions.SILICOSIS)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("berylliosis")
                .circuitMeta(10)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, Beryllium, 8)
                .environmentalHazard(GTMedicalConditions.BERYLLIOSIS)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("methanol_poisoning")
                .circuitMeta(11)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(Methanol.getFluid(1000))
                .environmentalHazard(GTMedicalConditions.METHANOL_POISONING)
                .duration(200).EUt(VHA[LV]).save(provider);

        AIR_SCRUBBER_RECIPES.recipeBuilder("carbon_monoxide_poisoning")
                .circuitMeta(12)
                .inputFluids(Air.getFluid(10000))
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .environmentalHazard(GTMedicalConditions.CARBON_MONOXIDE_POISONING)
                .duration(200).EUt(VHA[LV]).save(provider);
    }
}
