package com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.List;

public class NBTPredicates {

    public static NBTPredicate eq_int(String key, int value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, int value) {
        return new EqualsNBTPredicate(key, IntTag.valueOf(value));
    }

    public static NBTPredicate eq_float(String key, float value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, float value) {
        return new EqualsNBTPredicate(key, FloatTag.valueOf(value));
    }

    public static NBTPredicate eq_double(String key, double value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, double value) {
        return new EqualsNBTPredicate(key, DoubleTag.valueOf(value));
    }

    // Note: Bools are handled as bytes
    public static NBTPredicate eq_bool(String key, boolean value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, boolean value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value));
    }

    public static NBTPredicate eq_byte(String key, byte value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, byte value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value));
    }

    public static NBTPredicate eq_tag(String key, Tag value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, Tag value) {
        return new EqualsNBTPredicate(key, value);
    }

    public static NBTPredicate eq_string(String key, String value) {
        return eq(key, value);
    }

    @HideFromJS
    public static NBTPredicate eq(String key, String value) {
        return new EqualsNBTPredicate(key, StringTag.valueOf(value));
    }

    public static NBTPredicate neq_int(String key, int value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, int value) {
        return new EqualsNBTPredicate(key, IntTag.valueOf(value), true);
    }

    public static NBTPredicate neq_float(String key, float value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, float value) {
        return new EqualsNBTPredicate(key, FloatTag.valueOf(value), true);
    }

    public static NBTPredicate neq_double(String key, double value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, double value) {
        return new EqualsNBTPredicate(key, DoubleTag.valueOf(value), true);
    }

    // Note: Bools are handled as bytes
    public static NBTPredicate neq_bool(String key, boolean value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, boolean value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value), true);
    }

    public static NBTPredicate neq_byte(String key, byte value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, byte value) {
        return new EqualsNBTPredicate(key, ByteTag.valueOf(value), true);
    }

    public static NBTPredicate neq_tag(String key, Tag value) {
        return neq(key, value);
    }

    @HideFromJS
    public static NBTPredicate neq(String key, Tag value) {
        return new EqualsNBTPredicate(key, value, true);
    }

    public static NBTPredicate neq_string(String key, String value) {
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
}
