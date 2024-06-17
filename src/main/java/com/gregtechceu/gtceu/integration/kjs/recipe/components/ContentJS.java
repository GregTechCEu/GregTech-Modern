package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;

public record ContentJS<T>(RecipeComponent<T> baseComponent, RecipeCapability<?> capability)
        implements RecipeComponent<Content> {

    @Override
    public Codec<Content> codec() {
        return Content.codec(capability);
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Content.class);
    }

    @Override
    public Content replaceInput(Context cx, KubeRecipe recipe, Content original, ReplacementMatch match,
                                InputReplacement with) {
        return new Content(
                baseComponent.replaceInput(cx, recipe, baseComponent.wrap(cx, recipe, original.content), match, with),
                original.chance, original.tierChanceBoost, original.slotName, original.uiName);
    }

    @Override
    public Content replaceOutput(Context cx, KubeRecipe recipe, Content original, ReplacementMatch match,
                                 OutputReplacement with) {
        return new Content(with.replaceOutput(cx, recipe, match, with), original.chance,
                original.tierChanceBoost, original.slotName, original.uiName);
    }
}
