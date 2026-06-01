package com.gregtechceu.gtceu.api.recipe.ingredient.item;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.world.item.ItemStack;

import lombok.Getter;

public final class IntCircuitIngredient extends ItemIngredient {

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 32;

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static IntCircuitIngredient of(int configuration) {
        if (configuration < CIRCUIT_MIN || configuration > CIRCUIT_MAX) {
            throw new IndexOutOfBoundsException("Circuit configuration " + configuration + " is out of range");
        }
        IntCircuitIngredient ingredient = INGREDIENTS[configuration];
        if (ingredient == null) {
            INGREDIENTS[configuration] = ingredient = new IntCircuitIngredient(configuration);
        }
        return ingredient;
    }

    @Getter
    private final int configuration;
    private ItemStack[] items;

    private IntCircuitIngredient(int configuration) {
        super(1);
        this.configuration = configuration;
    }

    @Override
    public int hash() {
        return configuration;
    }

    @Override
    public ItemStack[] getItems() {
        if (items == null) {
            items = new ItemStack[] { IntCircuitBehaviour.stack(configuration) };
        }
        return items;
    }

    @Override
    public ItemStack toStack() {
        return IntCircuitBehaviour.stack(configuration);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(GTItems.PROGRAMMED_CIRCUIT.get()) &&
                IntCircuitBehaviour.getCircuitConfiguration(itemStack) == configuration;
    }

    @Override
    public boolean isChanced() {
        return true;
    }

    @Override
    public int getChance() {
        return 0;
    }

    @Override
    public IntCircuitIngredient copy() {
        return of(configuration);
    }

    @Override
    public IntCircuitIngredient copyWithMultiplier(int multiplier) {
        return copy();
    }

    @Override
    public IntCircuitIngredient copyWithChance(int chance) {
        return copy();
    }
}
