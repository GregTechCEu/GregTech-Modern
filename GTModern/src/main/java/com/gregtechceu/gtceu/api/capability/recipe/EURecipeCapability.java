package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;

import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class EURecipeCapability extends RecipeCapability<Long> {

    public final static EURecipeCapability CAP = new EURecipeCapability();

    protected EURecipeCapability() {
        super("eu", 0xFFFFFF00, false, 2, SerializerLong.INSTANCE);
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public Long copyWithModifier(Long content, ContentModifier modifier) {
        return modifier.apply(content).longValue();
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return List.of(ingredients.stream().map(Long.class::cast).reduce(0L, Long::sum));
    }

    @Override
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        long maxVoltage = Long.MAX_VALUE;
        if (holder instanceof IOverclockMachine overclockMachine) {
            maxVoltage = overclockMachine.getOverclockVoltage();
        } else if (holder instanceof ITieredMachine tieredMachine) {
            maxVoltage = tieredMachine.getMaxVoltage();
        }

        long recipeEUt = RecipeHelper.getOutputEUt(recipe);
        if (recipeEUt == 0) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(Ints.saturatedCast(maxVoltage / recipeEUt));
    }

    @Override
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        long maxVoltage = Long.MAX_VALUE;
        if (holder instanceof IOverclockMachine overclockMachine) {
            maxVoltage = overclockMachine.getOverclockVoltage();
        } else if (holder instanceof ITieredMachine tieredMachine) {
            maxVoltage = tieredMachine.getMaxVoltage();
        }

        long recipeEUt = RecipeHelper.getInputEUt(recipe);
        if (recipeEUt == 0) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(Ints.saturatedCast(maxVoltage / recipeEUt));
    }
}
