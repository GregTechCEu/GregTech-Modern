package com.gregtechceu.gtceu.api.recipe.lookup;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class MapFluidTagIngredient extends AbstractMapIngredient {

    TagKey<Fluid> tag;

    public MapFluidTagIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    protected int hash() {
        return tag.location().hashCode();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return tag == ((MapFluidTagIngredient) obj).tag;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidTagIngredient{" + "tag=" + tag.location() + "}";
    }
}
