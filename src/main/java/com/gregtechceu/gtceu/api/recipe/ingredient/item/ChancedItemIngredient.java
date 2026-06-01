package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import net.minecraft.world.item.ItemStack;
import lombok.Getter;
import org.jetbrains.annotations.Range;

public final class ChancedItemIngredient extends ItemIngredient implements IChancedIngredient{

    @Getter
    private final ItemIngredient inner;
    private final int chance;
    @Getter
    private final int multiplier;

    protected ChancedItemIngredient(ItemIngredient ingredient, @Range(from = 0, to = 10000) int chance, int multiplier) {
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
        return getItems()[0].copyWithCount(inner.getCount() * IChancedIngredient.rollSuccesses(multiplier, chance));
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
    public ChancedItemIngredient copyWithMultiplier(int multiplier) {
        return new ChancedItemIngredient(inner.copy(), chance, this.multiplier * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(inner.copy(), chance, multiplier);
    }
}
