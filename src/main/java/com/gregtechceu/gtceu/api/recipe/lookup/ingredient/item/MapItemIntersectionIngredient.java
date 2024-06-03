package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredientEquality;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MapItemIntersectionIngredient extends AbstractMapIngredient {

    protected IntersectionIngredient intersectionIngredient;
    @Getter
    protected List<Ingredient> ingredients;

    public MapItemIntersectionIngredient(IntersectionIngredient ingredient) {
        this.intersectionIngredient = ingredient;
        this.ingredients = new ArrayList<>(ingredient.children());
        this.ingredients.sort(ItemIngredientEquality.INGREDIENT_COMPARATOR);
    }

    @Override
    protected int hash() {
        int hash = 31;
        for (Ingredient ingredient : ingredients) {
            hash *= 31 * ingredient.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemIntersectionIngredient other = (MapItemIntersectionIngredient) o;
            if (this.ingredients != null) {
                if (other.ingredients != null) {
                    if (this.ingredients.size() != other.ingredients.size()) return false;
                    for (int i = 0; i < this.ingredients.size(); ++i) {
                        Ingredient ingredient1 = this.ingredients.get(i);
                        Ingredient ingredient2 = other.ingredients.get(i);
                        if (!ingredient1.equals(ingredient2)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (o instanceof MapItemStackIngredient stackIngredient) {
            return this.intersectionIngredient.test(stackIngredient.stack);
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemIntersectionIngredient{" + "ingredient=" + intersectionIngredient + "}";
    }
}
