package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.StrictNBTIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

public class IngredientEquality {

    public static boolean ingredientEquals(Ingredient first, Ingredient second) {
        if (first instanceof SizedIngredient sized1) {
            if (second instanceof SizedIngredient sized2) {
                return cmp(sized1.getInner(), sized2.getInner());
            } else {
                cmp(sized1, second);
            }
        } else if (second instanceof SizedIngredient sized2) {
            return cmp(first, sized2);
        }
        return cmp(first, second);
    }

    private static boolean cmp(Ingredient first, Ingredient second) {
        if (first == second) return true;

        if (first instanceof StrictNBTIngredient strict1) {
            if (second instanceof StrictNBTIngredientAccessor strict2) {
                return strict1.test(strict2.getStack());
            }
        }

        if (((IngredientAccessor)first).getValues().length != ((IngredientAccessor)second).getValues().length) return false;
        for (Ingredient.Value value1 : ((IngredientAccessor)first).getValues()) {
            for (Ingredient.Value value2 : ((IngredientAccessor)second).getValues()) {
                if (value1 instanceof Ingredient.TagValue tagValue) {
                    if (!(value2 instanceof Ingredient.TagValue tagValue1)) {
                        return false;
                    }
                    if (((TagValueAccessor)tagValue).getTag() != ((TagValueAccessor)tagValue1).getTag()) {
                        return false;
                    }
                } else if (value1 instanceof Ingredient.ItemValue) {
                    if (!(value2 instanceof Ingredient.ItemValue)) {
                        return false;
                    }
                    if (!value1.getItems().containsAll(value2.getItems())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
