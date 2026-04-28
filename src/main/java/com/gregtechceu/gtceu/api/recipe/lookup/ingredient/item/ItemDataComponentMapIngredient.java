package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ExtensionMethod(SizedIngredientExtensions.class)
public class ItemDataComponentMapIngredient extends ItemStackMapIngredient {

    protected DataComponentIngredient componentIngredient;

    public ItemDataComponentMapIngredient(ItemStack s, DataComponentIngredient ingredient, Ingredient vanilla) {
        super(s, vanilla);
        this.componentIngredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentIngredient ingredient) {
        Ingredient vanilla = ingredient.toVanilla();
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : vanilla.items().map(holder -> new ItemStack(holder, 1)).toList()) {
            list.add(new ItemDataComponentMapIngredient(s, ingredient, vanilla));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        Ingredient strict = DataComponentIngredient.of(true, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) strict.getCustomIngredient(), strict));

        Ingredient partial = DataComponentIngredient.of(false, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) partial.getCustomIngredient(), partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemDataComponentMapIngredient other) {
            if (!ItemStack.isSameItem(this.stack, other.stack)) {
                return false;
            }
            if (this.componentIngredient == other.componentIngredient) {
                return true;
            }

            if (this.componentIngredient != null) {
                if (other.componentIngredient != null) {
                    if (this.componentIngredient.componentsExhaustive() !=
                            other.componentIngredient.componentsExhaustive()) {
                        return false;
                    }
                    if (!this.componentIngredient.components().equals(other.componentIngredient.components())) {
                        return false;
                    }

                    if (this.componentIngredient.componentsExhaustive()) {
                        for (ItemStack tStack : this.ingredient.items().map(holder -> new ItemStack(holder, 1))
                                .toList()) {
                            for (ItemStack oStack : other.ingredient.items().map(holder -> new ItemStack(holder, 1))
                                    .toList()) {
                                if (ItemStack.isSameItemSameComponents(tStack, oStack)) return true;
                            }
                        }
                    } else {
                        var otherItems = other.componentIngredient.items().toList();
                        var thisItems = this.componentIngredient.items().toList();
                        boolean thisContains = thisItems.stream().allMatch(otherItems::contains);
                        boolean otherContains = otherItems.stream().allMatch(thisItems::contains);
                        return thisContains && otherContains;
                    }
                } else {
                    return this.componentIngredient.test(other.stack);
                }
            } else {
                return other.componentIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DataComponentItemStackMapIngredient{item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
