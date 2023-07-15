package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import lombok.RequiredArgsConstructor;

import java.util.IdentityHashMap;

@RequiredArgsConstructor
public class CapabilityMap extends IdentityHashMap<RecipeCapability<?>, Content[]> implements InputReplacement, OutputReplacement {
    public final boolean isOutput;

    @Override
    public Object replaceInput(RecipeJS recipe, ReplacementMatch match, InputReplacement original) {
        if (!isOutput) {
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
        }
        return this;
    }

    @Override
    public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
        if (!isOutput) {
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
        }
        return this;
    }
}
