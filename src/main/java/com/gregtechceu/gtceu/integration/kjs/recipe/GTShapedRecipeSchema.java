package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.TinyMap;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.KEY;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.PATTERN;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.RESULT;

public interface GTShapedRecipeSchema {

    class ShapedRecipeJS extends RecipeJS {

        @Getter
        protected boolean addMaterialInfo = false;

        public ShapedRecipeJS addMaterialInfo() {
            addMaterialInfo = true;
            return this;
        }

        @HideFromJS
        public List<IngredientAction> getIngredientActions() {
            if (recipeIngredientActions == null) return Collections.emptyList();
            return recipeIngredientActions;
        }

        // Adapted from KJS's ShapedRecipeSchema#ShapedRecipeJS
        @Override
        public void afterLoaded() {
            super.afterLoaded();
            var pattern = getValue(PATTERN);
            var key = getValue(KEY);

            if (pattern.length == 0) {
                throw new RecipeExceptionJS("Pattern is empty!");
            }

            if (key.isEmpty()) {
                throw new RecipeExceptionJS("Key map is empty!");
            }

            final CharSet tools = ToolHelper.getToolSymbols();
            CharSet addedTools = new CharArraySet(9);

            CharList airs = new CharArrayList(1);

            var keyEntries = new ArrayList<>(Arrays.asList(key.entries()));
            for (var it = keyEntries.iterator(); it.hasNext();) {
                var entry = it.next();
                char entryKey = entry.key();
                if (entry.value() == null || entry.value().isEmpty()) {
                    airs.add(entryKey);
                    it.remove();
                } else if (tools.contains(entryKey)) {
                    ConsoleJS.SERVER.warn("Symbol {" + entryKey + "} set as key in tooled recipe - overriding");
                    it.remove();
                }
            }

            for (int i = 0; i < pattern.length; i++) {
                for (var it = airs.iterator(); it.hasNext();) {
                    pattern[i] = pattern[i].replace(it.nextChar(), ' ');
                }
                for (char c : pattern[i].toCharArray()) { // Inject tool symbol mappings
                    if (tools.contains(c) && !addedTools.contains(c)) {
                        var tool = ToolHelper.getToolFromSymbol(c);
                        keyEntries.add(new TinyMap.Entry<>(c, InputItem.of(tool.craftingTags.get(0))));
                        addedTools.add(c);
                    }
                }
            }

            if (!airs.isEmpty() || !addedTools.isEmpty()) {
                setValue(PATTERN, pattern);
                setValue(KEY, new TinyMap<>(keyEntries));
            }
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(ShapedRecipeJS.class, ShapedRecipeJS::new, RESULT, PATTERN, KEY)
            .constructor(RESULT, PATTERN, KEY)
            .uniqueOutputId(RESULT);
}
