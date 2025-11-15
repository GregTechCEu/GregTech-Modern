package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

public abstract class NBTPredicate {

    // FromJson is handled by the NBTPredicates
    public abstract JsonObject toJson();

    public abstract boolean test(CompoundTag tag);
}
