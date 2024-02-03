package com.gregtechceu.gtceu.api.recipe.lookup;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapItemStackIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected CompoundTag tag;
    protected Ingredient ingredient = null;

    public MapItemStackIngredient(ItemStack stack, CompoundTag tag) {
        this.stack = stack;
        this.tag = tag;
    }

    public MapItemStackIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.tag = stack.getTag();
        this.ingredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull Ingredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getItems()) {
            list.add(new MapItemStackIngredient(s, r));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            if (this.stack.getItem() != other.stack.getItem()) {
                return false;
            }
            if (this.ingredient != null) {
                if (other.ingredient != null) {
                    return ingredient.equals(other.ingredient);
                }
            } else if (other.ingredient != null) {
                return other.ingredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * (this.tag != null ? this.tag.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "} {tag=" + tag + "}";
    }
}