package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.IntersectionIngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.ItemValueAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import com.gregtechceu.gtceu.utils.IngredientEquality;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;

import java.util.ArrayList;
import java.util.List;

public class MapIntersectionIngredient extends AbstractMapIngredient {

    protected IntersectionIngredient intersectionIngredient;
    protected List<Ingredient> ingredients;

    public MapIntersectionIngredient(IntersectionIngredient ingredient) {
        this.intersectionIngredient = ingredient;
        this.ingredients = new ArrayList<>(((IntersectionIngredientAccessor) ingredient).getChildren());
        this.ingredients.sort(IngredientEquality.INGREDIENT_COMPARATOR);
    }

    @Override
    protected int hash() {
        int hash = 31;
        for (Ingredient ingredient : ingredients) {
            for (Ingredient.Value value : ((IngredientAccessor) ingredient).getValues()) {
                if (value instanceof Ingredient.TagValue tagValue) {
                    hash *= 31 * ((TagValueAccessor) tagValue).getTag().location().hashCode();
                } else {
                    hash *= 31 * ((ItemValueAccessor) value).getItem().getItem().hashCode();
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
                        if (!IngredientEquality.ingredientEquals(ingredient1, ingredient2)) {
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
        return "MapIntersectionIngredient{" + "ingredient=" + intersectionIngredient + "}";
    }
}
