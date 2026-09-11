package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class TitaniumRecipes {

    private TitaniumRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        magnesiumRecoveryProcess(provider);
        solvayProcess(provider);
    }

    private static void magnesiumRecoveryProcess(Consumer<FinishedRecipe> provider) {
        // MgCl2 + 2Na -> Mg + 2NaCl
        CHEMICAL_RECIPES.recipeBuilder("salt_from_magnesium_chloride")
                .inputItems(dust, MagnesiumChloride, 3)
                .inputItems(dust, Sodium, 2)
                .outputItems(dust, Magnesium, 1)
                .outputItems(dust, Salt, 4)
                .duration(200).EUt(VA[HV]).save(provider);
    }

    private static void solvayProcess(Consumer<FinishedRecipe> provider) {
        // CaCO3 -> CaO + CO2
        CHEMICAL_RECIPES.recipeBuilder("quicklime_from_calcite")
                .circuitMeta(1)
                .inputItems(dust, Calcite, 5)
                .outputItems(dust, Quicklime, 2)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).save(provider);

        // NaCl(H2O) + CO2 + NH3 -> NH4Cl + NaHCO3
        CHEMICAL_RECIPES.recipeBuilder("sodium_bicarbonate_from_salt")
                .inputItems(dust, Salt, 2)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputItems(dust, AmmoniumChloride, 2)
                .outputItems(dust, SodiumBicarbonate, 6)
                .duration(400).EUt(VA[MV]).save(provider);

        // 2NaHCO3 -> Na2CO3 + CO2 + H2O
        ELECTROLYZER_RECIPES.recipeBuilder("soda_ash_from_bicarbonate")
                .inputItems(dust, SodiumBicarbonate, 12)
                .outputItems(dust, SodaAsh, 6)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);

        // 2NH4Cl + CaO -> CaCl2 + 2NH3 + H2O
        CHEMICAL_RECIPES.recipeBuilder("calcium_chloride_from_quicklime")
                .inputItems(dust, AmmoniumChloride, 4)
                .inputItems(dust, Quicklime, 2)
                .outputItems(dust, CalciumChloride, 3)
                .outputFluids(Ammonia.getFluid(2000))
                .outputFluids(Water.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);
    }
}