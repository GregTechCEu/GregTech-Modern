package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public class CapabilityMapComponent implements RecipeComponent<CapabilityMap> {

    @Override
    public Codec<CapabilityMap> codec() {
        return CapabilityMap.CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(CapabilityMap.class);
    }

    @Override
    public CapabilityMap replaceInput(Context cx, KubeRecipe recipe, CapabilityMap original, ReplacementMatch match,
                                      InputReplacement with) {
        return wrap(cx, recipe, original.replaceInput(cx, recipe, match, with));
    }

    @Override
    public CapabilityMap replaceOutput(Context cx, KubeRecipe recipe, CapabilityMap original, ReplacementMatch match,
                                       OutputReplacement with) {
        return wrap(cx, recipe, original.replaceOutput(cx, recipe, match, with));
    }
}
