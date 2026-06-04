package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CircuitMapIngredient extends AbstractMapIngredient {

    private final int configuration;

    public CircuitMapIngredient(int configuration) {
        this.configuration = configuration;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(int configuration) {
        return List.of(new CircuitMapIngredient(configuration));
    }

    @Override
    protected int hash() {
        return configuration;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && configuration == ((CircuitMapIngredient) obj).configuration;
    }

    @Override
    public String toString() {
        return "CircuitMapIngredient{" + "configuration=" + configuration + "}";
    }
}
