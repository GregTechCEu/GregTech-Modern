package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MapItemTagIngredient extends AbstractMapIngredient {

    TagKey<Item> tag;

    public MapItemTagIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    protected int hash() {
        return tag.location().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return tag == ((MapItemTagIngredient) obj).tag;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemTagIngredient{" + "tag=" + tag.location() + "}";
    }
}
