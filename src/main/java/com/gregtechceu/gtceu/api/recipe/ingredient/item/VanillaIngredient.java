package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.utils.IngredientEquality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import lombok.Getter;

public class VanillaIngredient extends ItemIngredient{

    @Getter
    private final Ingredient inner;
    protected ItemStack[] items;

    public VanillaIngredient(Ingredient ingredient, int count) {
        super(count);
        inner = ingredient;
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
    public ItemStack toStack() {
        return getItems()[0];
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
    public VanillaIngredient copyWithMultiplier(int multiplier) {
        return new VanillaIngredient(inner, count * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(copy(), chance);
    }
}
