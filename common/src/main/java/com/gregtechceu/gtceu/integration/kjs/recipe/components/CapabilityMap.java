package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor
public class CapabilityMap extends IdentityHashMap<RecipeCapability<?>, Content[]> implements InputReplacement, OutputReplacement {

    public CapabilityMap(Map<RecipeCapability<?>, Content[]> m) {
        super(m);
    }

    public boolean isInput(RecipeJS recipe, ReplacementMatch match) {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (!pair.getFirst().isOutput()) {
                for (Content value : values) {
                    if (pair.getFirst().isInput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
            if (!pair.getSecond().isOutput()) {
                for (Content value : values) {
                    if (pair.getSecond().isInput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
        });
        return returnValue.get();
    }

    public boolean isOutput(RecipeJS recipe, ReplacementMatch match) {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (pair.getFirst().isOutput()) {
                for (Content value : values) {
                    if (pair.getFirst().isOutput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
            if (pair.getSecond().isOutput()) {
                for (Content value : values) {
                    if (pair.getSecond().isOutput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
        });
        return returnValue.get();
    }

    @Override
    public Object replaceInput(RecipeJS recipe, ReplacementMatch match, InputReplacement original) {
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (!pair.getFirst().isOutput()) {
                for (Content value : values) {
                    pair.getFirst().replaceInput(recipe, value, match, original);
                }
            } else if (!pair.getSecond().isOutput()) {
                for (Content value : values) {
                    pair.getSecond().replaceInput(recipe, value, match, original);
                }
            }
        });
        return new CapabilityMap(this);
    }

    @Override
    public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (pair.getFirst().isOutput()) {
                for (Content value : values) {
                    pair.getFirst().replaceOutput(recipe, value, match, original);
                }
            } else if (pair.getSecond().isOutput()) {
                for (Content value : values) {
                    pair.getSecond().replaceOutput(recipe, value, match, original);
                }
            }
        });
        return new CapabilityMap(this);
    }
}
