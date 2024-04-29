package com.gregtechceu.gtceu.utils;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

import com.google.common.collect.Lists;

import java.util.*;

public class IngredientEquality {

    public static final Comparator<ItemStack> STACK_COMPARATOR = Comparator
            .comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()));

    public static final Comparator<Ingredient.Value> INGREDIENT_VALUE_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(Ingredient.Value value1, Ingredient.Value value2) {
            if (value1 instanceof Ingredient.TagValue tagValue) {
                if (!(value2 instanceof Ingredient.TagValue tagValue1)) {
                    return 1;
                }
                if (tagValue.tag() != tagValue1.tag()) {
                    return 1;
                }
            } else if (value1 instanceof Ingredient.ItemValue) {
                if (!(value2 instanceof Ingredient.ItemValue)) {
                    return 1;
                }
                for (ItemStack item1 : value1.getItems()) {
                    for (ItemStack item2 : value2.getItems()) {
                        int result = STACK_COMPARATOR.compare(item1, item2);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            }
            return 0;
        }
    };

    public static final Comparator<ItemStack> ITEM_STACK_COMPARATOR = Comparator.comparingInt(ItemStack::getCount).thenComparing(ItemStack::getItem, Comparator.comparing(BuiltInRegistries.ITEM::getKey));

    public static final Comparator<Ingredient> INGREDIENT_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(Ingredient first, Ingredient second) {
            if (first.getCustomIngredient() instanceof DataComponentIngredient strict1 && strict1.isStrict()) {
                if (second.getCustomIngredient() instanceof DataComponentIngredient strict2 && strict2.isStrict()) {
                    return strict1.test(strict2.getItems().findFirst().orElse(ItemStack.EMPTY)) ? 0 : 1;
                }
                return 1;
            }
            if (first.getCustomIngredient() instanceof DataComponentIngredient partial1 && !partial1.isStrict()) {
                if (second.getCustomIngredient() instanceof DataComponentIngredient partial2 && !partial2.isStrict()) {
                    if (partial1.getItems().count() != partial2.getItems().count())
                        return 1;
                    for (ItemStack stack : partial1.getItems().toList()) {
                        if (!partial2.test(stack)) {
                            return 1;
                        }
                    }
                    return 0;
                }
                return 1;
            }

            if (first.getCustomIngredient() instanceof IntersectionIngredient intersection1) {
                if (second.getCustomIngredient() instanceof IntersectionIngredient intersection2) {
                    List<Ingredient> ingredients1 = Lists.newArrayList(intersection1.children());
                    List<Ingredient> ingredients2 = Lists.newArrayList(intersection2.children());
                    if (ingredients1.size() != ingredients2.size()) return 1;

                    ingredients1.sort(this);
                    ingredients2.sort(this);

                    for (int i = 0; i < ingredients1.size(); ++i) {
                        Ingredient ingredient1 = ingredients1.get(i);
                        Ingredient ingredient2 = ingredients2.get(i);
                        int result = compare(ingredient1, ingredient2);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                }
                return 1;
            }
            if (first.isCustom() || second.isCustom()) {
                ICustomIngredient firstCustom = first.getCustomIngredient();
                ICustomIngredient secondCustom = second.getCustomIngredient();
                if (firstCustom.getItems().count() != secondCustom.getItems().count())
                    return 1;
                ItemStack[] values1 = firstCustom.getItems().toArray(ItemStack[]::new);
                ItemStack[] values2 = firstCustom.getItems().toArray(ItemStack[]::new);
                if (values1.length != values2.length) return 1;

                Arrays.parallelSort(values1, ITEM_STACK_COMPARATOR);
                Arrays.parallelSort(values2, ITEM_STACK_COMPARATOR);

                for (int i = 0; i < values1.length; ++i) {
                    ItemStack value1 = values1[i];
                    ItemStack value2 = values2[i];
                    int result = ITEM_STACK_COMPARATOR.compare(value1, value2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }

            if (first.getValues().length != second.getValues().length)
                return 1;
            Ingredient.Value[] values1 = first.getValues();
            Ingredient.Value[] values2 = second.getValues();

            Arrays.parallelSort(values1, INGREDIENT_VALUE_COMPARATOR);
            Arrays.parallelSort(values2, INGREDIENT_VALUE_COMPARATOR);

            for (int i = 0; i < values1.length; ++i) {
                Ingredient.Value value1 = values1[i];
                Ingredient.Value value2 = values2[i];
                int result = INGREDIENT_VALUE_COMPARATOR.compare(value1, value2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    };
}
