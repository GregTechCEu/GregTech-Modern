package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;

public class EqualsNBTPredicate<T> extends NBTPredicate {

    public static String OP = "equals";

    private String key;
    private String value;
    private int type;

    public EqualsNBTPredicate(String key, String value, int type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean test(CompoundTag tag) {
        if (!tag.contains(key, type)) {
            return false;
        }
        switch (type) {
            case Tag.TAG_STRING:
                return tag.getString(key).equals(value);
            // TODO: write all other cases or figure out something better
            default:
                return true;
        }
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("op", OP);
        object.addProperty("key", key);
        object.addProperty("value", value);
        object.addProperty("type", type);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("key") || !json.has("value") || !json.has("type") || !json.has("op")) {
            throw new IllegalStateException("Could not deserialize EqualsNBTPredicate: " + json);
        }
        if (!json.get("op").getAsString().equals(OP)) {
            throw new IllegalStateException("Trying to deserialize EqualsNBTPredicate but was something else: " + json);
        }
        String key = json.get("key").getAsString();
        String value = json.get("value").getAsString();
        int type = json.get("type").getAsInt();

        return new EqualsNBTPredicate(key, value, type);
    }
}
