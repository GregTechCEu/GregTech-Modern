package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionHolder;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaFunction;
import dev.latvian.mods.kubejs.recipe.special.KubeJSCraftingRecipe;
import dev.latvian.mods.kubejs.script.ConsoleJS;
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

public interface GTShapedRecipeSchema {

    class ShapedKubeRecipe extends KubeRecipe {

        @Getter
        protected boolean addMaterialInfo = false;

        public ShapedKubeRecipe addMaterialInfo() {
            addMaterialInfo = true;
            return this;
        }

        @HideFromJS
        public List<IngredientActionHolder> getIngredientActions() {
            if (recipeIngredientActions == null) return Collections.emptyList();
            return recipeIngredientActions;
        }

        // Adapted from KJS's ShapedRecipeSchema#ShapedKubeRecipe
        @Override
        public void afterLoaded() {
            super.afterLoaded();
            var pattern = new ArrayList<>(getValue(PATTERN));
            var key = getValue(KEY);

            if (pattern.isEmpty()) {
                throw new KubeRuntimeException("Pattern is empty!");
            }

            if (key.isEmpty()) {
                throw new KubeRuntimeException("Key map is empty!");
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

            for (int i = 0; i < pattern.size(); i++) {
                for (var it = airs.iterator(); it.hasNext();) {
                    pattern.set(i, pattern.get(i).replace(it.nextChar(), ' '));
                }
                for (char c : pattern.get(i).toCharArray()) { // Inject tool symbol mappings
                    if (tools.contains(c) && !addedTools.contains(c)) {
                        var tool = ToolHelper.getToolFromSymbol(c);
                        keyEntries.add(new TinyMap.Entry<>(c, Ingredient.of(tool.itemTags.getFirst())));
                        addedTools.add(c);
                    }
                }
            }

            if (!airs.isEmpty() || !addedTools.isEmpty()) {
                setValue(PATTERN, pattern);
                setValue(KEY, new TinyMap<>(keyEntries));
            }
        }

        @Override
        public RecipeTypeFunction getSerializationTypeFunction() {
            // Use vanilla shaped recipe type if KubeJS is not needed
            if (type == type.event.shaped // if this type == kubejs:shaped
                    && type.event.shaped != type.event.vanillaShaped // check if not in serverOnly mode
                    && !json.has(KubeJSCraftingRecipe.INGREDIENT_ACTIONS_KEY) &&
                    !json.has(KubeJSCraftingRecipe.MODIFY_RESULT_KEY) && !json.has(KubeJSCraftingRecipe.STAGE_KEY) &&
                    !json.has(KubeJSCraftingRecipe.MIRROR_KEY)) {
                return type.event.vanillaShaped;
            }

            return super.getSerializationTypeFunction();
        }
    }

    RecipeKey<ItemStack> RESULT = ItemStackComponent.STRICT_ITEM_STACK.outputKey("result");
    RecipeKey<List<String>> PATTERN = StringGridComponent.STRING_GRID.otherKey("pattern");
    RecipeKey<TinyMap<Character, Ingredient>> KEY = IngredientComponent.INGREDIENT
            .asPatternKey().inputKey("key");
    RecipeKey<Boolean> MIRROR = BooleanComponent.BOOLEAN.otherKey(KubeJSCraftingRecipe.MIRROR_KEY)
            .optional(true).exclude()
            .functionNames(List.of("kjsMirror"));
    RecipeKey<Boolean> SHRINK = BooleanComponent.BOOLEAN.otherKey("kubejs:shrink")
            .optional(true).exclude()
            .functionNames(List.of("kjsShrink"));
    RecipeKey<CraftingBookCategory> CATEGORY = BookCategoryComponent.CRAFTING_BOOK_CATEGORY.otherKey("category")
            .optional(CraftingBookCategory.MISC)
            .functionNames(List.of("kjsShrink"));

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, PATTERN, KEY, MIRROR, SHRINK, CATEGORY)
            .factory(new KubeRecipeFactory(GTCEu.id("shaped"), ShapedKubeRecipe.class, ShapedKubeRecipe::new))
            .constructor(RESULT, PATTERN, KEY)
            .uniqueId(RESULT)
            .typeOverride(KubeJS.id("shaped"))
            .function("noMirror", new RecipeSchemaFunction.SetFunction<>(MIRROR, false))
            .function("noShrink", new RecipeSchemaFunction.SetFunction<>(SHRINK, false));
}
