package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.NotImplementedException;

public class NBTPredicate {

    // FromJson is handled by the NBTPredicateManager
    public JsonObject toJson() {
        throw new NotImplementedException();
    }

    public boolean test(CompoundTag tag) {
        throw new NotImplementedException();
    };
}
