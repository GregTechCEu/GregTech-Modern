package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.*;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;

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
        if ((first == null) != (second == null)) {
            return false;
        }

        if (first instanceof SizedIngredient sized1) {
            if (second instanceof SizedIngredient sized2) {
                return cmp(sized1.getInner(), sized2.getInner());
            } else if (second instanceof IntProviderIngredient intProvider2) {
                return cmp(sized1.getInner(), intProvider2.getInner());
            } else {
                return cmp(sized1.getInner(), second);
            }
        } else if (first instanceof IntProviderIngredient intProvider1) {
            if (second instanceof IntProviderIngredient intProvider2) {
                return cmp(intProvider1.getInner(), intProvider2.getInner());
            } else if (second instanceof SizedIngredient sized) {
                return cmp(intProvider1.getInner(), sized.getInner());
            } else {
                return cmp(intProvider1.getInner(), second);
            }
        } else if (second instanceof SizedIngredient sized2) {
            return cmp(first, sized2.getInner());
        } else if (second instanceof IntProviderIngredient intProvider2) {
            return cmp(first, intProvider2.getInner());
        }
        return cmp(first, second);
    }

    private static boolean cmp(Ingredient first, Ingredient second) {
        return IngredientEquality.INGREDIENT_COMPARATOR.compare(first, second) == 0;
    }

    public static final class IngredientHashStrategy implements Hash.Strategy<Ingredient> {

        public static final IngredientHashStrategy INSTANCE = new IngredientHashStrategy();
        private static final ItemStackHashStrategy ITEM_STACK_HASH_STRATEGY = ItemStackHashStrategy.comparingAll();

        @Override
        public int hashCode(Ingredient o) {
            int hashCode = 0;
            if (o instanceof StrictNBTIngredientAccessor strict) {
                hashCode = ITEM_STACK_HASH_STRATEGY.hashCode(strict.getStack()) * 31;
            } else if (o instanceof PartialNBTIngredientAccessor partial) {
                hashCode = partial.getNbt().hashCode() * 31;
                hashCode += partial.getItems().hashCode() * 31;
            } else if (o instanceof IntersectionIngredientAccessor intersection) {
                for (Ingredient ingredient : intersection.getChildren()) {
                    hashCode += this.hashCode(ingredient) * 31;
                }
            } else if (o instanceof IngredientAccessor ingredient) {
                for (Ingredient.Value value : ingredient.getValues()) {
                    if (value instanceof TagValueAccessor tagValue) {
                        hashCode += tagValue.getTag().hashCode();
                    } else {
                        for (ItemStack stack : value.getItems()) {
                            hashCode += ITEM_STACK_HASH_STRATEGY.hashCode(stack);
                        }
                    }
                }
            }
            return hashCode;
        }

        @Override
        public boolean equals(Ingredient a, Ingredient b) {
            return IngredientEquality.ingredientEquals(a, b);
        }
    }
}
