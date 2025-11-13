package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonObject;

public class NotNBTPredicate extends NBTPredicate {

    public static final String OP = "not";

    private final NBTPredicate child;

    public NotNBTPredicate(NBTPredicate child) {
        this.child = child;
    }

    @Override
    public boolean test(CompoundTag tag) {
        return !child.test(tag);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("op", OP);
        object.add("child", child.toJson());
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.get("op").getAsString().equals(OP)) {
            throw new IllegalStateException(
                    "Trying to deserialize NotNBTPredicate but was something else: " + json);
        }

        if (!json.has("child")) {
            throw new IllegalStateException("Could not deserialize NotNBTPredicate: " + json);
        }

        NBTPredicate child = NBTPredicateManager.fromJson(json.get("child").getAsJsonObject());
        return new NotNBTPredicate(child);
    }
}
