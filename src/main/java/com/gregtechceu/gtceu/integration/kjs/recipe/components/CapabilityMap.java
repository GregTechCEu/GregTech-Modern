package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.rhino.Context;
import lombok.NoArgsConstructor;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@NoArgsConstructor
public class CapabilityMap extends IdentityHashMap<RecipeCapability<?>, List<Content>>
                           implements InputReplacement, OutputReplacement {

    public static final Codec<CapabilityMap> CODEC = RecipeCapability.CODEC
            .xmap(CapabilityMap::new, Function.identity());

    public CapabilityMap(Map<RecipeCapability<?>, List<Content>> m) {
        super(m);
    }

    public void add(RecipeCapability<?> capability, Content value) {
        this.get(capability).add(value);
    }

    @Override
    public CapabilityMap replaceInput(Context cx, KubeRecipe recipe, ReplacementMatch match, InputReplacement with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            for (int i = 0; i < values.size(); ++i) {
                Content value = values.get(i);
                Content result = pair.replaceInput(cx, recipe, value, match, with);
                if (!result.equals(value)) {
                    changed.set(true);
                    values.set(i, result);
                }
            }
        });
        return changed.get() ? new CapabilityMap(this) : this;
    }

    @Override
    public CapabilityMap replaceOutput(Context cx, KubeRecipe recipe, ReplacementMatch match, OutputReplacement with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            for (int i = 0; i < values.size(); ++i) {
                Content value = values.get(i);
                Content result = pair.replaceOutput(cx, recipe, value, match, with);
                if (!result.equals(value)) {
                    changed.set(true);
                    values.set(i, result);
                }
            }
        });
        return changed.get() ? new CapabilityMap(this) : this;
    }
}
