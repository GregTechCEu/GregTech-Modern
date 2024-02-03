package com.gregtechceu.gtceu.api.recipe.lookup;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MapEUIngredient extends AbstractMapIngredient {
    private final long eu;

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            MapEUIngredient o = (MapEUIngredient) obj;
            return this.eu == o.eu;
        }
        return false;
    }

    @Override
    protected int hash() {
        return (int) (eu << 8 | eu >> 31);
    }
}
