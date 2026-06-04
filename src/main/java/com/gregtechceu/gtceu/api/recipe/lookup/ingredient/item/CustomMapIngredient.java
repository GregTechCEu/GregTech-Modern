package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomMapIngredient extends AbstractMapIngredient {

    protected Item item;
    protected Ingredient ingredient = null;

    public CustomMapIngredient(ItemStack stack) {
        this.item = stack.getItem();
    }

    public CustomMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.item = stack.getItem();
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ArrayList<>();
        ItemStack[] stacks = ingredient.getItems();
        for (ItemStack stack : stacks) {
            ingredients.add(new CustomMapIngredient(stack, ingredient));
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new CustomMapIngredient(stack));
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            CustomMapIngredient other = (CustomMapIngredient) o;
            if (this.item != other.item) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    for (ItemStack stack : other.ingredient.getItems()) {
                        if (!this.ingredient.test(stack)) return false;
                    }
                    for (ItemStack stack : this.ingredient.getItems()) {
                        if (!other.ingredient.test(stack)) return false;
                    }
                    return true;
                } else {
                    return this.ingredient.test(other.item.getDefaultInstance());
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.item.getDefaultInstance());
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        return item.hashCode() * 31;
    }

    @Override
    public String toString() {
        return "CustomMapIngredient{" +
                "item=" + item +
                "ingredient=" + ingredient +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
