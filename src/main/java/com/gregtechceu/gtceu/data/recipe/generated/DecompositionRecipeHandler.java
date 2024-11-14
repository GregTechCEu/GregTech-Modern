package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CENTRIFUGE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTROLYZER_RECIPES;

public class DecompositionRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider) {
        for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            var prefix = material.hasProperty(PropertyKey.DUST) ? dust : null;
            processDecomposition(prefix, material, provider);
        }
    }

    private static void processDecomposition(TagPrefix decomposePrefix, Material material,
                                             Consumer<FinishedRecipe> provider) {
        if (material.getMaterialComponents().isEmpty() ||
                (!material.hasFlag(DECOMPOSITION_BY_ELECTROLYZING) &&
                        !material.hasFlag(DECOMPOSITION_BY_CENTRIFUGING)) ||
                // disable decomposition if explicitly disabled for this material or for one of it's components
                material.hasFlag(DISABLE_DECOMPOSITION) ||
                material.getMaterialComponents().size() > 6)
            return;

        List<ItemStack> outputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        int totalInputAmount = 0;

        // compute outputs
        for (MaterialStack component : material.getMaterialComponents()) {
            totalInputAmount += component.amount();
            if (component.material().hasProperty(PropertyKey.DUST)) {
                outputs.add(ChemicalHelper.get(dust, component.material(), (int) component.amount()));
            } else if (component.material().hasProperty(PropertyKey.FLUID)) {
                fluidOutputs.add(component.material().getFluid((int) (1000 * component.amount())));
            }
        }

        // only reduce items
        if (decomposePrefix != null) {
            // calculate lowest common denominator
            List<Integer> materialAmounts = new ArrayList<>();
            materialAmounts.add(totalInputAmount);
            outputs.forEach(itemStack -> materialAmounts.add(itemStack.getCount()));
            fluidOutputs.forEach(fluidStack -> materialAmounts.add((int) (fluidStack.getAmount() / 1000)));

            int highestDivisor = 1;

            int smallestMaterialAmount = getSmallestMaterialAmount(materialAmounts);
            for (int i = 2; i <= smallestMaterialAmount; i++) {
                if (isEveryMaterialReducible(i, materialAmounts))
                    highestDivisor = i;
            }

            // divide components
            if (highestDivisor != 1) {
                List<ItemStack> reducedOutputs = new ArrayList<>();

                for (ItemStack itemStack : outputs) {
                    ItemStack reducedStack = itemStack.copy();
                    reducedStack.setCount(reducedStack.getCount() / highestDivisor);
                    reducedOutputs.add(reducedStack);
                }

                List<FluidStack> reducedFluidOutputs = new ArrayList<>();

                for (FluidStack fluidStack : fluidOutputs) {
                    FluidStack reducedFluidStack = fluidStack.copy();
                    reducedFluidStack.setAmount(reducedFluidStack.getAmount() / highestDivisor);
                    reducedFluidOutputs.add(reducedFluidStack);
                }

                outputs = reducedOutputs;
                fluidOutputs = reducedFluidOutputs;
                totalInputAmount /= highestDivisor;
            }
        }

        // generate builder
        GTRecipeBuilder builder;
        if (material.hasFlag(DECOMPOSITION_BY_ELECTROLYZING)) {
            builder = ELECTROLYZER_RECIPES.recipeBuilder("decomposition_electrolyzing", material.getName())
                    .duration(((int) material.getProtons() * totalInputAmount * 2))
                    .EUt(material.getMaterialComponents().size() <= 2 ? VA[LV] : 2L * VA[LV]);
        } else {
            builder = CENTRIFUGE_RECIPES.recipeBuilder("decomposition_centrifuging_", material.getName())
                    .duration((int) Math.ceil(material.getMass() * totalInputAmount * 1.5))
                    .EUt(VA[LV]);
        }
        builder.outputItems(outputs.toArray(ItemStack[]::new));
        builder.outputFluids(fluidOutputs.toArray(FluidStack[]::new));

        // finish builder
        if (decomposePrefix != null) {
            builder.inputItems(decomposePrefix, material, totalInputAmount);
        } else {
            builder.inputFluids(material.getFluid(1000));
        }

        // register recipe
        builder.save(provider);
    }

    private static boolean isEveryMaterialReducible(int divisor, List<Integer> materialAmounts) {
        for (int amount : materialAmounts) {
            if (amount % divisor != 0)
                return false;
        }
        return true;
    }

    private static int getSmallestMaterialAmount(List<Integer> materialAmounts) {
        return materialAmounts.stream().min(Integer::compare).orElse(0);
    }
}
