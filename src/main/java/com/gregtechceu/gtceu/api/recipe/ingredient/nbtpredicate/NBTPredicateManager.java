package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NBTPredicateManager {

    public static Map<String, Function<JsonObject, NBTPredicate>> predicates = new HashMap<>();

    static {
        predicates.put(TrueNBTPredicate.OP, TrueNBTPredicate::fromJson);
        predicates.put(EqualsNBTPredicate.OP, EqualsNBTPredicate::fromJson);
        predicates.put(ComparisonNBTPredicate.OP, ComparisonNBTPredicate::fromJson);
        predicates.put(AllNBTPredicate.OP, AllNBTPredicate::fromJson);
        predicates.put(AnyNBTPredicate.OP, AnyNBTPredicate::fromJson);
        predicates.put(NotNBTPredicate.OP, NotNBTPredicate::fromJson);
    };

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("op")) {
            throw new IllegalStateException("Can't deserialize JSON without operation key: " + json);
        }
        String op = json.get("op").getAsString();
        return predicates.get(op).apply(json);
    }
}
