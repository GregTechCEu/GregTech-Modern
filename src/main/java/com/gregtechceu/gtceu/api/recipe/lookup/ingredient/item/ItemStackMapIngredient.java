package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.utils.IngredientEquality;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemStackMapIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient = null;

    public ItemStackMapIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStackMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            ItemStackMapIngredient other = (ItemStackMapIngredient) o;
            if (!ItemStack.isSameItem(this.stack, other.stack)) {
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
        return "MapItemStackIngredient{" + "item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }
}
