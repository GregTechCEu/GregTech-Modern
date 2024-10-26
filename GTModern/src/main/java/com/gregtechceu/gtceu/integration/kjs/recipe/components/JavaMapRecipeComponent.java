package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;

import java.util.HashMap;
import java.util.Map;

public record JavaMapRecipeComponent<K, V>(RecipeComponent<K> key, RecipeComponent<V> component)
        implements RecipeComponent<Map<K, V>> {

    @Override
    public String componentType() {
        return "map";
    }

    @Override
    public TypeDescJS constructorDescription(DescriptionContext ctx) {
        return component.constructorDescription(ctx).asMap(key.constructorDescription(ctx));
    }

    @Override
    public ComponentRole role() {
        return component.role();
    }

    @Override
    public Class<?> componentClass() {
        return Map.class;
    }

    @Override
    public JsonObject write(RecipeJS recipe, Map<K, V> value) {
        var json = new JsonObject();

        for (var entry : value.entrySet()) {
            json.add(key.write(recipe, entry.getKey()).getAsString(), component.write(recipe, entry.getValue()));
        }

        return json;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<K, V> read(RecipeJS recipe, Object from) {
        if (from instanceof Map map) {
            return map;
        } else if (from instanceof JsonObject o) {
            Map<K, V> map = new HashMap<>();
            int i = 0;

            for (var entry : o.entrySet()) {
                var k = key.read(recipe, entry.getKey());
                var v = component.read(recipe, entry.getValue());
                map.put(k, v);
            }

            return map;
        } else {
            throw new IllegalArgumentException("Expected JSON object!");
        }
    }

    @Override
    public boolean isInput(RecipeJS recipe, Map<K, V> value, ReplacementMatch match) {
        for (var entry : value.entrySet()) {
            if (component.isInput(recipe, entry.getValue(), match)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<K, V> replaceInput(RecipeJS recipe, Map<K, V> original, ReplacementMatch match, InputReplacement with) {
        var map = original;

        for (Map.Entry<K, V> entry : original.entrySet()) {
            var r = component.replaceInput(recipe, entry.getValue(), match, with);
            if (r != entry.getValue()) {
                if (map == original) {
                    map = new HashMap<>(original);
                }
                map.put(entry.getKey(), r);
            }
        }

        return map;
    }

    @Override
    public boolean isOutput(RecipeJS recipe, Map<K, V> value, ReplacementMatch match) {
        for (var entry : value.entrySet()) {
            if (component.isOutput(recipe, entry.getValue(), match)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<K, V> replaceOutput(RecipeJS recipe, Map<K, V> original, ReplacementMatch match,
                                   OutputReplacement with) {
        var map = original;

        for (Map.Entry<K, V> entry : original.entrySet()) {
            var r = component.replaceOutput(recipe, entry.getValue(), match, with);
            if (r != entry.getValue()) {
                if (map == original) {
                    map = new HashMap<>(original);
                }
                map.put(entry.getKey(), r);
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "java_map{" + key + ":" + component + "}";
    }
}
