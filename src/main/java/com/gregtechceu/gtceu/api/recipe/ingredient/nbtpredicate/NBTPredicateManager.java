package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NBTPredicateManager {

    public static Map<String, Function<JsonObject, NBTPredicate>> predicates = new HashMap<>();

    static {
        predicates.put(EqualsNBTPredicate.OP, EqualsNBTPredicate::fromJson);
    };

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("op")) {
            throw new IllegalStateException("Can't deserialize JSON without operation key: " + json);
        }
        String op = json.get("op").getAsString();
        return predicates.get(op).apply(json);
    }
}
