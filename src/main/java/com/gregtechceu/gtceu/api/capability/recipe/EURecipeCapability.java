package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerEnergyStack;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.GTMath;

import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Map;

public class EURecipeCapability extends RecipeCapability<EnergyStack> {

    public final static EURecipeCapability CAP = new EURecipeCapability();

    protected EURecipeCapability() {
        super("eu", 0xFFFFFF00, false, 2, EnergyStack.CODEC);
    }

    @Override
    public EnergyStack fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return EnergyStack.fromNetwork(friendlyByteBuf);
    }

    @Override
    public void toNetwork(EnergyStack ingredient, FriendlyByteBuf friendlyByteBuf) {
        ingredient.toNetwork(friendlyByteBuf);
    }

    @Override
    public EnergyStack copyInner(EnergyStack content, int multiplier) {
        return content.withAmperage(content.amperage() * multiplier);
    }

    @Override
    public int limitMaxParallelByOutput(RecipeHandlerGroup holder, GTRecipe recipe, int multiplier, boolean tick) {
        if(tick) {
            long recipeEUt = recipe.getOutputEUt().getTotalEU();
            if (recipeEUt == 0) return multiplier;
            long handlerEUt = holder.getOutputHandlerMap().getOrDefault(EURecipeCapability.CAP, List.of())
                    .stream()
                    .filter(IEnergyContainer.class::isInstance)
                    .map(IEnergyContainer.class::cast)
                    .mapToLong(c -> c.getOutputVoltage() * c.getOutputAmperage())
                    .sum();

            return Math.min(multiplier, Math.abs(GTMath.saturatedCast(handlerEUt / recipeEUt)));
        }
        else {
            var outputs = recipe.getOutputContents(this);
            if (outputs.isEmpty()) return multiplier;

            var handlers = holder.getOutputHandlerMap().get(this);
            if (handlers == null || handlers.isEmpty()) return 0;

            int minMultiplier = 0;
            int maxMultiplier = multiplier;

            long totalEU = 0L;
            for (var content : outputs) totalEU += content.getTotalEU();
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
    public int getMaxParallelByInput(RecipeHandlerGroup holder, GTRecipe recipe, int limit, boolean tick) {
        if(tick) {
            long recipeEUt = recipe.getInputEUt().getTotalEU();
            if (recipeEUt == 0) return limit;
            long handlerEUt = holder.getInputHandlerMap().getOrDefault(EURecipeCapability.CAP, List.of())
                    .stream()
                    .filter(IEnergyContainer.class::isInstance)
                    .map(IEnergyContainer.class::cast)
                    .mapToLong(c -> c.getInputVoltage() * c.getInputAmperage())
                    .sum();

            return Math.min(limit, Math.abs(GTMath.saturatedCast(handlerEUt / recipeEUt)));
        }
        else {
            var inputs = recipe.getInputContents(this);
            if (inputs.isEmpty()) return limit;

            long consumable = 0;
            for (var content : inputs) {
                consumable += content.getTotalEU();
            }

            if (consumable == 0) return limit;

            long sum = 0;
            var handlers = holder.getInputHandlerMap().get(this);
            if (handlers == null || handlers.isEmpty()) return 0;
            for (var handler : handlers) {
                for (var content : handler.getContents()) {
                    if (content instanceof EnergyStack es) sum += es.getTotalEU();
                    else if (content instanceof Long l) sum += l;
                }
            }

            return Math.min(GTMath.saturatedCast(sum / consumable), limit);
        }
    }

    /**
     * Puts an EU Singleton Content in the given content map
     * 
     * @param contents content map
     * @param eu       EU value to put inside content map
     */
    public static void putEUContent(ContentListMap contents, EnergyStack eu) {
        contents.put(EURecipeCapability.CAP, List.of(eu));
    }

}
