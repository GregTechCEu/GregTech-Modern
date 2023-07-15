package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.util.GsonHelper;

public record ContentJS<T>(RecipeComponent<T> baseComponent, boolean isOutput) implements RecipeComponent<Content> {

    @Override
    public Class<?> componentClass() {
        return Content.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, Content value) {
        JsonObject object = new JsonObject();
        object.add("content", baseComponent.write(recipe, UtilsJS.cast(value.content)));
        object.addProperty("chance", value.chance);
        object.addProperty("tierChanceBoost", value.tierChanceBoost);
        if (value.slotName != null) {
            object.addProperty("slotName", value.slotName);
        }
        if (value.uiName != null) {
            object.addProperty("uiName", value.uiName);
        }
        return object;
    }

    @Override
    public Content read(RecipeJS recipe, Object from) {
        if (from instanceof Content) return (Content) from;
        else if (from instanceof JsonObject json) {
            Object content = baseComponent.read(recipe, GsonHelper.getAsJsonObject(json, "content", new JsonObject()));
            float chance = GsonHelper.getAsFloat(json, "chance", 0.0f);
            float tierChanceBoost = GsonHelper.getAsFloat(json, "tierChanceBoost", 0.0f);
            String slotName = GsonHelper.getAsString(json, "slotName", null);
            String uiName = GsonHelper.getAsString(json, "uiName", null);
            return new Content(content, chance, tierChanceBoost, slotName, uiName);
        }
        return null;
    }

    @Override
    public Content replaceInput(RecipeJS recipe, Content original, ReplacementMatch match, InputReplacement with) {
        return new Content(baseComponent.replaceInput(recipe, UtilsJS.cast(original.content), match, with), original.chance, original.tierChanceBoost, original.slotName, original.uiName);
    }

    @Override
    public Content replaceOutput(RecipeJS recipe, Content original, ReplacementMatch match, OutputReplacement with) {
        return new Content(baseComponent.replaceOutput(recipe, UtilsJS.cast(original.content), match, with), original.chance, original.tierChanceBoost, original.slotName, original.uiName);
    }
}
