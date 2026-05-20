package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.FERTILIZER;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLERY_RECIPES;

public class DistillationRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        DISTILLATION_RECIPES.recipeBuilder("distill_creosote")
                .inputFluids(Creosote.getFluid(24))
                .outputFluids(Lubricant.getFluid(12))
                .duration(16).EUt(96).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_dilute_hcl")
                .inputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(600).EUt(64).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_dilute_sulfuric")
                .inputFluids(DilutedSulfuricAcid.getFluid(3000))
                .outputFluids(SulfuricAcid.getFluid(2000))
                .outputFluids(Water.getFluid(1000))
                .duration(600).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_charcoal_byproducts")
                .inputFluids(CharcoalByproducts.getFluid(1000))
                .chancedOutput(dust, Charcoal, 2500, 0)
                .outputFluids(WoodTar.getFluid(250))
                .outputFluids(WoodVinegar.getFluid(400))
                .outputFluids(WoodGas.getFluid(250))
                .outputFluids(Dimethylbenzene.getFluid(100))
                .duration(40).EUt(256).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_wood_tar")
                .inputFluids(WoodTar.getFluid(1000))
                .outputFluids(Creosote.getFluid(300))
                .outputFluids(Phenol.getFluid(75))
                .outputFluids(Benzene.getFluid(350))
                .outputFluids(Toluene.getFluid(75))
                .outputFluids(Dimethylbenzene.getFluid(200))
                .duration(40).EUt(256).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_wood_vinegar")
                .inputFluids(WoodVinegar.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(100))
                .outputFluids(Water.getFluid(500))
                .outputFluids(Ethanol.getFluid(10))
                .outputFluids(Methanol.getFluid(300))
                .outputFluids(Acetone.getFluid(50))
                .outputFluids(MethylAcetate.getFluid(10))
                .duration(40).EUt(256).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_wood_gas")
                .inputFluids(WoodGas.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(490))
                .outputFluids(Ethylene.getFluid(20))
                .outputFluids(Methane.getFluid(130))
                .outputFluids(CarbonMonoxide.getFluid(340))
                .outputFluids(Hydrogen.getFluid(20))
                .duration(40).EUt(256).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_water_large")
                .inputFluids(Water.getFluid(576))
                .outputFluids(DistilledWater.getFluid(520))
                .duration(160).EUt(VA[MV]).save(provider);

        DISTILLERY_RECIPES.recipeBuilder("distill_water_small")
                .inputFluids(Water.getFluid(5))
                .circuitMeta(5)
                .outputFluids(DistilledWater.getFluid(5))
                .duration(16).EUt(10).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_acetone")
                .inputFluids(Acetone.getFluid(1000))
                .outputFluids(Ethenone.getFluid(1000))
                .outputFluids(Methane.getFluid(1000))
                .duration(80).EUt(640).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_dissolved_calcium_acetate")
                .inputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .outputItems(dust, Quicklime, 2)
                .outputFluids(Acetone.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(80).EUt(VA[MV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_seed_oil")
                .inputFluids(SeedOil.getFluid(24))
                .outputFluids(Lubricant.getFluid(12))
                .duration(16).EUt(96).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_fish_oil")
                .inputFluids(FishOil.getFluid(1200))
                .outputFluids(Lubricant.getFluid(500))
                .duration(16).EUt(96).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_fermented_biomass")
                .inputFluids(FermentedBiomass.getFluid(1000))
                .outputItems(FERTILIZER)
                .outputFluids(AceticAcid.getFluid(25))
                .outputFluids(Water.getFluid(375))
                .outputFluids(Ethanol.getFluid(150))
                .outputFluids(Methanol.getFluid(150))
                .outputFluids(Ammonia.getFluid(100))
                .outputFluids(CarbonDioxide.getFluid(400))
                .outputFluids(Methane.getFluid(600))
                .duration(75).EUt(180).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_biomass")
                .inputFluids(Biomass.getFluid(1000))
                .chancedOutput(dust, Wood, 5000, 0)
                .outputFluids(Ethanol.getFluid(600))
                .outputFluids(Water.getFluid(300))
                .duration(32).EUt(400).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_coal_gas")
                .inputFluids(CoalGas.getFluid(1000))
                .chancedOutput(dust, Coke, 2500, 0)
                .outputFluids(CoalTar.getFluid(200))
                .outputFluids(Ammonia.getFluid(300))
                .outputFluids(Ethylbenzene.getFluid(250))
                .outputFluids(CarbonDioxide.getFluid(250))
                .duration(80).EUt(VA[MV])
                .save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_coal_tar")
                .inputFluids(CoalTar.getFluid(1000))
                .chancedOutput(dust, Coke, 2500, 0)
                .outputFluids(Naphthalene.getFluid(400))
                .outputFluids(HydrogenSulfide.getFluid(300))
                .outputFluids(Creosote.getFluid(200))
                .outputFluids(Phenol.getFluid(100))
                .duration(80).EUt(VA[MV])
                .save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_liquid_air")
                .inputFluids(LiquidAir.getFluid(50000))
                .outputFluids(Nitrogen.getFluid(35000))
                .outputFluids(Oxygen.getFluid(11000))
                .outputFluids(CarbonDioxide.getFluid(2500))
                .outputFluids(Helium.getFluid(1000))
                .outputFluids(Argon.getFluid(500))
                .chancedOutput(dust, Ice, 9000, 0)
                .disableDistilleryRecipes(true)
                .duration(2000).EUt(VA[HV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_liquid_nether_air")
                .inputFluids(LiquidNetherAir.getFluid(100000))
                .outputFluids(CarbonMonoxide.getFluid(72000))
                .outputFluids(CoalGas.getFluid(10000))
                .outputFluids(HydrogenSulfide.getFluid(7500))
                .outputFluids(SulfurDioxide.getFluid(7500))
                .outputFluids(Helium3.getFluid(2500))
                .outputFluids(Neon.getFluid(500))
                .chancedOutput(dust, Ash, 2250, 0)
                .disableDistilleryRecipes(true)
                .duration(2000).EUt(VA[EV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("distill_liquid_ender_air")
                .inputFluids(LiquidEnderAir.getFluid(200000))
                .outputFluids(NitrogenDioxide.getFluid(122000))
                .outputFluids(Deuterium.getFluid(50000))
                .outputFluids(Helium.getFluid(15000))
                .outputFluids(Tritium.getFluid(10000))
                .outputFluids(Krypton.getFluid(1000))
                .outputFluids(Xenon.getFluid(1000))
                .outputFluids(Radon.getFluid(1000))
                .chancedOutput(dust, EnderPearl, 1000, 0)
                .disableDistilleryRecipes(true)
                .duration(2000).EUt(VA[IV]).save(provider);
    }
}
