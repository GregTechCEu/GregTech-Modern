package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackDataComponentIngredient extends MapItemStackIngredient {

    protected Ingredient nbtIngredient;

    public MapItemStackDataComponentIngredient(ItemStack s, Ingredient nbtIngredient) {
        super(s);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull Ingredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getItems()) {
            list.add(new MapItemStackDataComponentIngredient(s, r));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull IntCircuitIngredient r) {
        Ingredient nbtIngredient = DataComponentIngredient.of(true, IntCircuitBehaviour.stack(r.getConfiguration()));
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        r.getItems().forEach(s -> list.add(new MapItemStackDataComponentIngredient(s, nbtIngredient)));
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapItemStackDataComponentIngredient other) {
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.nbtIngredient != null) {
                if (other.nbtIngredient != null) {
                    return ItemStack.isSameItemSameComponents(nbtIngredient.getItems()[0],
                            other.nbtIngredient.getItems()[0]);
                }
            } else if (other.nbtIngredient != null) {
                return other.nbtIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemStackDataComponentIngredient{" + "item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
