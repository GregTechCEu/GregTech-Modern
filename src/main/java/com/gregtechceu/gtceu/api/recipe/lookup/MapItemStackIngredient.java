package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.utils.IngredientEquality;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient = null;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public MapItemStackIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    return IngredientEquality.ingredientEquals(this.ingredient, other.ingredient);
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        return stack.getItem().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + Registry.ITEM.getKey(stack.getItem()) + "}";
    }
}