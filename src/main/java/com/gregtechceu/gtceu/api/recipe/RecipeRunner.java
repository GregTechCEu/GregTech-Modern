package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeRunner {

    private static final int GAUSSIAN_ROLL_THRESHOLD = 16;

    private final GTRecipe recipe;
    private final IO io;
    private final RecipeHandlerGroup group;
    private final boolean simulated;
    private Map<RecipeCapability<?>, List<Object>> recipeContents;
    private final Map<RecipeCapability<?>, List<Object>> searchRecipeContents;
    private final Predicate<RecipeCapability<?>> outputVoid;

    public RecipeRunner(GTRecipe recipe, IO io, boolean isTick,
                        RecipeHandlerGroup group, boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.group = group;

        this.recipeContents = new Reference2ObjectOpenHashMap<>();
        this.searchRecipeContents = simulated ? recipeContents : new Reference2ObjectOpenHashMap<>();
        this.simulated = simulated;
        this.outputVoid = group.getOutputVoid() == null ? cap -> false : group.getOutputVoid();
    }

    @NotNull
    public ActionResult handle(Map<RecipeCapability<?>, List<Content>> entries) {
        fillContentMatchList(entries);

        if (searchRecipeContents.isEmpty()) {
            return ActionResult.PASS_NO_CONTENTS;
        }

        return this.handleContents();
    }

    /**
     * Populates the content match list to know if conditions are satisfied.
     */
    private void fillContentMatchList(Map<RecipeCapability<?>, List<Content>> entries) {
        ChanceBoostFunction function = recipe.getType().getChanceFunction();
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        for (var entry : entries.entrySet()) {
            RecipeCapability<?> cap = entry.getKey();
            if (!cap.doMatchInRecipe()) continue;
            if (simulated && io == IO.OUT && outputVoid.test(cap)) continue;

            List<Content> chancedContents = new ArrayList<>();
            // skip if empty
            if (entry.getValue().isEmpty()) continue;
            // populate recipe content capability map
            var contentList = this.recipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            var searchContentList = this.searchRecipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            for (Content cont : entry.getValue()) {
                searchContentList.add(cont.content);

                // When simulating the recipe handling (used for recipe matching),
                // searchRecipeContents == recipeContents, so all contents, chanced and unchanced, must match
                if (simulated) continue;

                if (cont.chance >= cont.maxChance) {
                    contentList.add(cont.content);
                } else if (cont.chance > 0 || cont.tierChanceBoost > 0) {
                    chancedContents.add(cont);
                }
                // Do not add Non-Consumed ingredients; they'd just get dropped after the chance roll anyway
            }

            // add chanced contents to the recipe content map
            if (!chancedContents.isEmpty()) {
                chancedContents = rollIndependent(cap, chancedContents, function, recipeTier, chanceTier,
                        recipe.getTotalRuns());

                for (Content cont : chancedContents) {
                    contentList.add(cont.content);
                }
            }

            if (contentList.isEmpty()) recipeContents.remove(cap);
        }
    }

    private List<Content> rollIndependent(RecipeCapability<?> cap, List<Content> chancedContents,
                                          ChanceBoostFunction function, int recipeTier, int chanceTier, int times) {
        List<Content> rolled = new ArrayList<>();
        for (Content entry : chancedContents) {
            int maxChance = entry.maxChance;
            int chance = function.getBoostedChance(entry, recipeTier, chanceTier);
            int successes = rollSuccesses(times, chance, maxChance);
            if (successes > 0) {
                rolled.add(entry.copyChanced(cap, ContentModifier.multiplier(successes)));
            }
        }
        return rolled;
    }

    private static int rollSuccesses(int times, int chance, int maxChance) {
        if (times <= 0 || chance <= 0) return 0;
        if (chance >= maxChance) return times;

        if (times <= GAUSSIAN_ROLL_THRESHOLD) {
            int successes = 0;
            for (int i = 0; i < times; i++) {
                if (GTValues.RNG.nextInt(maxChance) < chance) successes++;
            }
            return successes;
        }

        double probability = (double) chance / maxChance;
        double mean = times * probability;
        double deviation = Math.sqrt(times * probability * (1.0 - probability));
        int successes = (int) Math.round(mean + GTValues.RNG.nextGaussian() * deviation);
        return Math.max(0, Math.min(times, successes));
    }

    private ActionResult handleContents() {
        if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
        if (!hasCapabilitiesForIO()) {
            return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.no_capabilities")
                            .append(Component.literal(": "))
                            .append(Component.translatable(io.tooltip)),
                    null, io);
        }

        var copiedRecipeContents = group.handleRecipe(io, recipe, searchRecipeContents, true);
        if (io == IO.OUT) {
            if (hasAnyNonVoidingContents(copiedRecipeContents)) {
                return getFailureResult(recipeContents);
            }
        } else if (io == IO.IN && !copiedRecipeContents.isEmpty()) {
            return getFailureResult(recipeContents);
        }

        if (simulated) return ActionResult.SUCCESS;

        recipeContents = group.handleRecipe(io, recipe, recipeContents, false);
        var result = getFailureResult(recipeContents);
        if (result.isSuccess()) {
            recipeContents.clear();
        }
        return result;
    }

    private boolean hasCapabilitiesForIO() {
        if (io == IO.IN) return !group.getInputHandlerMap().isEmpty();
        if (io == IO.OUT) return !group.getOutputHandlerMap().isEmpty();
        return false;
    }

    private ActionResult getFailureResult(Map<RecipeCapability<?>, List<Object>> contents) {
        for (var entry : contents.entrySet()) {
            // void excess real output contents if it can be voided
            if (!simulated && io == IO.OUT && this.outputVoid.test(entry.getKey())) {
                entry.getValue().clear();
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                return ActionResult.fail(null, entry.getKey(), io);
            }
        }

        return ActionResult.SUCCESS;
    }

    private boolean hasAnyNonVoidingContents(Map<RecipeCapability<?>, List<Object>> contents) {
        for (var entry : contents.entrySet()) {
            if (outputVoid.test(entry.getKey())) continue;
            if (!(entry.getValue() == null || entry.getValue().isEmpty())) {
                return true;
            }
        }
        return false;
    }
}
