package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class NuclearRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("uranium_hexafluoride").duration(200).EUt(VA[LV])
                .inputItems(dust, Uraninite, 3)
                .inputFluids(HydrofluoricAcid.getFluid(4000))
                .inputFluids(Fluorine.getFluid(2000))
                .outputFluids(UraniumHexafluoride.getFluid(1000))
                .outputFluids(Water.getFluid(2000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("uranium_hexafluoride_separation").duration(160).EUt(VA[HV])
                .inputFluids(UraniumHexafluoride.getFluid(1000))
                .outputFluids(EnrichedUraniumHexafluoride.getFluid(100))
                .outputFluids(DepletedUraniumHexafluoride.getFluid(900))
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("enriched_uranium_hexafluoride_separation").duration(160).EUt(VA[MV])
                .inputFluids(EnrichedUraniumHexafluoride.getFluid(1000))
                .outputItems(dust, Uranium235)
                .outputFluids(Fluorine.getFluid(6000))
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("depleted_uranium_hexafluoride_separation").duration(160).EUt(VA[MV])
                .inputFluids(DepletedUraniumHexafluoride.getFluid(1000))
                .outputItems(dust, Uranium238)
                .outputFluids(Fluorine.getFluid(6000))
                .save(provider);
    }
}
