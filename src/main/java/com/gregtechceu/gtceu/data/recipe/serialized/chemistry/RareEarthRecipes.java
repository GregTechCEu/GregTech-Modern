package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushedPurified;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class RareEarthRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Ln2O3 + 6HCl + 3H2O -> [2LnCl3 + 6H2O]aq
        MIXER_RECIPES.recipeBuilder("rare_earth_sludge_mixture_from_pure_rare_earth")
                .inputItems(dust, RareEarth, 4)
                .inputFluids(HydrochloricAcid.getFluid(6000))
                .inputFluids(Water.getFluid(3000))
                .outputFluids(RareEarthSludgeMixture.getFluid(1000))
                .duration(64).EUt(VA[MV]).save(provider);
        //[2LnCl3 + 6H2O]aq + 6NH3 -> LowLn(OH3) + HighLn(OH3) + 6NH4CL
        CENTRIFUGE_RECIPES.recipeBuilder("rare_earth_sludge_dust_washing_from_sludge_mixture")
                .inputFluids(RareEarthSludgeMixture.getFluid(1000))
                .inputFluids(Ammonia.getFluid(6000))
                .outputItems(dust, AmmoniumChloride, 12)
                .outputItems(dust, LightRareEarthSludge, 2)
                .outputItems(dust, HeavyRareEarthSludge, 2)
                .duration(64).EUt(VA[MV]).save(provider);
        //Basically a 66% chance to hit a dust per sludge batch,
        //so on average like 1-2 dusts per batch each category for both
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("sift_light_rare_earth_sludge")
                .inputItems(dust, LightRareEarthSludge, 1)
                .chancedOutput(dust, Cerium, 2200)
                .chancedOutput(dust, Neodymium, 3300)
                .chancedOutput(dust, Lanthanum, 2200)
                .duration(64).EUt(VA[MV]).save(provider);
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("sift_heavy_rare_earth_sludge")
                .inputItems(dust, HeavyRareEarthSludge, 1)
                .chancedOutput(dust, Cadmium, 2200)
                .chancedOutput(dust, Samarium, 3300)
                .chancedOutput(dust, Yttrium, 2200)
                .duration(64).EUt(VA[MV]).save(provider);



    }
}
