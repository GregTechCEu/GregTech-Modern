package com.gregtechceu.gtceu.data.recipe.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.api.GTValues.*;

public class NuclearRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Uraninite.getName()).duration(200).EUt(VA[LV])
                .inputItems(dust, Uraninite, 3)
                .inputFluids(HydrofluoricAcid.getFluid(4000))
                .inputFluids(Fluorine.getFluid(2000))
                .outputFluids(UraniumHexafluoride.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder(UraniumHexafluoride.getName()).duration(160).EUt(VA[HV])
                .inputFluids(UraniumHexafluoride.getFluid(1000))
                .outputFluids(EnrichedUraniumHexafluoride.getFluid(100))
                .outputFluids(DepletedUraniumHexafluoride.getFluid(900))
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder(EnrichedUraniumHexafluoride.getName()).duration(160).EUt(VA[MV])
                .inputFluids(EnrichedUraniumHexafluoride.getFluid(1000))
                .outputItems(dust, Uranium235)
                .outputFluids(Fluorine.getFluid(6000))
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder(DepletedUraniumHexafluoride.getName()).duration(160).EUt(VA[MV])
                .inputFluids(DepletedUraniumHexafluoride.getFluid(1000))
                .outputItems(dust, Uranium238)
                .outputFluids(Fluorine.getFluid(6000))
                .save(provider);

    }
}
