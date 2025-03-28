package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.util.GsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;

public record ContentJS<T>(RecipeComponent<T> baseComponent, RecipeCapability<?> capability, boolean isOutput)
        implements RecipeComponent<Content> {

    @Override
    public ComponentRole role() {
        return isOutput ? ComponentRole.OUTPUT : ComponentRole.INPUT;
    }

    @Override
    public Class<?> componentClass() {
        return Content.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Content value) {
        JsonObject object = new JsonObject();
        object.add("content", baseComponent.write(recipe, baseComponent.read(recipe, value.content)));
        object.addProperty("chance", value.chance);
        object.addProperty("maxChance", value.maxChance);
        object.addProperty("tierChanceBoost", value.tierChanceBoost);
        return object;
    }

    @Override
    public Content read(RecipeJS recipe, Object from) {
        if (from instanceof Content) return (Content) from;
        else if (from instanceof JsonObject json) {
            Object content = baseComponent.read(recipe, json.get("content"));
            int chance = GsonHelper.getAsInt(json, "chance", ChanceLogic.getMaxChancedValue());
            int maxChance = GsonHelper.getAsInt(json, "maxChance", ChanceLogic.getMaxChancedValue());
            int tierChanceBoost = GsonHelper.getAsInt(json, "tierChanceBoost", 0);
            return new Content(content, chance, maxChance, tierChanceBoost);
        }
        return null;
    }

    @Override
    public boolean isInput(RecipeJS recipe, Content value, ReplacementMatch match) {
        return !isOutput && baseComponent.isInput(recipe, baseComponent.read(recipe, value.content), match);
    }

    @Override
    public boolean isOutput(RecipeJS recipe, Content value, ReplacementMatch match) {
        return isOutput && baseComponent.isOutput(recipe, baseComponent.read(recipe, value.content), match);
    }

    @Override
    public Content replaceInput(RecipeJS recipe, Content original, ReplacementMatch match, InputReplacement with) {
        return isInput(recipe, original, match) ? new Content(
                baseComponent.replaceInput(recipe, baseComponent.read(recipe, original.content), match, with),
                original.chance, original.maxChance, original.tierChanceBoost) :
                original;
    }

    @Override
    public Content replaceOutput(RecipeJS recipe, Content original, ReplacementMatch match, OutputReplacement with) {
        return isOutput(recipe, original, match) ? new Content(with.replaceOutput(recipe, match, with),
                original.chance, original.maxChance, original.tierChanceBoost) :
                original;
    }
}
