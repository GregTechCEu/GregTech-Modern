package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackWeakDataComponentIngredient extends MapItemStackIngredient {

    Ingredient nbtIngredient;

    public MapItemStackWeakDataComponentIngredient(ItemStack stack, Ingredient nbtIngredient) {
        super(stack, nbtIngredient);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull Ingredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getItems()) {
            list.add(new MapItemStackWeakDataComponentIngredient(s, r));
        }
        return list;
    }

    @Override
    protected int hash() {
        return stack.getItem().hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapItemStackWeakDataComponentIngredient other) {
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.nbtIngredient != null) {
                if (other.nbtIngredient != null) {
                    if (this.nbtIngredient.getItems().length != other.nbtIngredient.getItems().length)
                        return false;
                    for (ItemStack stack : this.nbtIngredient.getItems()) {
                        if (!other.nbtIngredient.test(stack)) {
                            return false;
                        }
                    }
                    return true;
                }
            } else if (other.nbtIngredient != null) {
                return other.nbtIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemStackWeakDataComponentIngredient{" + "item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
