package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

/**
 * Used to handle recipes, only valid for a single RecipeCapability's entries
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
class RecipeRunner {

    static class ContentSlots {

        public @UnknownNullability List content = new ArrayList<>();
        public @NotNull Map<String, List> slots = new HashMap<>();
    }

    record RecipeHandlingResult(RecipeCapability<?> capability, ContentSlots result) {}

    // --------------------------------------------------------------------------------------------------------

    private final GTRecipe recipe;
    private final IO io;
    private final boolean isTick;
    private final IRecipeCapabilityHolder holder;
    private final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches;
    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies;
    private final boolean simulated;

    // These are only used to store mutable state during each invocation of handle()
    private RecipeCapability<?> capability;
    private Set<IRecipeHandler<?>> used;
    private ContentSlots content;
    private ContentSlots search;

    public RecipeRunner(GTRecipe recipe, IO io, boolean isTick,
                        IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                        boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.isTick = isTick;
        this.holder = holder;
        this.chanceCaches = chanceCaches;
        this.capabilityProxies = holder.getCapabilitiesProxy();
        this.simulated = simulated;
    }

    @Nullable
    public RecipeHandlingResult handle(Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        initState();

        this.fillContent(holder, entry);
        this.capability = this.resolveCapability(entry);

        if (capability == null)
            return null;

        var result = this.handleContents();
        if (result == null)
            return null;

        return new RecipeHandlingResult(capability, result);
    }

    private void initState() {
        used = new HashSet<>();
        content = new ContentSlots();
        search = simulated ? content : new ContentSlots();
    }

    private void fillContent(IRecipeCapabilityHolder holder, Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        RecipeCapability<?> cap = entry.getKey();
        ChanceBoostFunction function = recipe.getType().getChanceFunction();
        ChanceLogic logic = recipe.getChanceLogicForCapability(cap, this.io, this.isTick);
        List<Content> chancedContents = new ArrayList<>();
        for (Content cont : entry.getValue()) {
            // For simulated handling, search/content are the same instance, so there's no need to switch between them
            if (cont.slotName == null) {
                this.search.content.add(cont.content);
            } else {
                this.search.slots.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
            }

            // When simulating the recipe handling (used for recipe matching), chanced contents are ignored.
            if (simulated) continue;

            if (cont.chance >= cont.maxChance) {
                if (cont.slotName == null) {
                    this.content.content.add(cont.content);
                } else {
                    this.content.slots.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            } else {
                chancedContents.add(cont);
            }
        }

        // Only roll if there's anything to roll for
        if (!chancedContents.isEmpty()) {
            int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
            int chanceTier = recipeTier + recipe.ocLevel;
            var cache = this.chanceCaches.get(cap);
            chancedContents = logic.roll(chancedContents, function, recipeTier, chanceTier, cache, recipe.parallels);

            for (Content cont : chancedContents) {
                if (cont.slotName == null) {
                    this.content.content.add(cont.content);
                } else {
                    this.content.slots.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            }
        }
    }

    private RecipeCapability<?> resolveCapability(Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        if (!capability.doMatchInRecipe()) {
            return null;
        }

        content.content = this.content.content.stream().map(capability::copyContent).toList();
        if (this.content.content.isEmpty() && this.content.slots.isEmpty()) return null;
        if (this.content.content.isEmpty()) content.content = null;

        return capability;
    }

    @Nullable
    private ContentSlots handleContents() {
        handleContentsInternal(io);
        if (content.content == null && content.slots.isEmpty()) return null;
        handleContentsInternal(IO.BOTH);

        return content;
    }

    private void handleContentsInternal(IO capIO) {
        if (!capabilityProxies.contains(capIO, capability))
            return;

        // noinspection DataFlowIssue checked above.
        var handlers = new ArrayList<>(capabilityProxies.get(capIO, capability));
        handlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        // handle distinct first
        for (IRecipeHandler<?> handler : handlers) {
            if (!handler.isDistinct()) continue;
            var result = handler.handleRecipe(io, recipe, search.content, null, true);
            if (result == null) {
                // check distint slot handler
                if (handler.getSlotNames() != null && handler.getSlotNames().containsAll(search.slots.keySet())) {
                    boolean success = true;
                    for (var entry : search.slots.entrySet()) {
                        List<?> left = handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), true);
                        if (left != null) {
                            success = false;
                            break;
                        }
                    }
                    if (success) {
                        if (!simulated) {
                            for (var entry : content.slots.entrySet()) {
                                handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), false);
                            }
                        }
                        content.slots.clear();
                    }
                }
                if (content.slots.isEmpty()) {
                    if (!simulated) {
                        handler.handleRecipe(io, recipe, content.content, null, false);
                    }
                    content.content = null;
                }
            }
            if (content.content == null && content.slots.isEmpty()) {
                break;
            }
        }
        if (content.content != null || !content.slots.isEmpty()) {
            // handle undistinct later
            for (IRecipeHandler<?> proxy : handlers) {
                if (used.contains(proxy) || proxy.isDistinct()) continue;
                used.add(proxy);
                if (content.content != null) {
                    content.content = proxy.handleRecipe(io, recipe, content.content, null, simulated);
                }
                if (proxy.getSlotNames() != null) {
                    Iterator<String> iterator = content.slots.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (proxy.getSlotNames().contains(key)) {
                            List<?> left = proxy.handleRecipe(io, recipe, content.slots.get(key), key, simulated);
                            if (left == null) iterator.remove();
                        }
                    }
                }
                if (content.content == null && content.slots.isEmpty()) break;
            }
        }
    }
}
