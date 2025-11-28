package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class NBTPredicates {

    private NBTPredicates() {};

    public static NBTPredicate eqInt(String key, int value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, int value) {
        return new EqualsNBTPredicate(key, IntTag.valueOf(value));
    }

    public static NBTPredicate eqFloat(String key, float value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, float value) {
        return new EqualsNBTPredicate(key, FloatTag.valueOf(value));
    }

    public static NBTPredicate eqDouble(String key, double value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, double value) {
        return new EqualsNBTPredicate(key, DoubleTag.valueOf(value));
    }

    // Note: Bools are handled as bytes
    public static NBTPredicate eqBool(String key, boolean value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, boolean value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value));
    }

    public static NBTPredicate eqByte(String key, byte value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, byte value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value));
    }

    public static NBTPredicate eqTag(String key, Tag value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, Tag value) {
        return new EqualsNBTPredicate(key, value);
    }

    public static NBTPredicate eqString(String key, String value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, String value) {
        return new EqualsNBTPredicate(key, StringTag.valueOf(value));
    }

    public static NBTPredicate neqInt(String key, int value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, int value) {
        return new EqualsNBTPredicate(key, IntTag.valueOf(value), true);
    }

    public static NBTPredicate neqFloat(String key, float value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, float value) {
        return new EqualsNBTPredicate(key, FloatTag.valueOf(value), true);
    }

    public static NBTPredicate neqDouble(String key, double value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, double value) {
        return new EqualsNBTPredicate(key, DoubleTag.valueOf(value), true);
    }

    // Note: Bools are handled as bytes
    public static NBTPredicate neqBool(String key, boolean value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, boolean value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value), true);
    }

    public static NBTPredicate neqByte(String key, byte value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, byte value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value), true);
    }

    public static NBTPredicate neqTag(String key, Tag value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, Tag value) {
        return new EqualsNBTPredicate(key, value, true);
    }

    public static NBTPredicate neqString(String key, String value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, String value) {
        return new EqualsNBTPredicate(key, StringTag.valueOf(value), true);
    }

    public static NBTPredicate lte(String key, double value) {
        return new ComparisonNBTPredicate(key, value, true, true);
    }

    public static NBTPredicate lt(String key, double value) {
        return new ComparisonNBTPredicate(key, value, true, false);
    }

    public static NBTPredicate gte(String key, double value) {
        return new ComparisonNBTPredicate(key, value, false, true);
    }

    public static NBTPredicate gt(String key, double value) {
        return new ComparisonNBTPredicate(key, value, false, false);
    }

    public static NBTPredicate any(NBTPredicate... predicates) {
        return new AnyNBTPredicate(List.of(predicates));
    }

    @HideFromJS
    public static NBTPredicate any(List<NBTPredicate> predicates) {
        return new AnyNBTPredicate(predicates);
    }

    public static NBTPredicate all(NBTPredicate... predicates) {
        return new AllNBTPredicate(List.of(predicates));
    }

    @HideFromJS
    public static NBTPredicate all(List<NBTPredicate> predicates) {
        return new AllNBTPredicate(predicates);
    }

    public static NBTPredicate not(NBTPredicate predicate) {
        return new NotNBTPredicate(predicate);
    }

    public static final Map<String, Function<JsonObject, NBTPredicate>> predicateCodecs = new Object2ObjectOpenHashMap<>();

    static {
        predicateCodecs.put(TrueNBTPredicate.TYPE, TrueNBTPredicate::fromJson);
        predicateCodecs.put(EqualsNBTPredicate.TYPE, EqualsNBTPredicate::fromJson);
        predicateCodecs.put(ComparisonNBTPredicate.TYPE, ComparisonNBTPredicate::fromJson);
        predicateCodecs.put(AllNBTPredicate.TYPE, AllNBTPredicate::fromJson);
        predicateCodecs.put(AnyNBTPredicate.TYPE, AnyNBTPredicate::fromJson);
        predicateCodecs.put(NotNBTPredicate.TYPE, NotNBTPredicate::fromJson);
    };

    public static NBTPredicate fromJson(JsonObject json) {
        if (!json.has("type")) {
            throw new IllegalStateException("Can't deserialize JSON without operation key: " + json);
        }
        String op = json.get("type").getAsString();
        return predicateCodecs.get(op).apply(json);
    }
}
