package com.gregtechceu.gtceu.data.recipe.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.LARGE_CHEMICAL_RECIPES;

public class AcidRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        sulfuricAcidRecipes(provider);
        nitricAcidRecipes(provider);
        phosphoricAcidRecipes(provider);
        aceticAcidRecipes(provider);
    }

    private static void sulfuricAcidRecipes(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder("sulfur_dioxide.0")
                .circuitMeta(2)
                .inputItems(dust, Sulfur)
                .inputFluids(Oxygen.getFluid(2000))
                .outputFluids(SulfurDioxide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sulfur_dioxide.1")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(3000))
                .inputFluids(HydrogenSulfide.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(SulfurDioxide.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sulfur_trioxide")
                .inputFluids(SulfurDioxide.getFluid(1000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(SulfurTrioxide.getFluid(1000))
                .duration(200).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sulfur_acid.0")
                .inputFluids(SulfurTrioxide.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputFluids(SulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sulfur_acid.1")
                .circuitMeta(2)
                .inputFluids(HydrogenSulfide.getFluid(1000))
                .inputFluids(Oxygen.getFluid(4000))
                .outputFluids(SulfuricAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("sulfur_acid.2")
                .circuitMeta(24)
                .inputItems(dust, Sulfur)
                .inputFluids(Water.getFluid(4000))
                .outputFluids(SulfuricAcid.getFluid(1000))
                .EUt(VA[HV])
                .duration(320)
                .save(provider);
    }

    private static void nitricAcidRecipes(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder("acid_ammonia")
                .circuitMeta(1)
                .inputFluids(Hydrogen.getFluid(3000))
                .inputFluids(Nitrogen.getFluid(1000))
                .outputFluids(Ammonia.getFluid(1000))
                .duration(320).EUt(384).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acid_water")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(5000))
                .inputFluids(Ammonia.getFluid(2000))
                .outputFluids(NitricOxide.getFluid(2000))
                .outputFluids(Water.getFluid(3000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acid_nitrogen_dioxide.0")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(NitricOxide.getFluid(1000))
                .outputFluids(NitrogenDioxide.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("acid_nitrogen_dioxide.1")
                .circuitMeta(3)
                .inputFluids(Nitrogen.getFluid(1000))
                .inputFluids(Oxygen.getFluid(2000))
                .outputFluids(NitrogenDioxide.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nitric_acid_oxide")
                .circuitMeta(1)
                .inputFluids(NitrogenDioxide.getFluid(3000))
                .inputFluids(Water.getFluid(1000))
                .outputFluids(NitricAcid.getFluid(2000))
                .outputFluids(NitricOxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nitric_acid")
                .circuitMeta(3)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(NitrogenDioxide.getFluid(2000))
                .outputFluids(NitricAcid.getFluid(2000))
                .duration(240).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("nitric_acid_water.0")
                .circuitMeta(24)
                .inputFluids(Oxygen.getFluid(4000))
                .inputFluids(Ammonia.getFluid(1000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(320).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("nitric_acid_water.1")
                .circuitMeta(24)
                .inputFluids(Nitrogen.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(3000))
                .inputFluids(Oxygen.getFluid(4000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(320).EUt(VA[HV]).save(provider);
    }

    private static void phosphoricAcidRecipes(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder("dust_phosphorus_pentoxide")
                .circuitMeta(1)
                .inputItems(dust, Phosphorus, 4)
                .inputFluids(Oxygen.getFluid(10000))
                .outputItems(dust, PhosphorusPentoxide, 14)
                .duration(40).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("phosphoric_acid.0")
                .inputItems(dust, PhosphorusPentoxide, 14)
                .inputFluids(Water.getFluid(6000))
                .outputFluids(PhosphoricAcid.getFluid(4000))
                .duration(40).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("phosphoric_acid.1")
                .inputItems(dust, Apatite, 9)
                .inputFluids(SulfuricAcid.getFluid(5000))
                .inputFluids(Water.getFluid(10000))
                .outputItems(dust, Gypsum, 40)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(PhosphoricAcid.getFluid(3000))
                .duration(320).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phosphoric_acid.2")
                .circuitMeta(24)
                .inputItems(dust, Phosphorus, 2)
                .inputFluids(Water.getFluid(3000))
                .inputFluids(Oxygen.getFluid(5000))
                .outputFluids(PhosphoricAcid.getFluid(2000))
                .duration(320).EUt(VA[LV]).save(provider);
    }

    private static void aceticAcidRecipes(Consumer<FinishedRecipe> provider) {

    }
}
