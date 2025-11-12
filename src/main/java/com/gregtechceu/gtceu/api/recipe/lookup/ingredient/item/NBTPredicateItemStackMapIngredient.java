package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NBTPredicateItemStackMapIngredient extends ItemStackMapIngredient {

    protected NBTPredicateIngredient nbtIngredient;

    public NBTPredicateItemStackMapIngredient(ItemStack stack, NBTPredicateIngredient nbtIngredient) {
        super(stack, nbtIngredient);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull NBTPredicateIngredient ingredient) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : ingredient.getItems()) {
            list.add(new NBTPredicateItemStackMapIngredient(s, ingredient));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        if (stack.getShareTag() != null) {
            return Collections.singletonList(new NBTPredicateItemStackMapIngredient(stack,
                    NBTPredicateIngredient.of(stack)));
        }
        return Collections.emptyList();
    }

    @Override
    protected int hash() {
        return ItemStackHashStrategy.comparingItem().hashCode(stack) * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NBTPredicateItemStackMapIngredient other) {
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
                } else {
                    this.nbtIngredient.test(other.stack);
                }
            } else if (other.nbtIngredient != null) {
                return other.nbtIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NBTPredicateItemStackMapIngredient{" + "item=" + stack + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
