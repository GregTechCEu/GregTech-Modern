package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CENTRIFUGE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES;

public class GemSlurryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Ruby
        MIXER_RECIPES.recipeBuilder("ruby_slurry").duration(280).EUt(VA[EV])
                .inputItems(crushed, Ruby, 2)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(RubySlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("ruby_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(RubySlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .outputItems(dust, Chromium)
                .chancedOutput(dust, Titanium, 200, 0)
                .chancedOutput(dust, Iron, 200, 0)
                .chancedOutput(dust, Vanadium, 200, 0)
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .save(provider);

        // Sapphire
        MIXER_RECIPES.recipeBuilder("sapphire_slurry").duration(280).EUt(VA[EV])
                .inputItems(crushed, Sapphire, 2)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(SapphireSlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("sapphire_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(SapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(dust, Titanium, 200, 0)
                .chancedOutput(dust, Iron, 200, 0)
                .chancedOutput(dust, Vanadium, 200, 0)
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .save(provider);

        // Green Sapphire
        MIXER_RECIPES.recipeBuilder("green_sapphire_slurry").duration(280).EUt(VA[EV])
                .inputItems(crushed, GreenSapphire, 2)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(GreenSapphireSlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("green_sapphire_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(GreenSapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(dust, Beryllium, 200, 0)
                .chancedOutput(dust, Titanium, 200, 0)
                .chancedOutput(dust, Iron, 200, 0)
                .chancedOutput(dust, Vanadium, 200, 0)
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .save(provider);
    }
}
