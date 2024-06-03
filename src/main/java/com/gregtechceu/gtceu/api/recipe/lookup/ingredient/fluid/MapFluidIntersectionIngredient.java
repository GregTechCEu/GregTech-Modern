package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredientEquality;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MapFluidIntersectionIngredient extends AbstractMapIngredient {

    protected IntersectionFluidIngredient intersectionIngredient;
    @Getter
    protected List<FluidIngredient> ingredients;

    public MapFluidIntersectionIngredient(IntersectionFluidIngredient ingredient) {
        this.intersectionIngredient = ingredient;
        this.ingredients = new ArrayList<>(ingredient.children());
        this.ingredients.sort(FluidIngredientEquality.INGREDIENT_COMPARATOR);
    }

    @Override
    protected int hash() {
        int hash = 31;
        for (FluidIngredient ingredient : ingredients) {
            hash *= 31 * ingredient.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapFluidIntersectionIngredient other = (MapFluidIntersectionIngredient) o;
            if (this.ingredients != null) {
                if (other.ingredients != null) {
                    if (this.ingredients.size() != other.ingredients.size()) return false;
                    for (int i = 0; i < this.ingredients.size(); ++i) {
                        FluidIngredient ingredient1 = this.ingredients.get(i);
                        FluidIngredient ingredient2 = other.ingredients.get(i);
                        if (!ingredient1.equals(ingredient2)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (o instanceof MapFluidStackIngredient stackIngredient) {
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
        return "MapItemIntersectionIngredient{" + "ingredient=" + intersectionIngredient + "}";
    }
}
