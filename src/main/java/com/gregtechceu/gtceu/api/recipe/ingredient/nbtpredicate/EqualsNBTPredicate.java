package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;

import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicateUtils.getNestedTag;

public class EqualsNBTPredicate extends NBTPredicate {

    public static final String TYPE = "equals";

    @Override
    public String getType() {
        return TYPE;
    }

    private final String key;
    private final Tag value;
    private final boolean inverted;

    public EqualsNBTPredicate(String key, Tag value) {
        this(key, value, false);
    }

    public EqualsNBTPredicate(String key, Tag value, boolean inverted) {
        this.key = key;
        this.value = value;
        this.inverted = inverted;
    }

    @Override
    public boolean test(CompoundTag tag) {
        Tag toCompare = getNestedTag(tag, key);
        if (toCompare == null) {
            return false;
        } else {
            // Mixed numeric types (e.g., int vs. double)
            if (toCompare instanceof NumericTag toCompareNum &&
                    value instanceof NumericTag valueNum) {
                return inverted ^ (toCompareNum.getAsDouble() == valueNum.getAsDouble());
            }
            return inverted ^ toCompare.equals(value);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();
        object.addProperty("key", key);
        object.add("value", NBTPredicateUtils.toJson(value));
        object.addProperty("inverted", inverted);
        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("key") || !json.has("value") || !json.has("type")) {
            throw new IllegalStateException("Could not deserialize EqualsNBTPredicate: " + json);
        }
        if (!json.get("type").getAsString().equals(TYPE)) {
            throw new IllegalStateException("Trying to deserialize EqualsNBTPredicate but was something else: " + json);
        }
        String key = json.get("key").getAsString();
        Tag value = NBTPredicateUtils.fromJson(json.get("value"));
        boolean inverted = false;
        if (json.has("inverted")) {
            inverted = json.get("inverted").getAsBoolean();
        }
        return new EqualsNBTPredicate(key, value, inverted);
    }

    @Override
    public String toString() {
        return "EqualsNBTPredicate{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", inverted=" + inverted +
                '}';
    }
}
