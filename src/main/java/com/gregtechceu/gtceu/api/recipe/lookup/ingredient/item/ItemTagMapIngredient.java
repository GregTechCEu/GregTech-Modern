package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemTagMapIngredient extends AbstractMapIngredient {

    protected TagKey<Item> tag;

    public ItemTagMapIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(Ingredient ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        for (Ingredient.Value value : ((IngredientAccessor) ingredient).getValues()) {
            if (value instanceof TagValueAccessor tagValue) {
                ingredients.add(new ItemTagMapIngredient(tagValue.getTag()));
            }
        }
        return ingredients;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>();
        stack.getTags().forEach(tag -> ingredients.add(new ItemTagMapIngredient(tag)));
        return ingredients;
    }

    @Override
    protected int hash() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return tag == ((ItemTagMapIngredient) obj).tag;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ItemTagMapIngredient{" + "tag=" + tag.location() + "}";
    }
}
