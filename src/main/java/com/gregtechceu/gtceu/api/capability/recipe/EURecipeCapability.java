package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;
import com.gregtechceu.gtceu.utils.GTMath;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        return modifier.apply(content);
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return List.of(ingredients.stream().map(Long.class::cast).reduce(0L, Long::sum));
    }

    @Override
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        if (holder instanceof ICustomParallel p) return p.limitParallel(recipe, multiplier);

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
        return Math.abs(GTMath.saturatedCast(maxVoltage / recipeEUt));
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
        return Math.abs(GTMath.saturatedCast(maxVoltage / recipeEUt));
    }

    /**
     * Creates a {@code List<Content>} with the specified EU
     * 
     * @param eu EU/t value to put in the Content
     * @return Singleton list of a new Content with the given EU value
     */
    public static List<Content> makeEUContent(Long eu) {
        return List.of(
                new Content(eu, ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null));
    }

    /**
     * Puts an EU Singleton Content in the given content map
     * 
     * @param contents content map
     * @param eu       EU value to put inside content map
     */
    public static void putEUContent(Map<RecipeCapability<?>, List<Content>> contents, long eu) {
        contents.put(EURecipeCapability.CAP, makeEUContent(eu));
    }

    public interface ICustomParallel {

        /**
         * Custom impl of the parallel limiter used by ParallelLogic to limit by outputs
         * 
         * @param recipe     Recipe
         * @param multiplier Initial multiplier
         * @return Limited multiplier
         */
        int limitParallel(GTRecipe recipe, int multiplier);
    }
}
