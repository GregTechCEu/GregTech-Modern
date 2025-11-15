package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

import java.util.function.Predicate;

public abstract class NBTPredicate implements Predicate<CompoundTag> {

    // FromJson is handled by the NBTPredicates
    public abstract JsonObject toJson();

    public abstract boolean test(CompoundTag tag);
}
