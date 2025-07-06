package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerEnergyStack;
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
        return content.withVoltage(modifier.apply(content.voltage()));
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        if (holder instanceof ICustomParallel p) return p.limitEUParallel(recipe, multiplier, tick);
        if (tick) {
            long recipeEUt = recipe.getOutputEUt().getTotalEU();
            if (recipeEUt == 0) return multiplier;

            long maxVoltage = Long.MAX_VALUE;
            if (holder instanceof IOverclockMachine overclockMachine) {
                maxVoltage = overclockMachine.getOverclockVoltage();
            } else if (holder instanceof ITieredMachine tieredMachine) {
                maxVoltage = tieredMachine.getMaxVoltage();
            }

            return Math.min(multiplier, Math.abs(GTMath.saturatedCast(maxVoltage / recipeEUt)));
        } else {
            var outputs = recipe.getOutputContents(this);
            if (outputs.isEmpty()) return multiplier;

            if (!holder.hasCapabilityProxies()) return 0;
            var handlers = holder.getCapabilitiesFlat(IO.OUT, this);
            if (handlers.isEmpty()) return 0;

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
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (tick) {
            long maxVoltage = Long.MAX_VALUE;
            if (holder instanceof IOverclockMachine overclockMachine) {
                maxVoltage = overclockMachine.getOverclockVoltage();
            } else if (holder instanceof ITieredMachine tieredMachine) {
                maxVoltage = tieredMachine.getMaxVoltage();
            }

            long recipeEUt = recipe.getInputEUt().getTotalEU();
            if (recipeEUt == 0) return limit;
            return Math.min(limit, Math.abs(GTMath.saturatedCast(maxVoltage / recipeEUt)));
        } else {
            if (!holder.hasCapabilityProxies()) return 0;
            var inputs = recipe.getInputContents(this);
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
            for (var handler : holder.getCapabilitiesFlat(IO.IN, this)) {
                for (var content : handler.getContents()) {
                    if (content instanceof EnergyStack es) sum += es.getTotalEU();
                    else if (content instanceof Long l) sum += l;
                }
            }

            if (sum < nonConsumable) return 0;
            sum -= nonConsumable;
            return Math.min(GTMath.saturatedCast(sum / consumable), limit);
        }
    }

    /**
     * Creates a {@code List<Content>} with the specified EU
     * 
     * @param eu EU/t value to put in the Content
     * @return Singleton list of a new Content with the given EU value
     */
    public static List<Content> makeEUContent(EnergyStack eu) {
        return List.of(
                new Content(eu, ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0));
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
