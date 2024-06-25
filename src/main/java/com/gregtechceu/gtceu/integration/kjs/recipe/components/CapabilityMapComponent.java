package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public record CapabilityMapComponent(boolean isTick) implements RecipeComponent<CapabilityMap> {

    public static final Codec<CapabilityMap> CODEC = RecipeCapability.CODEC
            .xmap(CapabilityMap::new, Function.identity());

    @Override
    public Codec<CapabilityMap> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(CapabilityMap.class);
    }

    @Override
    public CapabilityMap replace(Context cx, KubeRecipe recipe, CapabilityMap original,
                                 ReplacementMatchInfo match, Object with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        original.forEach((key, values) -> {
            var content = GTRecipeComponents.VALID_CAPS.get(key);
            for (int i = 0; i < values.size(); ++i) {
                Content value = values.get(i);
                Content result = content.replace(cx, recipe, value, match, with);
                if (!result.equals(value)) {
                    changed.set(true);
                    values.set(i, result);
                }
            }
        });
        return changed.get() ? new CapabilityMap(original) : original;
    }

    @Override
    public String toString() {
        return "capability_map[tick=" + isTick + "]";
    }
}
