package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.TinyMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

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

            final var symbols = ToolHelper.getToolSymbols();

            CharList airs = new CharArrayList(1);

            var keyEntries = new ArrayList<>(Arrays.asList(key.entries()));
            boolean changed = false;
            for (var it = keyEntries.iterator(); it.hasNext();) {
                var entry = it.next();
                if (entry.value() == null || entry.value().isEmpty()) {
                    airs.add(entry.key().charValue());
                    it.remove();
                } else if (symbols.contains(entry.key())) {
                    ConsoleJS.SERVER.warn("Symbol {" + entry.key() + "} set as key in tooled recipe - overriding");
                    changed = true;
                    it.remove();
                }
            }

            for (int i = 0; i < pattern.length; i++) {
                for (var it = airs.iterator(); it.hasNext();) {
                    pattern[i] = pattern[i].replace(it.nextChar(), ' ');
                }
                for (Character c : pattern[i].toCharArray()) {
                    if (symbols.contains(c)) {
                        var tool = ToolHelper.getToolFromSymbol(c);
                        keyEntries.add(new TinyMap.Entry<>(c, InputItem.of(tool.itemTags.get(0))));
                        changed = true;
                    }
                }
            }

            if (!airs.isEmpty() || changed) {
                setValue(PATTERN, pattern);
                setValue(KEY, new TinyMap<>(keyEntries));
            }

            Boolean addInfo = getValue(MATERIAL_INFO);
            if (addInfo == null || !addInfo) return;

            // Parse Material Info
            Object2IntOpenHashMap<Character> inputMap = new Object2IntOpenHashMap<>();
            for (String s : pattern) {
                for (Character c : s.toCharArray()) {
                    if (symbols.contains(c)) continue;
                    inputMap.addTo(c, 1);
                }
            }
            if (inputMap.isEmpty()) return;
            var result = getValue(RESULT);
            ItemStack outItem = result.item;
            int outCount = result.getCount();
            Reference2LongOpenHashMap<Material> materials = new Reference2LongOpenHashMap<>();

            for (var entry : keyEntries) {
                Character c = entry.key();
                int inCount = inputMap.getInt(c);
                if (inCount == 0) continue;
                ItemStack[] stacks = entry.value().kjs$asIngredient().getItems();
                if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                var item = stacks[0].getItem();

                var info = ItemMaterialData.getMaterialInfo(item);
                if (info != null) {
                    for (var ms : info.getMaterials()) {
                        if (ms.material() instanceof MarkerMaterial) continue;
                        materials.addTo(ms.material(), (ms.amount() * inCount) / outCount);
                    }
                    continue;
                } else {
                    ItemMaterialData.UNRESOLVED_ITEM_MATERIAL_INFO.computeIfAbsent(outItem, i -> new ArrayList<>())
                            .add(stacks[0].copyWithCount(inCount));
                }

                var matStack = ChemicalHelper.getMaterialStack(item);
                if (!matStack.isEmpty() && !(matStack.material() instanceof MarkerMaterial)) {
                    materials.addTo(matStack.material(), (matStack.amount() * inCount) / outCount);
                }

                var prefix = ChemicalHelper.getPrefix(item);
                if (!prefix.isEmpty()) {
                    for (var ms : prefix.secondaryMaterials()) {
                        materials.addTo(ms.material(), (ms.amount() * inCount) / outCount);
                    }
                }
            }

            ItemMaterialData.registerMaterialInfo(outItem.getItem(), new ItemMaterialInfo(materials));
        }
    }

    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<String[]> PATTERN = StringComponent.NON_EMPTY.asArray().key("pattern");
    RecipeKey<TinyMap<Character, InputItem>> KEY = MapRecipeComponent.ITEM_PATTERN_KEY.key("key");
    RecipeKey<Boolean> MATERIAL_INFO = BooleanComponent.BOOLEAN.key("gtceu:material_info")
            .preferred("materialInfo")
            .optional(Boolean.FALSE)
            .exclude();

    RecipeSchema SCHEMA = new RecipeSchema(ShapedRecipeJS.class, ShapedRecipeJS::new, RESULT, PATTERN, KEY,
            MATERIAL_INFO)
            .constructor(RESULT, PATTERN, KEY)
            .constructor(RESULT, PATTERN, KEY, MATERIAL_INFO)
            .uniqueOutputId(RESULT);
}
