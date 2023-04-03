package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CENTRIFUGE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES;

public class GemSlurryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Ruby
        MIXER_RECIPES.recipeBuilder("ruby_slurry").duration(280).EUt(VA[EV])
                .inputItems(dust, Ruby, 6)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(RubySlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("ruby_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(RubySlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .outputItems(dust, Chrome)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);

        // Sapphire
        MIXER_RECIPES.recipeBuilder("sapphire_slurry").duration(280).EUt(VA[EV])
                .inputItems(dust, Sapphire, 5)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(SapphireSlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("sapphire_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(SapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);

        // Green Sapphire
        MIXER_RECIPES.recipeBuilder("green_sapphire_slurry").duration(280).EUt(VA[EV])
                .inputItems(dust, GreenSapphire, 5)
                .inputFluids(AquaRegia.getFluid(3000))
                .outputFluids(GreenSapphireSlurry.getFluid(3000))
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("green_sapphire_slurry_centrifuging").duration(320).EUt(VA[HV])
                .inputFluids(GreenSapphireSlurry.getFluid(3000))
                .outputItems(dust, Aluminium, 2)
                .chancedOutput(dustTiny, Beryllium, 2000, 0)
                .chancedOutput(dustTiny, Titanium, 2000, 0)
                .chancedOutput(dustTiny, Iron, 2000, 0)
                .chancedOutput(dustTiny, Vanadium, 2000, 0)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(NitricAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .save(provider);
    }
}
