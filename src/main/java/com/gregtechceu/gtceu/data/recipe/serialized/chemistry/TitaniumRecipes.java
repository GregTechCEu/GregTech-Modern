package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BLAST_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;

public class TitaniumRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        titaniumProcess(provider);
    }

    private static void titaniumProcess(Consumer<FinishedRecipe> provider) {
        // Rutile extraction from Ilmenite
        // FeTiO3 + C -> Fe + TiO2 + CO
        BLAST_RECIPES.recipeBuilder("rutile_from_ilmenite")
                .inputItems(dust, Ilmenite, 5)
                .inputItems(dust, Carbon)
                .outputItems(ingot, WroughtIron)
                .outputItems(dust, Rutile, 3)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .blastFurnaceTemp(1700)
                .duration(1600).EUt(VA[HV]).save(provider);

        // Chloride Process
        // TiO2 + 2C + 4Cl -> TiCl4 + 2CO
        CHEMICAL_RECIPES.recipeBuilder("titanium_tetrachloride")
                .inputItems(dust, Carbon, 2)
                .inputItems(dust, Rutile)
                .inputFluids(Chlorine.getFluid(4000))
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .outputFluids(TitaniumTetrachloride.getFluid(1000))
                .duration(400).EUt(VA[HV]).save(provider);

        // Kroll Process
        // TiCl4 + 2Mg -> Ti + 2MgCl2
        BLAST_RECIPES.recipeBuilder("titanium_from_tetrachloride")
                .inputItems(dust, Magnesium, 2)
                .inputFluids(TitaniumTetrachloride.getFluid(1000))
                .outputItems(ingotHot, Titanium)
                .outputItems(dust, MagnesiumChloride, 6)
                .blastFurnaceTemp(Titanium.getBlastTemperature() + 200)
                .duration(800).EUt(VA[HV]).save(provider);
    }

    private static void solvayProcess() {
        // CaCO3 -> CaO + CO2

    }
}
