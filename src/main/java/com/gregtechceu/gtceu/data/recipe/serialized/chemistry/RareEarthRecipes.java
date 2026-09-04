package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class RareEarthRecipes {

    // TODO: Rare Earths for some reason gives *cadmium*, fix it at a later date or keep it here.
    public static void init(Consumer<FinishedRecipe> provider) {
        // Ln2O3 + 6HCl + 3H2O -> [2LnCl3 + 6H2O]aq
        MIXER_RECIPES.recipeBuilder("rare_earth_sludge_mixture_from_pure_rare_earth")
                .inputItems(dust, RareEarth, 4)
                .inputFluids(HydrochloricAcid.getFluid(6000))
                .inputFluids(Water.getFluid(3000))
                .outputFluids(RareEarthSludgeMixture.getFluid(1000))
                .duration(480).EUt(VA[HV]).save(provider);
        // [2LnCl3 + 6H2O]aq + 6NH3 -> LowLn(OH)3 + HighLn(OH)3 + 6NH4Cl
        CENTRIFUGE_RECIPES.recipeBuilder("rare_earth_sludge_dust_washing_from_sludge_mixture")
                .inputFluids(RareEarthSludgeMixture.getFluid(1000))
                .inputFluids(Ammonia.getFluid(6000))
                .outputItems(dust, AmmoniumChloride, 12)
                .outputItems(dust, LightRareEarthSludge, 2)
                .outputItems(dust, HeavyRareEarthSludge, 2)
                .duration(360).EUt(VA[HV]).save(provider);
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("sift_light_rare_earth_sludge")
                .inputItems(dust, LightRareEarthSludge, 1)
                .chancedOutput(dust, Cerium, 2800)
                .chancedOutput(dust, Neodymium, 3200)
                .chancedOutput(dust, Lanthanum, 1500)
                .duration(70).EUt(VA[EV]).save(provider);
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("sift_heavy_rare_earth_sludge")
                .inputItems(dust, HeavyRareEarthSludge, 1)
                .chancedOutput(dust, Cadmium, 1800)
                .chancedOutput(dust, Samarium, 2700)
                .chancedOutput(dust, Yttrium, 1800)
                .duration(70).EUt(VA[EV]).save(provider);
    }
}
