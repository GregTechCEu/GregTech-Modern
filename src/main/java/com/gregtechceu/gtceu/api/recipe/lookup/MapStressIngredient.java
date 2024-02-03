package com.gregtechceu.gtceu.api.recipe.lookup;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MapStressIngredient extends AbstractMapIngredient {
    private final float stress;

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            MapStressIngredient o = (MapStressIngredient) obj;
            return this.stress == o.stress;
        }
        return false;
    }

    @Override
    protected int hash() {
        return Float.hashCode(stress);
    }
}
