package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ItemMapIngredient extends AbstractMapIngredient {

    protected final Item item;

    public ItemMapIngredient(Item item) {
        this.item = item;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new ItemMapIngredient(stack.getItem()));
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            ItemMapIngredient other = (ItemMapIngredient) o;
            return this.item == other.item;
        }
        return false;
    }

    @Override
    protected int hash() {
        return item.hashCode();
    }

    @Override
    public String toString() {
        return "ItemMapIngredient{" + "item=" + item + "}";
    }
}
