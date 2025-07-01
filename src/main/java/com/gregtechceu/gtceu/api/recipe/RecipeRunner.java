package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RecipeRunner {

    record RecipeHandlingResult(ActionResult result, @Nullable RecipeCapability<?> capability) {

        public static RecipeHandlingResult SUCCESS = new RecipeHandlingResult(ActionResult.SUCCESS, null);

        public boolean isSuccess() {
            return result.isSuccess();
        }
    }

    private final GTRecipe recipe;
    private final IO io;
    private final boolean isTick;
    private final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;
    private final Map<IO, List<RecipeHandlerList>> capabilityProxies;
    private final boolean simulated;
    private Map<RecipeCapability<?>, List<Object>> recipeContents;
    private final Map<RecipeCapability<?>, List<Object>> searchRecipeContents;

    public RecipeRunner(GTRecipe recipe, IO io, boolean isTick,
                        IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                        boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.isTick = isTick;
        this.chanceCaches = chanceCaches;
        this.capabilityProxies = holder.getCapabilitiesProxy();
        this.recipeContents = new Reference2ObjectOpenHashMap<>();
        this.searchRecipeContents = simulated ? recipeContents : new Reference2ObjectOpenHashMap<>();
        this.simulated = simulated;
    }

    @NotNull
    public RecipeHandlingResult handle(Map<RecipeCapability<?>, List<Content>> entries) {
        fillContentMatchList(entries);

        if (searchRecipeContents.isEmpty()) {
            return new RecipeHandlingResult(ActionResult.PASS_NO_CONTENTS, null);
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

            ChanceLogic logic = recipe.getChanceLogicForCapability(cap, this.io, this.isTick);
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
                } else {
                    chancedContents.add(cont);
                }
            }

            // add chanced contents to the recipe content map
            if (!chancedContents.isEmpty()) {
                var cache = this.chanceCaches.get(cap);
                chancedContents = logic.roll(chancedContents, function, recipeTier, chanceTier, cache,
                        recipe.parallels);

                for (Content cont : chancedContents) {
                    contentList.add(cont.content);
                }
            }

            if (contentList.isEmpty()) recipeContents.remove(cap);
        }
    }

    private RecipeHandlingResult handleContents() {
        return handleContentsInternal(io);
    }

    private RecipeHandlingResult handleContentsInternal(IO capIO) {
        if (recipeContents.isEmpty()) return RecipeHandlingResult.SUCCESS;
        if (!capabilityProxies.containsKey(capIO)) {
            return new RecipeHandlingResult(ActionResult.FAIL_NO_CAPABILITIES, null);
        }

        var handlers = capabilityProxies.get(capIO);
        // Only sort for non-tick outputs
        if (!isTick && capIO == IO.OUT) {
            handlers.sort(RecipeHandlerList.COMPARATOR.reversed());
        }
        List<RecipeHandlerList> distinct = new ArrayList<>();
        List<RecipeHandlerList> indistinct = new ArrayList<>();
        for (var handler : handlers) {
            if (handler.isDistinct()) distinct.add(handler);
            else indistinct.add(handler);
        }

        // handle distinct first
        for (var handler : distinct) {
            var res = handler.handleRecipe(io, recipe, searchRecipeContents, true);
            if (res.isEmpty()) {
                if (!simulated) {
                    handler.handleRecipe(io, recipe, recipeContents, false);
                }
                recipeContents.clear();
                return RecipeHandlingResult.SUCCESS;
            }
        }

        for (var handler : indistinct) {
            recipeContents = handler.handleRecipe(io, recipe, recipeContents, simulated);
            if (recipeContents.isEmpty()) {
                return RecipeHandlingResult.SUCCESS;
            }
        }

        for (var handler : distinct) {
            var res = handler.handleRecipe(io, recipe, recipeContents, simulated);
            if (res.isEmpty()) {
                recipeContents.clear();
                return RecipeHandlingResult.SUCCESS;
            }
        }

        for (var entry : recipeContents.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                return new RecipeHandlingResult(ActionResult.FAIL_NO_REASON, entry.getKey());
            }
        }

        return new RecipeHandlingResult(ActionResult.FAIL_NO_REASON, null);
    }
}
