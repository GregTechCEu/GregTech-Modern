package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.longs.LongIntPair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote RecipeHelper
 */
public class RecipeHelper {

    public static long getInputEUt(GTRecipe recipe) {
        return recipe.getTickInputContents(EURecipeCapability.CAP).stream()
                .map(Content::getContent)
                .mapToLong(EURecipeCapability.CAP::of)
                .sum();
    }

    public static long getOutputEUt(GTRecipe recipe) {
        return recipe.getTickOutputContents(EURecipeCapability.CAP).stream()
                .map(Content::getContent)
                .mapToLong(EURecipeCapability.CAP::of)
                .sum();
    }

    public static void setInputEUt(GTRecipe recipe, long eut) {
        recipe.getTickInputContents(EURecipeCapability.CAP).forEach(c -> c.content = eut);
    }

    public static void setOutputEUt(GTRecipe recipe, long eut) {
        recipe.getTickOutputContents(EURecipeCapability.CAP).forEach(c -> c.content = eut);
    }

    public static int getRecipeEUtTier(GTRecipe recipe) {
        long EUt = getInputEUt(recipe);
        if (EUt == 0) {
            EUt = getOutputEUt(recipe);
        }
        return GTUtil.getTierByVoltage(EUt);
    }

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     * @return a new recipe
     */
    public static GTRecipe applyOverclock(OverclockingLogic logic, @NotNull GTRecipe recipe, long maxOverclockVoltage) {
        long EUt = getInputEUt(recipe);
        if (EUt > 0) {
            var overclockResult = performOverclocking(logic, recipe, EUt, maxOverclockVoltage);
            if (overclockResult.leftLong() != EUt || recipe.duration != overclockResult.rightInt()) {
                recipe = recipe.copy();
                recipe.duration = overclockResult.rightInt();
                for (Content content : recipe.getTickInputContents(EURecipeCapability.CAP)) {
                    content.content = overclockResult.leftLong();
                }
            }
        }
        EUt = getOutputEUt(recipe);
        if (EUt > 0) {
            var overclockResult = performOverclocking(logic, recipe, EUt, maxOverclockVoltage);
            if (overclockResult.leftLong() != EUt || recipe.duration != overclockResult.rightInt()) {
                recipe = recipe.copy();
                recipe.duration = overclockResult.rightInt();
                for (Content content : recipe.getTickOutputContents(EURecipeCapability.CAP)) {
                    content.content = overclockResult.leftLong();
                }
            }
        }
        return recipe;
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe.
     * Then performs overclocking on the Recipe.
     *
     * @param recipe the recipe to overclock
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    public static LongIntPair performOverclocking(OverclockingLogic logic, @NotNull GTRecipe recipe, long EUt,
                                                  long maxOverclockVoltage) {
        int recipeTier = GTUtil.getTierByVoltage(EUt);
        int maximumTier = maxOverclockVoltage < Integer.MAX_VALUE ? logic.getOverclockForTier(maxOverclockVoltage) :
                GTUtil.getFakeVoltageTier(maxOverclockVoltage);
        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (numberOfOCs <= 0) return LongIntPair.of(EUt, recipe.duration);
        if (recipeTier == GTValues.ULV) numberOfOCs--; // no ULV overclocking

        // Always overclock even if numberOfOCs is <=0 as without it, some logic for coil bonuses ETC won't apply.
        return logic.getLogic().runOverclockingLogic(recipe, EUt, maxOverclockVoltage, recipe.duration, numberOfOCs);
    }

    /**
     * get all output items from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output items
     */
    public static List<ItemStack> getOutputItem(GTRecipe recipe) {
        return new ArrayList<>(recipe.getOutputContents(ItemRecipeCapability.CAP).stream()
                .map(content -> ItemRecipeCapability.CAP.of(content.getContent()))
                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                .collect(Collectors.toMap(ItemStack::getItem, Function.identity(),
                        (s1, s2) -> s1.copyWithCount(s1.getCount() + s2.getCount())))
                .values());
    }

    /**
     * get all output fluids from GTRecipes
     *
     * @param recipe GTRecipe
     * @return all output fluids
     */
    public static List<FluidStack> getOutputFluid(GTRecipe recipe) {
        return new ArrayList<>(recipe.getOutputContents(FluidRecipeCapability.CAP).stream()
                .map(content -> FluidRecipeCapability.CAP.of(content.getContent()))
                .flatMap(ingredient -> Arrays.stream(ingredient.getStacks()))
                .collect(Collectors.toMap(FluidStack::getFluid, Function.identity(),
                        (s1, s2) -> s1.copy(s1.getAmount() + s2.getAmount())))
                .values());
    }
}
