package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.Range;

import java.util.List;

public final class ChancedItemIngredient extends ItemIngredient implements IChancedIngredient {

    @Getter
    private final ItemIngredient inner;
    private final int chance;
    @Getter
    private final int multiplier;

    protected ChancedItemIngredient(ItemIngredient ingredient, @Range(from = 0, to = 10000) int chance,
                                    int multiplier) {
        super(ingredient.count * multiplier);
        this.inner = ingredient;
        this.chance = chance;
        this.multiplier = multiplier;
    }

    public ChancedItemIngredient(ItemIngredient ingredient, int chance) {
        this(ingredient, chance, 1);
    }

    @Override
    public int hash() {
        return inner.hash();
    }

    @Override
    public ItemStack[] getItems() {
        return inner.getItems();
    }

    @Override
    public ItemStack toStack() {
        int count = inner.getCount() * IChancedIngredient.rollSuccesses(multiplier, chance);
        if (count == 0) return ItemStack.EMPTY;
        else return inner.toStack().copyWithCount(count);
    }

    @Override
    public List<AbstractMapIngredient> getMapIngredients() {
        return inner.getMapIngredients();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return inner.test(itemStack);
    }

    @Override
    public boolean isChanced() {
        return true;
    }

    @Override
    public boolean isRanged() {
        return inner.isRanged();
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public ChancedItemIngredient copy() {
        return new ChancedItemIngredient(inner.copy(), chance, multiplier);
    }

    @Override
    public ItemIngredient copyWithCount(int count) {
        return inner.copyWithCount(count);
    }

    @Override
    public ChancedItemIngredient copyWithMultiplier(int multiplier) {
        return new ChancedItemIngredient(inner.copy(), chance, this.multiplier * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(inner.copy(), chance, multiplier);
    }
}
