package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerEnergyStack;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTMath;

import it.unimi.dsi.fastutil.longs.LongList;

import java.util.List;
import java.util.Map;

public class EURecipeCapability extends RecipeCapability<EnergyStack> {

    public final static EURecipeCapability CAP = new EURecipeCapability();

    protected EURecipeCapability() {
        super("eu", 0xFFFFFF00, false, 2, SerializerEnergyStack.INSTANCE);
    }

    @Override
    public EnergyStack copyInner(EnergyStack content) {
        return content;
    }

    @Override
    public EnergyStack copyWithModifier(EnergyStack content, ContentModifier modifier) {
        return content.withAmperage(modifier.apply(content.amperage()));
    }

    @Override
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int multiplier, boolean tick) {
        var outputs = tick ? recipe.getTickOutputContents(this) : recipe.getOutputContents(this);
        if (outputs.isEmpty()) return multiplier;

        var handlers = holder.getOutputHandlerMap().get(this);
        if (handlers == null || handlers.isEmpty()) return 0;

        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        long totalEU = 0L;
        for (var content : outputs) totalEU += of(content.content).getTotalEU();
        if (totalEU != 0 && multiplier > Long.MAX_VALUE / totalEU) {
            maxMultiplier = multiplier = GTMath.saturatedCast(Long.MAX_VALUE / totalEU);
        }

        while (minMultiplier != maxMultiplier) {
            List<Long> eu = LongList.of(totalEU * multiplier);
            for (var handler : handlers) {
                // noinspection unchecked
                eu = (List<Long>) handler.handleRecipe(IO.OUT, recipe, eu, true);
                if (eu == null) break;
            }
            int[] bin = ParallelLogic.adjustMultiplier(eu == null, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];
        }

        return multiplier;
    }

    @Override
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        var inputs = tick ? recipe.getTickInputContents(this) : recipe.getInputContents(this);
        if (inputs.isEmpty()) return limit;

        long nonConsumable = 0;
        long consumable = 0;
        for (Content content : inputs) {
            EnergyStack s = of(content.content);
            if (content.chance == 0) nonConsumable += s.getTotalEU();
            else consumable += s.getTotalEU();
        }

        if (nonConsumable == 0 && consumable == 0) return limit;

        long sum = 0;
        var handlers = holder.getInputHandlerMap().get(this);
        if (handlers == null || handlers.isEmpty()) return 0;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof EnergyStack es) sum += es.getTotalEU();
                else if (content instanceof Long l) sum += l;
            }
        }

        if (sum < nonConsumable) return 0;
        if (consumable == 0) return limit;
        sum -= nonConsumable;
        return Math.min(GTMath.saturatedCast(sum / consumable), limit);
    }

    /**
     * Creates a {@code List<Content>} with the specified EU
     * 
     * @param eu EU/t value to put in the Content
     * @return Singleton list of a new Content with the given EU value
     */
    public static List<Content> makeEUContent(EnergyStack eu) {
        return List.of(
                new Content(eu, 10000, 10000, 0));
    }

    /**
     * Puts an EU Singleton Content in the given content map
     * 
     * @param contents content map
     * @param eu       EU value to put inside content map
     */
    public static void putEUContent(Map<RecipeCapability<?>, List<Content>> contents, EnergyStack eu) {
        contents.put(EURecipeCapability.CAP, makeEUContent(eu));
    }

    public interface ICustomParallel {

        /**
         * Custom impl of the parallel limiter used by ParallelLogic to limit by outputs
         *
         * @param recipe     Recipe
         * @param multiplier Initial multiplier
         * @param tick       Tick or not
         * @return Limited multiplier
         */
        int limitEUParallel(GTRecipe recipe, int multiplier, boolean tick);
    }
}
