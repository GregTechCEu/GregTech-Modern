package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.TinyMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;

import java.util.ArrayList;
import java.util.Arrays;

public interface GTShapedRecipeSchema {

    class ShapedRecipeJS extends RecipeJS {

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

            final var symbols = ToolHelper.getSymbols();

            CharList airs = new CharArrayList(1);

            var entries = new ArrayList<>(Arrays.asList(key.entries()));
            var itr = entries.iterator();

            while (itr.hasNext()) {
                var entry = itr.next();
                if (entry.value() == null || entry.value().isEmpty()) {
                    airs.add(entry.key().charValue());
                    itr.remove();
                } else if (symbols.containsKey(entry.key())) {
                    ConsoleJS.SERVER.warn("Symbol {" + entry.key() + "} set as key in tooled recipe - overriding");
                    itr.remove();
                }
            }

            boolean changed = false;
            for (int i = 0; i < pattern.length; i++) {
                for (var it = airs.iterator(); it.hasNext();) {
                    pattern[i] = pattern[i].replace(it.nextChar(), ' ');
                }
                for (var symbolEntry : ToolHelper.getSymbols().entrySet()) {
                    var c = symbolEntry.getKey();
                    if (pattern[i].indexOf(c) >= 0) {
                        entries.add(new TinyMap.Entry<>(c, InputItem.of(symbolEntry.getValue().itemTags.get(0))));
                        changed = true;
                    }
                }
            }

            if (!airs.isEmpty() || changed) {
                setValue(PATTERN, pattern);
                setValue(KEY, new TinyMap<>(entries));
            }
        }

        @Override
        public RecipeTypeFunction getSerializationTypeFunction() {
            return type.event.vanillaShaped;
        }
    }

    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<String[]> PATTERN = StringComponent.NON_EMPTY.asArray().key("pattern");
    RecipeKey<TinyMap<Character, InputItem>> KEY = MapRecipeComponent.ITEM_PATTERN_KEY.key("key");

    RecipeSchema SCHEMA = new RecipeSchema(ShapedRecipeJS.class, ShapedRecipeJS::new, RESULT, PATTERN, KEY)
            .constructor(RESULT, PATTERN, KEY)
            .uniqueOutputId(RESULT);
}
