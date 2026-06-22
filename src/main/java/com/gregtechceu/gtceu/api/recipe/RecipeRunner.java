package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupColor;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness.BUS_DISTINCT;
import static com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness.BYPASS_DISTINCT;
import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

public class RecipeRunner {

    private final GTRecipe recipe;
    private final IO io;
    private final boolean isTick;
    private final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;
    private final Map<IO, List<RecipeHandlerList>> capabilityProxies;
    private final boolean simulated;
    private Map<RecipeCapability<?>, List<Object>> recipeContents;
    private final Map<RecipeCapability<?>, List<Object>> searchRecipeContents;
    private final Predicate<RecipeCapability<?>> outputVoid;
    @Getter
    private int groupColor;

    /** Highest closeness score seen across handler-group attempts, and the leftover map that produced it. */
    private double bestScore = 0;
    private @Nullable Map<RecipeCapability<?>, List<Object>> bestLeftover;

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
        this.outputVoid = cap -> holder instanceof IVoidable voidable && voidable.canVoidRecipeOutputs(cap);
        this.groupColor = recipe.groupColor;
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

            ChanceLogic logic = recipe.getChanceLogicForCapability(cap, this.io, this.isTick);
            List<Content> chancedContents = new ArrayList<>();
            // skip if empty
            if (entry.getValue().isEmpty()) continue;
            // populate recipe content capability map
            var contentList = this.recipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            var searchContentList = this.searchRecipeContents.computeIfAbsent(cap, c -> new ArrayList<>());
            for (Content cont : entry.getValue()) {
                searchContentList.add(cont.content());

                // When simulating the recipe handling (used for recipe matching),
                // searchRecipeContents == recipeContents, so all contents, chanced and unchanced, must match
                if (simulated) continue;

                if (cont.chance() >= cont.maxChance()) {
                    contentList.add(cont.content());
                } else if (cont.chance() > 0 || cont.tierChanceBoost() > 0) {
                    chancedContents.add(cont);
                }
                // Do not add Non-Consumed ingredients; they'd just get dropped after the chance roll anyway
            }

            // add chanced contents to the recipe content map
            if (!chancedContents.isEmpty()) {
                var cache = this.chanceCaches.get(cap);
                chancedContents = logic.roll(cap, chancedContents, function, recipeTier, chanceTier, cache,
                        recipe.getTotalRuns());

                for (Content cont : chancedContents) {
                    contentList.add(cont.content());
                }
            }

