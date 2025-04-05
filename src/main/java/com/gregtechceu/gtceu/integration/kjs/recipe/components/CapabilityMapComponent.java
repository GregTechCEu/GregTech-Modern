package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.util.GsonHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import java.util.Set;

public record CapabilityMapComponent(boolean isOutput) implements RecipeComponent<CapabilityMap> {

    @Override
    public ComponentRole role() {
        return isOutput ? ComponentRole.OUTPUT : ComponentRole.INPUT;
    }

    @Override
    public boolean isOutput(RecipeJS recipe, CapabilityMap value, ReplacementMatch match) {
        return isOutput && value.isOutput(recipe, match);
    }

    @Override
    public boolean isInput(RecipeJS recipe, CapabilityMap value, ReplacementMatch match) {
        return !isOutput && value.isInput(recipe, match);
    }

    @Override
    public Class<?> componentClass() {
        return CapabilityMap.class;
    }

    @Override
    public CapabilityMap replaceInput(RecipeJS recipe, CapabilityMap original, ReplacementMatch match,
                                      InputReplacement with) {
        return isInput(recipe, original, match) ? read(recipe, original.replaceInput(recipe, match, with)) : original;
    }

    @Override
    public CapabilityMap replaceOutput(RecipeJS recipe, CapabilityMap original, ReplacementMatch match,
                                       OutputReplacement with) {
        return isOutput(recipe, original, match) ? read(recipe, original.replaceOutput(recipe, match, with)) : original;
    }

    @Override
    public JsonElement write(RecipeJS recipe, CapabilityMap map) {
        JsonObject json = new JsonObject();
        map.forEach((key, value) -> {
            JsonArray array = new JsonArray();
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            for (Content content : value) {
                array.add((isOutput ? pair.getSecond() : pair.getFirst()).write(recipe, content));
            }
            json.add(key.name, array);
        });
        return json;
    }

    @Override
    public CapabilityMap read(RecipeJS recipe, Object from) {
        if (from instanceof CapabilityMap map) return map;
        CapabilityMap map = new CapabilityMap();
        if (from instanceof JsonObject json) {
            for (String key : json.keySet()) {
                if (GTRegistries.RECIPE_CAPABILITIES.containKey(key) &&
                        GTRegistries.RECIPE_CAPABILITIES.get(key) != null) {
                    RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(key);
                    var pair = GTRecipeComponents.VALID_CAPS.get(cap);
                    Set<Content> result = new ObjectLinkedOpenHashSet<>();
                    JsonArray value = GsonHelper.getAsJsonArray(json, key, new JsonArray());
                    for (int i = 0; i < value.size(); ++i) {
                        result.add((isOutput ? pair.getSecond() : pair.getFirst()).read(recipe, value.get(i)));
                    }
                    map.put(cap, result.toArray(Content[]::new));
                }
            }
        }
        return map;
    }
}
