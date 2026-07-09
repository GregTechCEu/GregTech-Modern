package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.ItemMapIngredient;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import lombok.Getter;

import java.util.List;

public final class SimpleItemIngredient extends ItemIngredient {

    @Getter
    private final Item item;

    public SimpleItemIngredient(Item item, int count) {
        super(count);
        this.item = item;
    }

    @Override
    public int hash() {
        return item.hashCode();
    }

    @Override
    public ItemStack[] getItems() {
        return new ItemStack[] { new ItemStack(item, count) };
    }

    @Override
    public List<AbstractMapIngredient> getMapIngredients() {
        return ItemMapIngredient.from(toStack());
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(item);
    }

    @Override
    public SimpleItemIngredient copy() {
        return new SimpleItemIngredient(item, count);
    }

    @Override
    public SimpleItemIngredient copyWithCount(int count) {
        return new SimpleItemIngredient(item, count);
    }

    @Override
    public SimpleItemIngredient copyWithMultiplier(int multiplier) {
        return new SimpleItemIngredient(item, count * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(copy(), chance);
    }

    @Override
    public Ingredient toVanillaIngredient() {
        return Ingredient.of(item);
    }
}
