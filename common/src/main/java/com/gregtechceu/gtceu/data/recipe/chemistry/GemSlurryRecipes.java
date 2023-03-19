package com.gregtechceu.gtceu.data.recipe.chemistry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;

public class GemSlurryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Ruby
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(RubySlurry.getName()).duration(280).EUt(GTValues.VA[GTValues.EV])
                .inputItems(dust, Ruby, 6)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(RubySlurry.getFluid(3000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(RubySlurry.getName()).duration(320).EUt(GTValues.VA[GTValues.HV])
                .inputFluids(RubySlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .outputItems(dust, Chrome)
                .chancedOutput(ChemicalHelper.get(dustTiny, Titanium), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Iron), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Vanadium), 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);

        // Sapphire
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(SapphireSlurry.getName()).duration(280).EUt(GTValues.VA[GTValues.EV])
                .inputItems(dust, Sapphire, 5)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(SapphireSlurry.getFluid(3000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(SapphireSlurry.getName()).duration(320).EUt(GTValues.VA[GTValues.HV])
                .inputFluids(SapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(ChemicalHelper.get(dustTiny, Titanium), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Iron), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Vanadium), 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);

        // Green Sapphire
        GTRecipeTypes.MIXER_RECIPES.recipeBuilder(GreenSapphireSlurry.getName()).duration(280).EUt(GTValues.VA[GTValues.EV])
                .inputItems(dust, GreenSapphire, 5)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(GreenSapphireSlurry.getFluid(3000))
                .save(provider);

        GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(GreenSapphireSlurry.getName()).duration(320).EUt(GTValues.VA[GTValues.HV])
                .inputFluids(GreenSapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(ChemicalHelper.get(dustTiny, Beryllium), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Titanium), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Iron), 2000, 0)
                .chancedOutput(ChemicalHelper.get(dustTiny, Vanadium), 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);
    }
}
