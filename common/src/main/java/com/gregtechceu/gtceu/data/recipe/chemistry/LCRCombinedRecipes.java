package com.gregtechceu.gtceu.data.recipe.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.api.GTValues.*;

public class LCRCombinedRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        LARGE_CHEMICAL_RECIPES.recipeBuilder("ammonia_caron")
                .circuitMeta(24)
                .inputFluids(Methane.getFluid(3000))
                .inputFluids(Nitrogen.getFluid(4000))
                .inputFluids(Oxygen.getFluid(3000))
                .outputFluids(Ammonia.getFluid(4000))
                .outputFluids(CarbonMonoxide.getFluid(3000))
                .EUt(VA[HV])
                .duration(320)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("methane_water")
                .circuitMeta(24)
                .inputFluids(Hydrogen.getFluid(6000))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .outputFluids(Methane.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .EUt(VA[LV])
                .duration(160)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phenol_acetone")
                .circuitMeta(24)
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(Benzene.getFluid(1000))
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(PhosphoricAcid.getFluid(100))
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(Acetone.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("phenol_hydrochloric_dilucted_hydrochloric")
                .circuitMeta(24)
                .inputFluids(Benzene.getFluid(1000))
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .duration(560).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("dust_salt")
                .circuitMeta(24)
                .inputFluids(Benzene.getFluid(2000))
                .inputFluids(Chlorine.getFluid(4000))
                .inputItems(dust, SodiumHydroxide, 6)
                .outputItems(dust, Salt, 4)
                .outputFluids(Phenol.getFluid(2000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .duration(1120).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder(Diesel.getName())
                .circuitMeta(24)
                .inputFluids(LightFuel.getFluid(20000))
                .inputFluids(HeavyFuel.getFluid(4000))
                .outputFluids(Diesel.getFluid(24000))
                .duration(100).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder(CetaneBoostedDiesel.getName() + ".0")
                .circuitMeta(24)
                .inputFluids(Diesel.getFluid(10000))
                .inputFluids(Tetranitromethane.getFluid(200))
                .outputFluids(CetaneBoostedDiesel.getFluid(10000))
                .duration(120).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder(CetaneBoostedDiesel.getName() + ".1")
                .circuitMeta(24)
                .inputFluids(BioDiesel.getFluid(10000))
                .inputFluids(Tetranitromethane.getFluid(400))
                .outputFluids(CetaneBoostedDiesel.getFluid(7500))
                .duration(120).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("acetone_oxygen")
                .inputFluids(AceticAcid.getFluid(3000))
                .notConsumable(ChemicalHelper.get(dust, Quicklime))
                .circuitMeta(24)
                .outputFluids(Acetone.getFluid(2000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(400).EUt(VA[HV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("dinitrogen_tetroxide_water")
                .circuitMeta(5)
                .inputFluids(Oxygen.getFluid(7000))
                .inputFluids(Nitrogen.getFluid(2000))
                .inputFluids(Hydrogen.getFluid(6000))
                .outputFluids(DinitrogenTetroxide.getFluid(1000))
                .outputFluids(Water.getFluid(3000))
                .duration(1100).EUt(VA[HV]).save(provider);
    }
}
