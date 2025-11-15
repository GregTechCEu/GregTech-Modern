package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.Predicate;

public abstract class NBTPredicate implements Predicate<CompoundTag> {

    // FromJson is handled by the NBTPredicates
    @MustBeInvokedByOverriders
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", getType());
        return object;
    };

    public abstract String getType();

    public abstract boolean test(CompoundTag tag);
}
