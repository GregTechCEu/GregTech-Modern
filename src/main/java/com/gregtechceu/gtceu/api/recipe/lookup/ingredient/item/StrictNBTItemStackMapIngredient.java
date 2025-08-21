package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.core.mixins.forge.StrictNBTIngredientAccessor;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class StrictNBTItemStackMapIngredient extends ItemStackMapIngredient {

    protected StrictNBTIngredient nbtIngredient;

    public StrictNBTItemStackMapIngredient(ItemStack s, StrictNBTIngredient nbtIngredient) {
        super(s);
        this.nbtIngredient = nbtIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull StrictNBTIngredient ingredient) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : ingredient.getItems()) {
            list.add(new StrictNBTItemStackMapIngredient(s, ingredient));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        if (stack.hasTag()) {
            return Collections.singletonList(new StrictNBTItemStackMapIngredient(stack, StrictNBTIngredient.of(stack)));
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
        if (obj instanceof StrictNBTItemStackMapIngredient other) {
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.nbtIngredient != null) {
                if (other.nbtIngredient != null) {
                    return ItemStack.isSameItemSameTags(((StrictNBTIngredientAccessor) nbtIngredient).getStack(),
                            ((StrictNBTIngredientAccessor) other.nbtIngredient).getStack());
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
        return "StrictNBTItemStackMapIngredient{" + "item=" + stack + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
