package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PlatGroupMetalsRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Primary Chain

        // Platinum Group Sludge Production
        CHEMICAL_RECIPES.recipeBuilder("pgs_from_cooperite").duration(50).EUt(VA[LV])
                .inputItems(crushedPurified, Cooperite)
                .inputFluids(NitricAcid.getFluid(100))
                .outputItems(dust, PlatinumGroupSludge, 4)
                .outputFluids(SulfuricNickelSolution.getFluid(1000))
                .save(provider);

        // Aqua Regia
        // HNO3 + HCl -> [HNO3 + HCl]
        MIXER_RECIPES.recipeBuilder("aqua_regia").duration(30).EUt(VA[LV])
                .inputFluids(NitricAcid.getFluid(1000))
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(AquaRegia.getFluid(3000))
                .save(provider);
    }
}
