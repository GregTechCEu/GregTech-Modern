package com.gregtechceu.gtceu.api.recipe.lookup;

import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

@AllArgsConstructor
public class MapBlockStateIngredient extends AbstractMapIngredient {
    private final BlockState state;

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            MapBlockStateIngredient o = (MapBlockStateIngredient) obj;
            return this.state == o.state;
        }
        return false;
    }

    @Override
    protected int hash() {
        int hash = BuiltInRegistries.BLOCK.getId(state.getBlock());
        hash *= 31 * state.getValues().hashCode();
        return hash;
    }
}