            if (contentList.isEmpty()) recipeContents.remove(cap);
        }
    }

    private ActionResult handleContents() {
        if (recipeContents.isEmpty()) return ActionResult.SUCCESS;
        if (!capabilityProxies.containsKey(io)) {
            return ActionResult.fail(
                    Component.translatable("gtceu.recipe_logic.no_capabilities")
                            .append(Component.literal(": "))
                            .append(Component.translatable(io.getTooltip())),
                    null, io);
        }

        List<RecipeHandlerList> handlers = capabilityProxies.getOrDefault(io, Collections.emptyList());
        // Only sort for non-tick outputs
        if (!isTick && io.support(IO.OUT)) {
            handlers.sort(RecipeHandlerList.COMPARATOR.reversed());
        }

        Map<RecipeHandlerGroup, List<RecipeHandlerList>> handlerGroups = new HashMap<>();
        for (var handler : handlers) {
            addToRecipeHandlerMap(handler.getGroup(), handler, handlerGroups);
        }
        // Specifically check distinct handlers first
        for (RecipeHandlerList handler : handlerGroups.getOrDefault(BUS_DISTINCT, Collections.emptyList())) {
            // Handle the contents of this handler and also all the bypassed handlers
            var res = handler.handleRecipe(io, recipe, searchRecipeContents, true);
            if (!res.isEmpty()) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                        Collections.emptyList())) {
                    res = bypassHandler.handleRecipe(io, recipe, res, true);
                    if (res.isEmpty()) break;
                }
            }
            if (io == IO.OUT) {
                if (hasAnyNonVoidingContents(res)) {
                    recordLeftover(res);
                    continue;
                }
            } else if (io == IO.IN) {
                if (!res.isEmpty()) {
                    recordLeftover(res);
                    continue;
                }
            }
            if (!simulated) {
                // Actually consume the contents of this handler and also all the bypassed handlers
                recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
                if (!recipeContents.isEmpty()) {
                    for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                            Collections.emptyList())) {
                        recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                        if (recipeContents.isEmpty()) break;
                    }
                }
            }
            recipeContents.clear();
            return ActionResult.SUCCESS;
        }

        // Check the other groups. For every group, try consuming the ingredients,
        // see if it succeeds.
        for (Map.Entry<RecipeHandlerGroup, List<RecipeHandlerList>> handlerListEntry : handlerGroups.entrySet()) {
            if (handlerListEntry.getKey().equals(BUS_DISTINCT)) continue;

            if (handlerListEntry.getKey() instanceof RecipeHandlerGroupColor coloredGroup) {
                if (io == IO.IN && simulated && !isTick) {
                    groupColor = coloredGroup.color();
                } else if (coloredGroup.color() != -1 && coloredGroup.color() != groupColor) {
                    continue;
                }
            }
            // List to keep track of the remaining items for this RecipeHandlerGroup
            Map<RecipeCapability<?>, List<Object>> copiedRecipeContents = searchRecipeContents;

            for (RecipeHandlerList handler : handlerListEntry.getValue()) {
                copiedRecipeContents = handler.handleRecipe(io, recipe, copiedRecipeContents, true);
                if (copiedRecipeContents.isEmpty()) {
                    break;
                }
            }
            // If we're already in the bypass_distinct group, don't check it twice.
            if (!handlerListEntry.getKey().equals(BYPASS_DISTINCT)) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                        Collections.emptyList())) {
                    copiedRecipeContents = bypassHandler.handleRecipe(io, recipe, copiedRecipeContents, true);
                    if (copiedRecipeContents.isEmpty()) {
                        break;
                    }
                }
            }

            if (io == IO.OUT) {
                if (hasAnyNonVoidingContents(copiedRecipeContents)) {
                    recordLeftover(copiedRecipeContents);
                    continue;
                }
            } else if (io == IO.IN) {
                if (!copiedRecipeContents.isEmpty()) {
                    recordLeftover(copiedRecipeContents);
                    continue;
                }
            }
            if (simulated) return ActionResult.SUCCESS;
            // Start actually removing items.
            // Keep track of the remaining items for this RecipeHandlerGroup
            // First go through the handlers of the group
            for (RecipeHandlerList handler : handlerListEntry.getValue()) {
                recipeContents = handler.handleRecipe(io, recipe, recipeContents, false);
                if (recipeContents.isEmpty()) {
                    return ActionResult.SUCCESS;
                }
            }
            // Then go through the handlers that bypass the distinctness system and empty those
            // If we're already in the bypass_distinct group, don't check it twice.
            if (!handlerListEntry.getKey().equals(BYPASS_DISTINCT)) {
                for (RecipeHandlerList bypassHandler : handlerGroups.getOrDefault(BYPASS_DISTINCT,
                        Collections.emptyList())) {
                    recipeContents = bypassHandler.handleRecipe(io, recipe, recipeContents, false);
                    if (recipeContents.isEmpty()) {
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        for (var entry : recipeContents.entrySet()) {
            // void excess real output contents if it can be voided
            if (!simulated && io == IO.OUT && this.outputVoid.test(entry.getKey())) {
                entry.getValue().clear();
            }
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                RecipeCapability<?> failedCap = entry.getKey();
                if (simulated && bestLeftover != null) {
                    for (var leftoverEntry : bestLeftover.entrySet()) {
                        if (leftoverEntry.getValue() != null && !leftoverEntry.getValue().isEmpty() &&
                                leftoverEntry.getKey() != null) {
                            failedCap = leftoverEntry.getKey();
                        }
                    }
                }
                return ActionResult.fail(null, failedCap, io, bestScore);
            }
        }

        // if, post voiding, we don't have stuff, pass instead of fail
        boolean containsStuff = false;
        for (var entry : recipeContents.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                containsStuff = true;
                break;
            }
        }
        if (!containsStuff) {
            return ActionResult.PASS_NO_CONTENTS;
        }

        return ActionResult.fail(null, null, io, bestScore);
    }

    /**
     * Records a failed handler-group attempt's leftover map if it's the closest match seen so far. Higher score wins;
     * ties keep the earlier attempt. The retained map identifies which capabilities the closest attempt was short on.
     */
    private void recordLeftover(Map<RecipeCapability<?>, List<Object>> leftover) {
        double score = scoreLeftover(leftover);
        if (bestLeftover == null || score > bestScore) {
            bestScore = score;
            bestLeftover = leftover;
        }
    }

    private double scoreLeftover(Map<RecipeCapability<?>, List<Object>> leftover) {
        double sum = 0;
        int caps = 0;
        for (var entry : searchRecipeContents.entrySet()) {
            int total = entry.getValue().size();
            if (total <= 0) continue;
            caps++;
            var leftList = leftover.get(entry.getKey());
            int leftCount = leftList == null ? 0 : Math.min(leftList.size(), total);
            sum += (double) (total - leftCount) / total;
        }
        return caps == 0 ? 1.0 : sum / caps;
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
