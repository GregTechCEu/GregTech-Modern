package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.CustomMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.IntersectionMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemTagMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.NBTPredicateItemStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.PartialNBTItemStackMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.StrictNBTItemStackMapIngredient;
import com.gregtechceu.gtceu.utils.IngredientEquality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import java.util.ArrayList;
import java.util.List;

public class VanillaIngredient extends ItemIngredient{

    private final Ingredient inner;
    protected ItemStack[] items;

    public VanillaIngredient(Ingredient ingredient, int count) {
        super(count);
        inner = ingredient;
    }

    public Ingredient getIngredient() {
        return inner;
    }

    @Override
    public int hash() {
        return IngredientEquality.IngredientHashStrategy.INSTANCE.hashCode(inner);
    }

    @Override
    public ItemStack[] getItems() {
        if(items == null) {
            var innerStacks = inner.getItems();
            this.items = new ItemStack[innerStacks.length];
            for (int i = 0; i < items.length; i++) {
                items[i] = innerStacks[i].copyWithCount(count);
            }
        }
        return items;
    }

    @Override
    public List<AbstractMapIngredient> getMapIngredients() {
        if (inner instanceof StrictNBTIngredient strictNBTIngredient) {
            return StrictNBTItemStackMapIngredient.from(strictNBTIngredient);
        }
        if (inner instanceof PartialNBTIngredient partialNBTIngredient) {
            return PartialNBTItemStackMapIngredient.from(partialNBTIngredient);
        }
        if (inner instanceof NBTPredicateIngredient predicateIngredient) {
            return NBTPredicateItemStackMapIngredient.from(predicateIngredient);
        }
        if (inner instanceof IntersectionIngredient intersectionIngredient) {
            return IntersectionMapIngredient.from(intersectionIngredient);
        }

        return CustomMapIngredient.from(inner);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return inner.test(itemStack);
    }

    @Override
    public VanillaIngredient copy() {
        return new VanillaIngredient(inner, count);
    }

    @Override
    public VanillaIngredient copyWithCount(int count) {
        return new VanillaIngredient(inner, count);
    }

    @Override
    public VanillaIngredient copyWithMultiplier(int multiplier) {
        return new VanillaIngredient(inner, count * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(copy(), chance);
    }
}
