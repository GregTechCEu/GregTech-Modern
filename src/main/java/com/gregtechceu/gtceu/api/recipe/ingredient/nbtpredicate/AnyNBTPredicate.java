package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnyNBTPredicate extends NBTPredicate {

    public static final String TYPE = "any";

    @Override
    public String getType() {
        return TYPE;
    }

    private final List<NBTPredicate> children;

    public AnyNBTPredicate(List<NBTPredicate> children) {
        this.children = children;
    }

    @Override
    public boolean test(CompoundTag tag) {
        for (NBTPredicate child : children) {
            if (child.test(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();
        JsonArray childArray = new JsonArray();
        for (NBTPredicate child : children) {
            childArray.add(child.toJson());
        }
        object.add("children", childArray);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("children") || !json.has("type")) {
            throw new IllegalStateException("Could not deserialize AnyNBTPredicate: " + json);
        }
        if (!json.get("type").getAsString().equals(TYPE)) {
            throw new IllegalStateException(
                    "Trying to deserialize AnyNBTPredicate but was something else: " + json);
        }

        List<NBTPredicate> children = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("children")) {
            children.add(NBTPredicates.fromJson(element.getAsJsonObject()));
        }

        return new AnyNBTPredicate(children);
    }

    @Override
    public String toString() {
        return "AnyNBTPredicate{" +
                "children=[" + children.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "]}";
    }
}
