package com.gregtechceu.gtceu.integration.kjs.recipe;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.component.ComponentValueMap;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.Context;

public class IDRecipeConstructor extends RecipeConstructor {

    public IDRecipeConstructor() {
        super(GTRecipeSchema.ID);
    }

    @Override
    public KubeRecipe create(Context cx, SourceLine sourceLine, RecipeTypeFunction type, RecipeSchemaType schemaType,
                             ComponentValueMap from) {
        var r = super.create(cx, sourceLine, type, schemaType, from);
        r.id(KubeResourceLocation.wrap(from.getValue(cx, r, GTRecipeSchema.ID)));
        return r;
    }

    @Override
    public void setValues(Context cx, KubeRecipe recipe, RecipeSchemaType schemaType, ComponentValueMap from) {
        for (var entry : overrides.entrySet()) {
            recipe.setValue(entry.getKey(), Cast.to(entry.getValue().getDefaultValue(schemaType)));
        }
    }
}
