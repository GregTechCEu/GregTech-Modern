package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackNBTIngredient extends MapItemStackIngredient {

    protected PartialNBTIngredient gtRecipeInput = null;

    public MapItemStackNBTIngredient(ItemStack stack, CompoundTag tag) {
        super(stack, tag);
    }

    public MapItemStackNBTIngredient(ItemStack s, PartialNBTIngredient gtRecipeInput) {
        super(s, (CompoundTag) null);
        this.gtRecipeInput = gtRecipeInput;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull PartialNBTIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getItems()) {
            list.add(new MapItemStackNBTIngredient(s, r));
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
        if (obj instanceof MapItemStackNBTIngredient) {
            MapItemStackNBTIngredient other = (MapItemStackNBTIngredient) obj;
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.gtRecipeInput != null) {
                if (other.gtRecipeInput != null) {
                    return gtRecipeInput.equals(other.gtRecipeInput);
                }
            } else if (other.gtRecipeInput != null) {
                return other.gtRecipeInput.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemStackNBTIngredient{" + "item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}