package com.gregtechceu.gtceu.api.pattern.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Contains an context used for storing temporary data
 * related to current check and shared between all predicates doing it
 */
public class PatternMatchContext {

    private final Map<String, Object> data = new HashMap<>();

    public void reset() {
        this.data.clear();
    }

    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    public int getInt(String key) {
        return data.containsKey(key) ? (int) data.get(key) : 0;
    }

    public void increment(String key, int value) {
        set(key, getOrDefault(key, 0) + value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T getOrCreate(String key, Supplier<T> creator) {
        T result = get(key);
        if (result == null) {
            result = creator.get();
            set(key, result);
        }
        return result;
    }

    public <T> T getOrPut(String key, T initialValue) {
        T result = get(key);
        if (result == null) {
            result = initialValue;
            set(key, result);
        }
        return result;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return data.entrySet();
    }
}
