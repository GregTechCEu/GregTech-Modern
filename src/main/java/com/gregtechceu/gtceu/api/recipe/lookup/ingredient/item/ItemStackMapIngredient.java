package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.ItemValueAccessor;
import com.gregtechceu.gtceu.utils.IngredientEquality;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

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

    @NotNull
    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        for (Ingredient.Value value : ((IngredientAccessor) ingredient).getValues()) {
            if (value instanceof ItemValueAccessor itemValue) {
                ingredients.add(new ItemStackMapIngredient(itemValue.getItem(), ingredient));
            }
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new ItemStackMapIngredient(stack));
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
        return stack.getItem().hashCode() * 31;
    }

    @Override
    public String toString() {
        return "ItemStackMapIngredient{" + "item=" + stack + "}";
    }
}
