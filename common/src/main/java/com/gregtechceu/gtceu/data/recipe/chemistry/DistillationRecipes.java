package com.gregtechceu.gtceu.data.recipe.chemistry;


import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.libs.GTItems;
import com.gregtechceu.gtceu.common.libs.GTMaterials;
import com.gregtechceu.gtceu.common.libs.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;

public class DistillationRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("creosote")
                .inputFluids(GTMaterials.Creosote.getFluid(24))
                .outputFluids(GTMaterials.Lubricant.getFluid(12))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("diluted_hydrochloric_acid")
                .inputFluids(GTMaterials.DilutedHydrochloricAcid.getFluid(2000))
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .outputFluids(GTMaterials.HydrochloricAcid.getFluid(1000))
                .duration(600).EUt(64), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("diluted_sulfuric_acid")
                .inputFluids(GTMaterials.DilutedSulfuricAcid.getFluid(3000))
                .outputFluids(GTMaterials.SulfuricAcid.getFluid(2000))
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(600).EUt(GTValues.VA[GTValues.MV]), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("charcoal_byproducts")
                .inputFluids(GTMaterials.CharcoalByproducts.getFluid(1000))
                .outputItems(dustSmall, GTMaterials.Charcoal)
                .outputFluids(GTMaterials.WoodTar.getFluid(250))
                .outputFluids(GTMaterials.WoodVinegar.getFluid(400))
                .outputFluids(GTMaterials.WoodGas.getFluid(250))
                .outputFluids(GTMaterials.Dimethylbenzene.getFluid(100))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("wood_tar")
                .inputFluids(GTMaterials.WoodTar.getFluid(1000))
                .outputFluids(GTMaterials.Creosote.getFluid(300))
                .outputFluids(GTMaterials.Phenol.getFluid(75))
                .outputFluids(GTMaterials.Benzene.getFluid(350))
                .outputFluids(GTMaterials.Toluene.getFluid(75))
                .outputFluids(GTMaterials.Dimethylbenzene.getFluid(200))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("wood_vinegar")
                .inputFluids(GTMaterials.WoodVinegar.getFluid(1000))
                .outputFluids(GTMaterials.AceticAcid.getFluid(100))
                .outputFluids(GTMaterials.Water.getFluid(500))
                .outputFluids(GTMaterials.Ethanol.getFluid(10))
                .outputFluids(GTMaterials.Methanol.getFluid(300))
                .outputFluids(GTMaterials.Acetone.getFluid(50))
                .outputFluids(GTMaterials.MethylAcetate.getFluid(10))
                .duration(40).EUt(256), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("wood_gas")
                .inputFluids(GTMaterials.WoodGas.getFluid(1000))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(490))
                .outputFluids(GTMaterials.Ethylene.getFluid(20))
                .outputFluids(GTMaterials.Methane.getFluid(130))
                .outputFluids(GTMaterials.CarbonMonoxide.getFluid(340))
                .outputFluids(GTMaterials.Hydrogen.getFluid(20))
                .duration(40).EUt(256), provider);

        GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("water")
                .inputFluids(GTMaterials.Water.getFluid(576))
                .outputFluids(GTMaterials.DistilledWater.getFluid(520))
                .duration(160).EUt(GTValues.VA[GTValues.MV]).save(provider);

        GTRecipeTypes.DISTILLERY_RECIPES.recipeBuilder("water")
                .inputFluids(GTMaterials.Water.getFluid(5))
                .circuitMeta(5)
                .outputFluids(GTMaterials.DistilledWater.getFluid(5))
                .duration(16).EUt(10).save(provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("acetone")
                .inputFluids(GTMaterials.Acetone.getFluid(1000))
                .outputFluids(GTMaterials.Ethenone.getFluid(1000))
                .outputFluids(GTMaterials.Methane.getFluid(1000))
                .duration(80).EUt(640), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("dissolved_calcium_acetate")
                .inputFluids(GTMaterials.DissolvedCalciumAcetate.getFluid(1000))
                .outputItems(dust, GTMaterials.Quicklime, 2)
                .outputFluids(GTMaterials.Acetone.getFluid(1000))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputFluids(GTMaterials.Water.getFluid(1000))
                .duration(80).EUt(GTValues.VA[GTValues.MV]), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("seed_oil")
                .inputFluids(GTMaterials.SeedOil.getFluid(24))
                .outputFluids(GTMaterials.Lubricant.getFluid(12))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("fish_oil")
                .inputFluids(GTMaterials.FishOil.getFluid(1200))
                .outputFluids(GTMaterials.Lubricant.getFluid(500))
                .duration(16).EUt(96), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("fermented_biomass")
                .inputFluids(GTMaterials.FermentedBiomass.getFluid(1000))
                .outputItems(GTItems.FERTILIZER.asStack())
                .outputFluids(GTMaterials.AceticAcid.getFluid(25))
                .outputFluids(GTMaterials.Water.getFluid(375))
                .outputFluids(GTMaterials.Ethanol.getFluid(150))
                .outputFluids(GTMaterials.Methanol.getFluid(150))
                .outputFluids(GTMaterials.Ammonia.getFluid(100))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(400))
                .outputFluids(GTMaterials.Methane.getFluid(600))
                .duration(75).EUt(180), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("biomass")
                .inputFluids(GTMaterials.Biomass.getFluid(1000))
                .outputItems(dustSmall, GTMaterials.Wood, 2)
                .outputFluids(GTMaterials.Ethanol.getFluid(600))
                .outputFluids(GTMaterials.Water.getFluid(300))
                .duration(32).EUt(400), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("coal_gas")
                .inputFluids(GTMaterials.CoalGas.getFluid(1000))
                .outputItems(dustSmall, GTMaterials.Coke)
                .outputFluids(GTMaterials.CoalTar.getFluid(200))
                .outputFluids(GTMaterials.Ammonia.getFluid(300))
                .outputFluids(GTMaterials.Ethylbenzene.getFluid(250))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(250))
                .duration(80).EUt(GTValues.VA[GTValues.MV]), provider);

        genDistilleryRecipes(GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("goal_tar")
                .inputFluids(GTMaterials.CoalTar.getFluid(1000))
                .outputItems(dustSmall, GTMaterials.Coke)
                .outputFluids(GTMaterials.Naphthalene.getFluid(400))
                .outputFluids(GTMaterials.HydrogenSulfide.getFluid(300))
                .outputFluids(GTMaterials.Creosote.getFluid(200))
                .outputFluids(GTMaterials.Phenol.getFluid(100))
                .duration(80).EUt(GTValues.VA[GTValues.MV]), provider);

        GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("liquid_air")
                .inputFluids(GTMaterials.LiquidAir.getFluid(50000))
                .outputFluids(GTMaterials.Nitrogen.getFluid(35000))
                .outputFluids(GTMaterials.Oxygen.getFluid(11000))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(2500))
                .outputFluids(GTMaterials.Helium.getFluid(1000))
                .outputFluids(GTMaterials.Argon.getFluid(500))
                .chancedOutput(ChemicalHelper.get(dust, GTMaterials.Ice), 9000, 0)
                .duration(2000).EUt(GTValues.VA[GTValues.HV]).save(provider);

        GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("liquid_nether_air")
                .inputFluids(GTMaterials.LiquidNetherAir.getFluid(100000))
                .outputFluids(GTMaterials.CarbonMonoxide.getFluid(72000))
                .outputFluids(GTMaterials.CoalGas.getFluid(10000))
                .outputFluids(GTMaterials.HydrogenSulfide.getFluid(7500))
                .outputFluids(GTMaterials.SulfurDioxide.getFluid(7500))
                .outputFluids(GTMaterials.Helium3.getFluid(2500))
                .outputFluids(GTMaterials.Neon.getFluid(500))
                .chancedOutput(ChemicalHelper.get(dustSmall, GTMaterials.Ash), 9000, 0)
                .duration(2000).EUt(GTValues.VA[GTValues.EV]).save(provider);

        GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("liquid_ender_air")
                .inputFluids(GTMaterials.LiquidEnderAir.getFluid(200000))
                .outputFluids(GTMaterials.NitrogenDioxide.getFluid(122000))
                .outputFluids(GTMaterials.Deuterium.getFluid(50000))
                .outputFluids(GTMaterials.Helium.getFluid(15000))
                .outputFluids(GTMaterials.Tritium.getFluid(10000))
                .outputFluids(GTMaterials.Krypton.getFluid(1000))
                .outputFluids(GTMaterials.Xenon.getFluid(1000))
                .outputFluids(GTMaterials.Radon.getFluid(1000))
                .chancedOutput(ChemicalHelper.get(dustTiny, GTMaterials.EnderPearl), 9000, 0)
                .duration(2000).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    public static void genDistilleryRecipes(GTRecipeBuilder recipeBuilder, Consumer<FinishedRecipe> provider) {
        var fluidOutputs = recipeBuilder.output.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
        var fluidInputs = recipeBuilder.input.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
        var outputs = recipeBuilder.output.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
        for (int i = 0; i < fluidOutputs.size(); i++) {
            var builder = GTRecipeTypes.DISTILLERY_RECIPES.recipeBuilder(recipeBuilder.id.getPath() + "." + i)
                    .EUt(Math.max(1, recipeBuilder.EUt() / 4))
                    .circuitMeta(i + 1);

            int ratio = getRatioForDistillery(FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()),
                    FluidRecipeCapability.CAP.of(fluidOutputs.get(i).getContent()),
                    outputs.size() > 0 ? ItemRecipeCapability.CAP.of(outputs.get(0).getContent()).getItems()[0] : null);

            int recipeDuration = (int) (recipeBuilder.duration * OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR);

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
