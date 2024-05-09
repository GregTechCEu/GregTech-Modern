package com.gregtechceu.gtceu.api.recipes.lookup;

import com.gregtechceu.gtceu.utils.IngredientEquality;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

import java.util.ArrayList;
import java.util.List;

public class MapIntersectionIngredient extends AbstractMapIngredient {
    protected IntersectionIngredient intersectionIngredient;
    protected List<Ingredient> ingredients;

    public MapIntersectionIngredient(IntersectionIngredient ingredient) {
        this.intersectionIngredient = ingredient;
        this.ingredients = new ArrayList<>(ingredient.children());
        this.ingredients.sort(IngredientEquality.INGREDIENT_COMPARATOR);
    }

    @Override
    protected int hash() {
        int hash = 31;
        for (Ingredient ingredient : ingredients) {
            for (Ingredient.Value value : ingredient.getValues()) {
                if (value instanceof Ingredient.TagValue tagValue) {
                    hash *= 31 * tagValue.tag().location().hashCode();
                } else if (value instanceof Ingredient.ItemValue itemValue) {
                    hash *= 31 * itemValue.item().getItem().hashCode();
                }
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapIntersectionIngredient other = (MapIntersectionIngredient) o;
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
    public boolean isSpecialIngredient() {
        return true;
    }

    @Override
    public String toString() {
        return "MapIntersectionIngredient{" + "ingredient=" + intersectionIngredient + "}";
    }
}
