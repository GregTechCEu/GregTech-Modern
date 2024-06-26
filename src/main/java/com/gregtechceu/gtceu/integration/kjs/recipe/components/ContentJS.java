package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
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
    public Content replace(Context cx, KubeRecipe recipe, Content original, ReplacementMatchInfo match, Object with) {
        return new Content(
                baseComponent.replace(cx, recipe, baseComponent.wrap(cx, recipe, original.content), match, with),
                original.chance, original.tierChanceBoost, original.slotName, original.uiName);
    }

    @Override
    public String toString() {
        return "content[" + baseComponent + "]";
    }
}
