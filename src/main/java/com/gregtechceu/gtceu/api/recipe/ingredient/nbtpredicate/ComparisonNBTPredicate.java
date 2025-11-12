package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;

import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicateUtils.getNestedTag;

public class ComparisonNBTPredicate extends NBTPredicate {

    public static String OP = "compare";

    private String key;
    private double value;
    private boolean lessThan;
    private boolean equals;

    public ComparisonNBTPredicate(String key, double value) {
        this(key, value, false, false);
    }

    public ComparisonNBTPredicate(String key, double value, boolean lessThan, boolean equals) {
        this.key = key;
        this.value = value;
        this.lessThan = lessThan;
        this.equals = equals;
    }

    @Override
    public boolean test(CompoundTag tag) {
        Tag toCompare = getNestedTag(tag, key);
        if (toCompare != null) {
            if (toCompare instanceof NumericTag toCompareNum) {
                if (equals) {
                    if (toCompareNum.getAsDouble() == value) {
                        return true;
                    }
                }
                if (lessThan) {
                    return toCompareNum.getAsDouble() < value;
                } else {
                    return toCompareNum.getAsDouble() > value;
                }
            }
        }
        return false;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("op", OP);
        object.addProperty("key", key);
        object.addProperty("value", value);
        object.addProperty("lessThan", lessThan);
        object.addProperty("equals", equals);

        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.get("op").getAsString().equals(OP)) {
            throw new IllegalStateException(
                    "Trying to deserialize ComparisonNBTPredicate but was something else: " + json);
        }
        if (!json.has("key") ||
                !json.has("value") ||
                !json.has("lessThan") ||
                !json.has("equals") ||
                !json.has("op")) {
            throw new IllegalStateException("Could not deserialize ComparisonNBTPredicate: " + json);
        }
        String key = json.get("key").getAsString();
        double value = json.get("value").getAsDouble();
        boolean lessThan = json.get("lessThan").getAsBoolean();
        boolean equals = json.get("equals").getAsBoolean();
        return new ComparisonNBTPredicate(key, value, lessThan, equals);
    }
}
