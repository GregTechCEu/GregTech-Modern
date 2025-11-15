package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;

import static com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicateUtils.getNestedTag;

public class ComparisonNBTPredicate extends NBTPredicate {

    public static final String TYPE = "compare";

    @Override
    public String getType() {
        return TYPE;
    }

    private final String key;
    private final double value;
    private final boolean lessThan;
    private final boolean equals;

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

    @Override
    public JsonObject toJson() {
        JsonObject object = super.toJson();
        object.addProperty("key", key);
        object.addProperty("value", value);
        object.addProperty("lessThan", lessThan);
        object.addProperty("equals", equals);

        return object;
    }

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("key") ||
                !json.has("value") ||
                !json.has("lessThan") ||
                !json.has("equals") ||
                !json.has("type")) {
            throw new IllegalStateException("Could not deserialize ComparisonNBTPredicate: " + json);
        }
        if (!json.get("type").getAsString().equals(TYPE)) {
            throw new IllegalStateException(
                    "Trying to deserialize ComparisonNBTPredicate but was something else: " + json);
        }
        String key = json.get("key").getAsString();
        double value = json.get("value").getAsDouble();
        boolean lessThan = json.get("lessThan").getAsBoolean();
        boolean equals = json.get("equals").getAsBoolean();
        return new ComparisonNBTPredicate(key, value, lessThan, equals);
    }

    @Override
    public String toString() {
        return "ComparisonNBTPredicate{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", lessThan=" + lessThan +
                ", equals=" + equals +
                '}';
    }
}
