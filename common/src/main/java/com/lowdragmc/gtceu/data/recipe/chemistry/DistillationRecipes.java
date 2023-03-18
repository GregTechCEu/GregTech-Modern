package com.lowdragmc.gtceu.data.recipe.chemistry;


import com.lowdragmc.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.lowdragmc.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.lowdragmc.gtceu.api.data.chemical.ChemicalHelper;
import com.lowdragmc.gtceu.common.libs.GTItems;
import com.lowdragmc.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

import static com.lowdragmc.gtceu.api.recipe.OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.DISTILLATION_RECIPES;
import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.DISTILLERY_RECIPES;

public class DistillationRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("creosote")
                .inputFluids(Creosote.getFluid(24))
                .outputFluids(Lubricant.getFluid(12))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("diluted_hydrochloric_acid")
                .inputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(600).EUt(64), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("diluted_sulfuric_acid")
                .inputFluids(DilutedSulfuricAcid.getFluid(3000))
                .outputFluids(SulfuricAcid.getFluid(2000))
                .outputFluids(Water.getFluid(1000))
                .duration(600).EUt(VA[MV]), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("charcoal_byproducts")
                .inputFluids(CharcoalByproducts.getFluid(1000))
                .outputItems(dustSmall, Charcoal)
                .outputFluids(WoodTar.getFluid(250))
                .outputFluids(WoodVinegar.getFluid(400))
                .outputFluids(WoodGas.getFluid(250))
                .outputFluids(Dimethylbenzene.getFluid(100))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("wood_tar")
                .inputFluids(WoodTar.getFluid(1000))
                .outputFluids(Creosote.getFluid(300))
                .outputFluids(Phenol.getFluid(75))
                .outputFluids(Benzene.getFluid(350))
                .outputFluids(Toluene.getFluid(75))
                .outputFluids(Dimethylbenzene.getFluid(200))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("wood_vinegar")
                .inputFluids(WoodVinegar.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(100))
                .outputFluids(Water.getFluid(500))
                .outputFluids(Ethanol.getFluid(10))
                .outputFluids(Methanol.getFluid(300))
                .outputFluids(Acetone.getFluid(50))
                .outputFluids(MethylAcetate.getFluid(10))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("wood_gas")
                .inputFluids(WoodGas.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(490))
                .outputFluids(Ethylene.getFluid(20))
                .outputFluids(Methane.getFluid(130))
                .outputFluids(CarbonMonoxide.getFluid(340))
                .outputFluids(Hydrogen.getFluid(20))
                .duration(40).EUt(256), provider);

        DISTILLATION_RECIPES.recipeBuilder("water")
                .inputFluids(Water.getFluid(576))
                .outputFluids(DistilledWater.getFluid(520))
                .duration(160).EUt(VA[MV]).save(provider);

        DISTILLERY_RECIPES.recipeBuilder("water")
                .inputFluids(Water.getFluid(5))
                .circuitMeta(5)
                .outputFluids(DistilledWater.getFluid(5))
                .duration(16).EUt(10).save(provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("acetone")
                .inputFluids(Acetone.getFluid(1000))
                .outputFluids(Ethenone.getFluid(1000))
                .outputFluids(Methane.getFluid(1000))
                .duration(80).EUt(640), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("dissolved_calcium_acetate")
                .inputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .outputItems(dust, Quicklime, 2)
                .outputFluids(Acetone.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(80).EUt(VA[MV]), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("seed_oil")
                .inputFluids(SeedOil.getFluid(24))
                .outputFluids(Lubricant.getFluid(12))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("fish_oil")
                .inputFluids(FishOil.getFluid(1200))
                .outputFluids(Lubricant.getFluid(500))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("fermented_biomass")
                .inputFluids(FermentedBiomass.getFluid(1000))
                .outputItems(GTItems.FERTILIZER.asStack())
                .outputFluids(AceticAcid.getFluid(25))
                .outputFluids(Water.getFluid(375))
                .outputFluids(Ethanol.getFluid(150))
                .outputFluids(Methanol.getFluid(150))
                .outputFluids(Ammonia.getFluid(100))
                .outputFluids(CarbonDioxide.getFluid(400))
                .outputFluids(Methane.getFluid(600))
                .duration(75).EUt(180), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("biomass")
                .inputFluids(Biomass.getFluid(1000))
                .outputItems(dustSmall, Wood, 2)
                .outputFluids(Ethanol.getFluid(600))
                .outputFluids(Water.getFluid(300))
                .duration(32).EUt(400), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("coal_gas")
                .inputFluids(CoalGas.getFluid(1000))
                .outputItems(dustSmall, Coke)
                .outputFluids(CoalTar.getFluid(200))
                .outputFluids(Ammonia.getFluid(300))
                .outputFluids(Ethylbenzene.getFluid(250))
                .outputFluids(CarbonDioxide.getFluid(250))
                .duration(80).EUt(VA[MV]), provider);

        genDistilleryRecipes(DISTILLATION_RECIPES.recipeBuilder("goal_tar")
                .inputFluids(CoalTar.getFluid(1000))
                .outputItems(dustSmall, Coke)
                .outputFluids(Naphthalene.getFluid(400))
                .outputFluids(HydrogenSulfide.getFluid(300))
                .outputFluids(Creosote.getFluid(200))
                .outputFluids(Phenol.getFluid(100))
                .duration(80).EUt(VA[MV]), provider);

        DISTILLATION_RECIPES.recipeBuilder("liquid_air")
                .inputFluids(LiquidAir.getFluid(50000))
                .outputFluids(Nitrogen.getFluid(35000))
                .outputFluids(Oxygen.getFluid(11000))
                .outputFluids(CarbonDioxide.getFluid(2500))
                .outputFluids(Helium.getFluid(1000))
                .outputFluids(Argon.getFluid(500))
                .chancedOutput(ChemicalHelper.get(dust, Ice), 9000, 0)
                .duration(2000).EUt(VA[HV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("liquid_nether_air")
                .inputFluids(LiquidNetherAir.getFluid(100000))
                .outputFluids(CarbonMonoxide.getFluid(72000))
                .outputFluids(CoalGas.getFluid(10000))
                .outputFluids(HydrogenSulfide.getFluid(7500))
                .outputFluids(SulfurDioxide.getFluid(7500))
                .outputFluids(Helium3.getFluid(2500))
                .outputFluids(Neon.getFluid(500))
                .chancedOutput(ChemicalHelper.get(dustSmall, Ash), 9000, 0)
                .duration(2000).EUt(VA[EV]).save(provider);

        DISTILLATION_RECIPES.recipeBuilder("liquid_ender_air")
                .inputFluids(LiquidEnderAir.getFluid(200000))
                .outputFluids(NitrogenDioxide.getFluid(122000))
                .outputFluids(Deuterium.getFluid(50000))
                .outputFluids(Helium.getFluid(15000))
                .outputFluids(Tritium.getFluid(10000))
                .outputFluids(Krypton.getFluid(1000))
                .outputFluids(Xenon.getFluid(1000))
                .outputFluids(Radon.getFluid(1000))
                .chancedOutput(ChemicalHelper.get(dustTiny, EnderPearl), 9000, 0)
                .duration(2000).EUt(VA[IV]).save(provider);
    }

    public static void genDistilleryRecipes(GTRecipeBuilder recipeBuilder, Consumer<FinishedRecipe> provider) {
        var fluidOutputs = recipeBuilder.output.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
        var fluidInputs = recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
        var outputs = recipeBuilder.output.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
        for (int i = 0; i < fluidOutputs.size(); i++) {
            var builder = DISTILLERY_RECIPES.recipeBuilder(recipeBuilder.id.getPath() + "." + i)
                    .EUt(Math.max(1, recipeBuilder.EUt() / 4))
                    .circuitMeta(i + 1);

            int ratio = getRatioForDistillery(FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()),
                    FluidRecipeCapability.CAP.of(fluidOutputs.get(i).getContent()),
                    outputs.size() > 0 ? ItemRecipeCapability.CAP.of(outputs.get(0).getContent()).getItems()[0] : null);

            int recipeDuration = (int) (recipeBuilder.duration * STANDARD_OVERCLOCK_DURATION_DIVISOR);

            boolean shouldDivide = ratio != 1;

            boolean fluidsDivisible = isFluidStackDivisibleForDistillery(FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()), ratio) &&
                    isFluidStackDivisibleForDistillery(FluidRecipeCapability.CAP.of(fluidOutputs.get(i).getContent()), ratio);

            var dividedInputFluid = FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()).copy();
            dividedInputFluid.setAmount(Math.max(1, dividedInputFluid.getAmount() / ratio));

            var dividedOutputFluid = FluidRecipeCapability.CAP.of(fluidOutputs.get(i).getContent()).copy();
            dividedOutputFluid.setAmount(Math.max(1, dividedOutputFluid.getAmount() / ratio));

            if (shouldDivide && fluidsDivisible)
                builder.inputFluids(dividedInputFluid)
                        .outputFluids(dividedOutputFluid)
                        .duration(Math.max(1, recipeDuration / ratio));

            else if (!shouldDivide) {
                builder.inputFluids(FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()))
                        .outputFluids(FluidRecipeCapability.CAP.of(fluidOutputs.get(i).getContent()))
                        .duration(recipeDuration);
                if (outputs.size() > 0) {
                    builder.outputItems(ItemRecipeCapability.CAP.of(outputs.get(0).getContent()).getItems());
                }
                builder.save(provider);
                continue;
            }

            if (outputs.size() > 0) {
                boolean itemsDivisible = GTUtil.isItemStackCountDivisible(ItemRecipeCapability.CAP.of(outputs.get(0).getContent()).getItems()[0], ratio) && fluidsDivisible;

                if (fluidsDivisible && itemsDivisible) {
                    ItemStack stack = ItemRecipeCapability.CAP.of(outputs.get(0).getContent()).getItems()[0].copy();
                    stack.setCount(stack.getCount() / ratio);

                    builder.outputItems(stack);
                }
            }
            builder.save(provider);
        }
        recipeBuilder.save(provider);
    }

    private static int getRatioForDistillery(FluidStack fluidInput, FluidStack fluidOutput, ItemStack output) {
        int[] divisors = new int[]{2, 5, 10, 25, 50};
        int ratio = -1;

        for (int divisor : divisors) {

            if (!(isFluidStackDivisibleForDistillery(fluidInput, divisor)))
                continue;

            if (!(isFluidStackDivisibleForDistillery(fluidOutput, divisor)))
                continue;

            if (output != null && !(GTUtil.isItemStackCountDivisible(output, divisor)))
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    private static boolean isFluidStackDivisibleForDistillery(FluidStack fluidStack, int divisor) {
        return GTUtil.isFluidStackAmountDivisible(fluidStack, divisor) && fluidStack.getAmount() / divisor >= 25;
    }
}
