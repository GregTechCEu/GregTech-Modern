package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.HashMap;
import java.util.Map;

public record JavaMapRecipeComponent<K, V>(RecipeComponent<K> key, RecipeComponent<V> value)
        implements RecipeComponent<Map<K, V>> {

    @Override
    public Map<K, V> replace(RecipeScriptContext cx, Map<K, V> original, ReplacementMatchInfo match,
                             Object with) {
        var map = original;

        for (Map.Entry<K, V> entry : original.entrySet()) {
            var r = value.replace(cx, entry.getValue(), match, with);
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
        return "java_map{" + key + ":" + value + "}";
    }

    @Override
    public Codec<Map<K, V>> codec() {
        return Codec.unboundedMap(key.codec(), value.codec());
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.RAW_MAP.withParams(key.typeInfo(), value.typeInfo());
    }

    public @Override RecipeComponentType<Map<K, V>> type() {
        return RecipeComponentType.<Map<K, V>>unit(ResourceLocation.parse("java_map{" + key + ":" + value + "}"), this);
    }
}
