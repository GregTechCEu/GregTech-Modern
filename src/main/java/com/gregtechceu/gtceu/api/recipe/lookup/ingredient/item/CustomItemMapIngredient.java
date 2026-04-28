package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomItemMapIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient = null;

    public CustomItemMapIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public CustomItemMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        for (ItemStack stack : ingredient.items().map(holder -> new ItemStack(holder, 1)).toList()) {
            ingredients.add(new CustomItemMapIngredient(stack, ingredient));
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new CustomItemMapIngredient(stack));
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            CustomItemMapIngredient other = (CustomItemMapIngredient) o;
            if (!ItemStack.isSameItem(this.stack, other.stack)) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    for (ItemStack stack : other.ingredient.items().map(holder -> new ItemStack(holder, 1)).toList()) {
                        if (!this.ingredient.test(stack)) return false;
                    }
                    for (ItemStack stack : this.ingredient.items().map(holder -> new ItemStack(holder, 1)).toList()) {
                        if (!other.ingredient.test(stack)) return false;
                    }
                    return true;
                } else {
                    return this.ingredient.test(other.stack);
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        return stack.getItem().builtInRegistryHolder().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "CustomMapIngredient{" +
                "item=" + stack +
                "ingredient=" + ingredient +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
