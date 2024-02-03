package com.gregtechceu.gtceu.api.recipe.lookup;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MapTagIngredient extends AbstractMapIngredient {

    TagKey<Item> tag;

    public MapTagIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    protected int hash() {
        return tag.location().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return tag == ((MapTagIngredient) obj).tag;
        }
        return false;
    }
}