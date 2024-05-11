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
    @FunctionalInterface
    interface ContentsHandler {
        Tuple<List, Map<String, List>> handlerContentsInternal(
            IO capIO, IO io,
            Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies,
            RecipeCapability<?> capability, RecipeHandling data, boolean simulate
        );
    }

    record RecipeHandlingResult(RecipeCapability<?> capability, Tuple<List, Map<String, List>> result) {
    }

// --------------------------------------------------------------------------------------------------------

    private final boolean simulated;

    private final Set<IRecipeHandler<?>> used = new HashSet<>();

    private List content = new ArrayList<>();
    private Map<String, List> contentSlot = new HashMap<>();

    private List contentSearch = new ArrayList<>();
    private Map<String, List> contentSlotSearch = new HashMap<>();

    private final IRecipeCapabilityHolder holder;
    private final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies;


    public RecipeHandling(IRecipeCapabilityHolder holder, boolean simulated) {
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
    public RecipeHandlingResult handle(IO io, Map.Entry<RecipeCapability<?>, List<Content>> entry, ContentsHandler contentsHandler) {
        this.fillContent(holder, entry);

        RecipeCapability<?> capability = this.resolveCapability(entry);
        if (capability == null)
            return null;

        var result = this.handleContents(io, capabilityProxies, capability, contentsHandler);
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

    private Tuple<List, Map<String, List>> handleContents(IO io, Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies, RecipeCapability<?> capability, ContentsHandler contentsHandler) {
        var result = contentsHandler.handlerContentsInternal(io, io, capabilityProxies, capability, this, simulated);

        //noinspection ConstantValue
        if (result.getA() == null && result.getB().isEmpty()) return null;

        return contentsHandler.handlerContentsInternal(IO.BOTH, io, capabilityProxies, capability, this.replaceContent(result), simulated);
    }
}
