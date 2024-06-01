package com.gregtechceu.gtceu.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedSingleFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedTagFluidIngredient;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.core.ISizedFluidIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.material.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.CENTRIFUGE_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.ELECTROLYZER_RECIPES;

public class DecompositionRecipeHandler {

    public static void init(RecipeOutput provider) {
        for (var material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            var prefix = material.hasProperty(PropertyKey.DUST) ? dust : null;
            processDecomposition(prefix, material, provider);
        }
    }

    private static void processDecomposition(TagPrefix decomposePrefix, Material material, RecipeOutput provider) {
        if (material.getMaterialComponents().isEmpty() ||
                (!material.hasFlag(DECOMPOSITION_BY_ELECTROLYZING) &&
                        !material.hasFlag(DECOMPOSITION_BY_CENTRIFUGING)) ||
                // disable decomposition if explicitly disabled for this material or for one of it's components
                material.hasFlag(DISABLE_DECOMPOSITION) ||
                material.getMaterialComponents().size() > 6)
            return;

        List<ItemStack> outputs = new ArrayList<>();
        List<FluidIngredient> fluidOutputs = new ArrayList<>();
        int totalInputAmount = 0;

        // compute outputs
        for (MaterialStack component : material.getMaterialComponents()) {
            totalInputAmount += (int) component.amount();
            if (component.material().hasProperty(PropertyKey.DUST)) {
                outputs.add(ChemicalHelper.get(dust, component.material(), (int) component.amount()));
            } else if (component.material().hasProperty(PropertyKey.FLUID)) {
                fluidOutputs.add(component.material().asSingleFluidIngredient((int) (1000 * component.amount())));
            }
        }

        // only reduce items
        if (decomposePrefix != null) {
            // calculate lowest common denominator
            List<Integer> materialAmounts = new ArrayList<>();
            materialAmounts.add(totalInputAmount);
            outputs.forEach(itemStack -> materialAmounts.add(itemStack.getCount()));
            fluidOutputs.forEach(fluidStack -> materialAmounts.add((int) (((ISizedFluidIngredient)fluidStack).getAmount() / 1000)));

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

                List<FluidIngredient> reducedFluidOutputs = new ArrayList<>();

                for (FluidIngredient fluidStack : fluidOutputs) {
                    FluidIngredient reducedFluidStack = fluidStack;
                    if (reducedFluidStack instanceof SizedSingleFluidIngredient sized) {
                        reducedFluidStack = sized.copy();
                    } else if (reducedFluidStack instanceof SizedTagFluidIngredient sized) {
                        reducedFluidStack = sized.copy();
                    }
                    ((ISizedFluidIngredient)reducedFluidStack)
                            .setAmount(((ISizedFluidIngredient)reducedFluidStack).getAmount() / highestDivisor);
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
            builder = CENTRIFUGE_RECIPES.recipeBuilder("decomposition_centrifuging", material.getName())
                    .duration((int) Math.ceil(material.getMass() * totalInputAmount * 1.5))
                    .EUt(VA[LV]);
        }
        builder.outputItems(outputs.toArray(ItemStack[]::new));
        builder.outputFluids(fluidOutputs.toArray(FluidIngredient[]::new));

        // finish builder
        if (decomposePrefix != null) {
            builder.inputItems(decomposePrefix, material, totalInputAmount);
        } else {
            builder.inputFluids(material.asFluidIngredient(1000));
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
