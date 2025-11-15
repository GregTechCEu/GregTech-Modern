package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrueNBTPredicate extends NBTPredicate {

    public static final String TYPE = "true";

    @Override
    public boolean test(CompoundTag tag) {
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TYPE);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("type")) {
            throw new IllegalStateException("Could not deserialize TrueNBTPredicate: " + json);
        }
        if (!json.get("type").getAsString().equals(TYPE)) {
            throw new IllegalStateException("Trying to deserialize TrueNBTPredicate but was something else: " + json);
        }
        return new TrueNBTPredicate();
    }
}
