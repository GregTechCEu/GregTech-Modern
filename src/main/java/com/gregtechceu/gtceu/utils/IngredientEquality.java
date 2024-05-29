package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.IntersectionIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.StrictNBTIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

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
                if (((TagValueAccessor) tagValue).getTag() != ((TagValueAccessor) tagValue1).getTag()) {
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

    public static final Comparator<Ingredient> INGREDIENT_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(Ingredient first, Ingredient second) {
            if (first instanceof StrictNBTIngredient strict1) {
                if (second instanceof StrictNBTIngredientAccessor strict2) {
                    return strict1.test(strict2.getStack()) ? 0 : 1;
                }
                return 1;
            }
            if (first instanceof PartialNBTIngredient partial1) {
                if (second instanceof PartialNBTIngredient partial2) {
                    if (partial1.getItems().length != partial2.getItems().length)
                        return 1;
                    for (ItemStack stack : partial1.getItems()) {
                        if (!partial2.test(stack)) {
                            return 1;
                        }
                    }
                    return 0;
                }
                return 1;
            }

            if (first instanceof IntersectionIngredient intersection1) {
                if (second instanceof IntersectionIngredient intersection2) {
                    List<Ingredient> ingredients1 = Lists
                            .newArrayList(((IntersectionIngredientAccessor) intersection1).getChildren());
                    List<Ingredient> ingredients2 = Lists
                            .newArrayList(((IntersectionIngredientAccessor) intersection2).getChildren());
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

            if (((IngredientAccessor) first).getValues().length != ((IngredientAccessor) second).getValues().length)
                return 1;
            Ingredient.Value[] values1 = ((IngredientAccessor) first).getValues();
            Ingredient.Value[] values2 = ((IngredientAccessor) second).getValues();
            if (values1.length != values2.length) return 1;

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

    public static boolean ingredientEquals(Ingredient first, Ingredient second) {
        if (first == second) return true;

        if (first instanceof SizedIngredient sized1) {
            if (second instanceof SizedIngredient sized2) {
                return cmp(sized1.getInner(), sized2.getInner());
            } else {
                return cmp(sized1, second);
            }
        } else if (second instanceof SizedIngredient sized2) {
            return cmp(first, sized2.getInner());
        }
        return cmp(first, second);
    }

    private static boolean cmp(Ingredient first, Ingredient second) {
        return IngredientEquality.INGREDIENT_COMPARATOR.compare(first, second) == 0;
    }
}
