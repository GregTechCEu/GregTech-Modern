package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;

import java.util.function.Function;

public record PassthroughComponent<T>(Function<JsonElement, T> deserializer) implements RecipeComponent<T> {
    @Override
    public Class<?> componentClass() {
        return Object.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, T value) {
        return null;
    }

    @Override
    public T read(RecipeJS recipe, Object from) {
        if (from instanceof JsonElement json) {
            deserializer.apply(json);
        }
        return null;
    }

    @Override
    public void readFromJson(RecipeComponentValue<T> cv, JsonObject json) {
        deserializer.apply(json);
    }
}
