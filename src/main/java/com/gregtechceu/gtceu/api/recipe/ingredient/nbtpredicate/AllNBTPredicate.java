package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AllNBTPredicate extends NBTPredicate {

    public static final String OP = "all";

    private final List<NBTPredicate> children;

    public AllNBTPredicate(List<NBTPredicate> children) {
        this.children = children;
    }

    @Override
    public boolean test(CompoundTag tag) {
        for (NBTPredicate child : children) {
            if (!child.test(tag)) {
                return false;
            }
        }
        return true;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("op", OP);
        JsonArray childArray = new JsonArray();
        for (NBTPredicate child : children) {
            childArray.add(child.toJson());
        }
        object.add("children", childArray);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.get("op").getAsString().equals(OP)) {
            throw new IllegalStateException(
                    "Trying to deserialize AllNBTPredicate but was something else: " + json);
        }

        if (!json.has("children")) {
            throw new IllegalStateException("Could not deserialize AllNBTPredicate: " + json);
        }

        List<NBTPredicate> children = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("children")) {
            children.add(NBTPredicateManager.fromJson(element.getAsJsonObject()));
        }

        return new AllNBTPredicate(children);
    }
}
