package com.gregtechceu.gtceu.api.recipe;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Accessors(fluent = true) @Getter
@SuppressWarnings({"rawtypes", "unchecked"})
class RecipeRunner {
    private static class ContentSlots {
        public List content = new ArrayList<>();
        public Map<String, List> slots = new HashMap<>();
    }
    
    record RecipeHandlingResult(RecipeCapability<?> capability, Tuple<List, Map<String, List>> result) {
    }

// --------------------------------------------------------------------------------------------------------

    private final GTRecipe recipe;
    private final IO io;
    private final IRecipeCapabilityHolder holder;
    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies;
    private final boolean simulated;

    private final Set<IRecipeHandler<?>> used = new HashSet<>();

    private ContentSlots cont = new ContentSlots();
    private ContentSlots search = new ContentSlots();


    public RecipeRunner(GTRecipe recipe, IO io, IRecipeCapabilityHolder holder, boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.holder = holder;
        this.capabilityProxies = holder.getCapabilitiesProxy();
        this.simulated = simulated;

        if (simulated) {
            this.search = this.cont;
        }
    }

    public void setContent(List content) {
        this.cont.content = content;
    }

    @Nullable
    public RecipeHandlingResult handle(Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        this.fillContent(holder, entry);

        RecipeCapability<?> capability = this.resolveCapability(entry);
        if (capability == null)
            return null;

        var result = this.handleContents(capabilityProxies, capability);
        if (result == null)
            return null;

        return new RecipeHandlingResult(capability, result);
    }

    private void fillContent(IRecipeCapabilityHolder holder, Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        for (Content cont : entry.getValue()) {
            if (cont.slotName == null) {
                this.search.content.add(cont.content);
            } else {
                this.search.slots.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
            }

            // When simulating the recipe handling (used for recipe matching), chanced contents are ignored.
            if (simulated) continue;

            if (cont.chance >= 1 || GTValues.RNG.nextFloat() < (cont.chance + holder.getChanceTier() * cont.tierChanceBoost)) { // chance input
                if (cont.slotName == null) {
                    this.cont.content.add(cont.content);
                } else {
                    this.cont.slots.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            }
        }
    }

    private RecipeCapability<?> resolveCapability(Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        if (!capability.doMatchInRecipe()) {
            return null;
        }

        this.setContent(this.cont.content.stream().map(capability::copyContent).toList());
        if (this.cont.content.isEmpty() && this.cont.slots.isEmpty()) return null;
        if (this.cont.content.isEmpty()) this.setContent(null);

        return capability;
    }

    private Tuple<List, Map<String, List>> handleContents(Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies, RecipeCapability<?> capability) {
        handleContentsInternal(io, capabilityProxies, capability, this);
        if (cont().content == null && cont().slots.isEmpty()) return null;
        handleContentsInternal(IO.BOTH, capabilityProxies, capability, this);

        return new Tuple<>(cont().content, cont().slots);
    }


    private void handleContentsInternal(
        IO capIO, Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies,
        RecipeCapability<?> capability, RecipeRunner data
    ) {
        if (!capabilityProxies.contains(capIO, capability))
            return;

        //noinspection DataFlowIssue checked above.
        var handlers = new ArrayList<>(capabilityProxies.get(capIO, capability));
        handlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        // handle distinct first
        for (IRecipeHandler<?> handler : handlers) {
            if (!handler.isDistinct()) continue;
            var result = handler.handleRecipe(io, recipe, data.search().content, null, true);
            if (result == null) {
                // check distint slot handler
                if (handler.getSlotNames() != null && handler.getSlotNames().containsAll(data.search().slots.keySet())) {
                    boolean success = true;
                    for (var entry : data.search().slots.entrySet()) {
                        List<?> left = handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), true);
                        if (left != null) {
                            success = false;
                            break;
                        }
                    }
                    if (success) {
                        if (!simulated) {
                            for (var entry : data.cont().slots.entrySet()) {
                                handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), false);
                            }
                        }
                        data.cont().slots.clear();
                    }
                }
                if (data.cont().slots.isEmpty()) {
                    if (!simulated) {
                        handler.handleRecipe(io, recipe, data.cont().content, null, false);
                    }
                    data.setContent(null);
                }
            }
            if (data.cont().content == null && data.cont().slots.isEmpty()) {
                break;
            }
        }
        if (data.cont().content != null || !data.cont().slots.isEmpty()) {
            // handle undistinct later
            for (IRecipeHandler<?> proxy : handlers) {
                if (data.used().contains(proxy) || proxy.isDistinct()) continue;
                data.used().add(proxy);
                if (data.cont().content != null) {
                    data.setContent(proxy.handleRecipe(io, recipe, data.cont().content, null, simulated));
                }
                if (proxy.getSlotNames() != null) {
                    Iterator<String> iterator = data.cont().slots.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (proxy.getSlotNames().contains(key)) {
                            List<?> left = proxy.handleRecipe(io, recipe, data.cont().slots.get(key), key, simulated);
                            if (left == null) iterator.remove();
                        }
                    }
                }
                if (data.cont().content == null && data.cont().slots.isEmpty()) break;
            }
        }
    }
}
