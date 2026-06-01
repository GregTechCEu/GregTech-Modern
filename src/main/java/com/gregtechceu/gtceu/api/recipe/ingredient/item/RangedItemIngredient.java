package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.api.GTValues;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import lombok.Getter;

public final class RangedItemIngredient extends ItemIngredient{

    @Getter
    private final ItemIngredient inner;
    @Getter
    private final int minCount;
    private final IntProvider countProvider;

    protected RangedItemIngredient(ItemIngredient ingredient, int minCount, int maxCount) {
        super(maxCount);
        this.inner = ingredient;
        this.minCount = minCount;
        this.countProvider = UniformInt.of(minCount, maxCount);
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
        return getItems()[0].copyWithCount(countProvider.sample(GTValues.RNG));
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return inner.test(itemStack);
    }

    @Override
    public boolean isRanged() {
        return true;
    }

    @Override
    public RangedItemIngredient copy() {
        return new RangedItemIngredient(inner.copy(), minCount, count);
    }

    @Override
    public RangedItemIngredient copyWithMultiplier(int multiplier) {
        return new RangedItemIngredient(inner.copy(), minCount * multiplier, count * multiplier);
    }

    @Override
    public ChancedItemIngredient copyWithChance(int chance) {
        return new ChancedItemIngredient(copy(), chance);
    }
}
