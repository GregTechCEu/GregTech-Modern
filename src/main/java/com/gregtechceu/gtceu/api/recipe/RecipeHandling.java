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
class RecipeHandling {

    record RecipeHandlingResult(RecipeCapability<?> capability, Tuple<List, Map<String, List>> result) {
    }

// --------------------------------------------------------------------------------------------------------

    private final GTRecipe recipe;
    private final IO io;
    private final IRecipeCapabilityHolder holder;
    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies;
    private final boolean simulated;

    private final Set<IRecipeHandler<?>> used = new HashSet<>();

    private List content = new ArrayList<>();
    private Map<String, List> contentSlot = new HashMap<>();

    private List contentSearch = new ArrayList<>();
    private Map<String, List> contentSlotSearch = new HashMap<>();


    public RecipeHandling(GTRecipe recipe, IO io, IRecipeCapabilityHolder holder, boolean simulated) {
        this.recipe = recipe;
        this.io = io;
        this.holder = holder;
        this.capabilityProxies = holder.getCapabilitiesProxy();
        this.simulated = simulated;

        if (simulated) {
            this.replaceContent(new Tuple<>(this.contentSearch, this.contentSlotSearch));
        }
    }

    public void setContent(List content) {
        this.content = content;

        if (simulated)
            this.contentSearch = content;
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


    private RecipeHandling replaceContent(Tuple<List, Map<String, List>> result) {
        this.content = result.getA();
        this.contentSlot = result.getB();
        return this;
    }

    private void fillContent(IRecipeCapabilityHolder holder, Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        for (Content cont : entry.getValue()) {
            if (cont.slotName == null) {
                this.contentSearch.add(cont.content);
            } else {
                this.contentSlotSearch.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
            }

            // When simulating the recipe handling (used for recipe matching), chanced contents are ignored.
            if (simulated) continue;

            if (cont.chance >= 1 || GTValues.RNG.nextFloat() < (cont.chance + holder.getChanceTier() * cont.tierChanceBoost)) { // chance input
                if (cont.slotName == null) {
                    this.content.add(cont.content);
                } else {
                    this.contentSlot.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            }
        }
    }

    private RecipeCapability<?> resolveCapability(Map.Entry<RecipeCapability<?>, List<Content>> entry) {
        RecipeCapability<?> capability = entry.getKey();
        if (!capability.doMatchInRecipe()) {
            return null;
        }

        this.setContent(this.content.stream().map(capability::copyContent).toList());
        if (this.content.isEmpty() && this.contentSlot.isEmpty()) return null;
        if (this.content.isEmpty()) this.setContent(null);

        return capability;
    }

    private Tuple<List, Map<String, List>> handleContents(Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies, RecipeCapability<?> capability) {
        var result = handleContentsInternal(io, capabilityProxies, capability, this);

        //noinspection ConstantValue
        if (result.getA() == null && result.getB().isEmpty()) return null;

        return handleContentsInternal(IO.BOTH, capabilityProxies, capability, this.replaceContent(result));
    }


    private Tuple<List, Map<String, List>> handleContentsInternal(
        IO capIO, Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies,
        RecipeCapability<?> capability, RecipeHandling data
    ) {
        if (!capabilityProxies.contains(capIO, capability))
            return new Tuple<>(data.content(), data.contentSlot());

        //noinspection DataFlowIssue checked above.
        var handlers = new ArrayList<>(capabilityProxies.get(capIO, capability));
        handlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        // handle distinct first
        for (IRecipeHandler<?> handler : handlers) {
            if (!handler.isDistinct()) continue;
            var result = handler.handleRecipe(io, recipe, data.contentSearch(), null, true);
            if (result == null) {
                // check distint slot handler
                if (handler.getSlotNames() != null && handler.getSlotNames().containsAll(data.contentSlotSearch().keySet())) {
                    boolean success = true;
                    for (var entry : data.contentSlotSearch().entrySet()) {
                        List<?> left = handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), true);
                        if (left != null) {
                            success = false;
                            break;
                        }
                    }
                    if (success) {
                        if (!simulated) {
                            for (var entry : data.contentSlot().entrySet()) {
                                handler.handleRecipe(io, recipe, entry.getValue(), entry.getKey(), false);
                            }
                        }
                        data.contentSlot().clear();
                    }
                }
                if (data.contentSlot().isEmpty()) {
                    if (!simulated) {
                        handler.handleRecipe(io, recipe, data.content(), null, false);
                    }
                    data.setContent(null);
                }
            }
            if (data.content() == null && data.contentSlot().isEmpty()) {
                break;
            }
        }
        if (data.content() != null || !data.contentSlot().isEmpty()) {
            // handle undistinct later
            for (IRecipeHandler<?> proxy : handlers) {
                if (data.used().contains(proxy) || proxy.isDistinct()) continue;
                data.used().add(proxy);
                if (data.content() != null) {
                    data.setContent(proxy.handleRecipe(io, recipe, data.content(), null, simulated));
                }
                if (proxy.getSlotNames() != null) {
                    Iterator<String> iterator = data.contentSlot().keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (proxy.getSlotNames().contains(key)) {
                            List<?> left = proxy.handleRecipe(io, recipe, data.contentSlot().get(key), key, simulated);
                            if (left == null) iterator.remove();
                        }
                    }
                }
                if (data.content() == null && data.contentSlot().isEmpty()) break;
            }
        }
        return new Tuple<>(data.content(), data.contentSlot());
    }
}
