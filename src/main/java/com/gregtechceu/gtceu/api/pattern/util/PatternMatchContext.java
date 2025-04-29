package com.gregtechceu.gtceu.api.pattern.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Contains a context used for storing temporary data
 * related to current check and shared between all predicates doing it.
 * 
 * @apiNote In practice, it is a {@code HashMap<String, Object>} with some special methods.
 */
public class PatternMatchContext {

    private final Map<String, Object> data = new HashMap<>();

    /**
     * Clears internal map content
     */
    public void reset() {
        this.data.clear();
    }

    /**
     * Associates the specified value with the specified key in the internal map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     * @param key   the map key.
     * @param value the map value.
     */
    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    /**
     * Returns specified map entry as an int.
     * @param key the entry name in the map.
     * @return the entry at {@code key} as an int, or 0 if the entry doesn't exist.
     */
    public int getInt(String key) {
        return data.containsKey(key) ? (int) data.get(key) : 0;
    }

    /**
     * Increments the specified entry by a specific value.
     * @param key   the entry name in the map.
     * @param value the value to increment by.
     */
    public void increment(String key, int value) {
        set(key, getOrDefault(key, 0) + value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code defaultValue} if this map contains no mapping for the key.
     * @param key          the entry name in the map.
     * @param defaultValue the value to return if the entry is empty.
     * @return the entry's content, or the default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * See {@link HashMap#get(Object)} for more details.
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * Returns the value to which the specified key is mapped.
     * If the entry is null, generates a new value with the given {@link Supplier},
     * sets the entry to the newly created value, and returns said value.
     * @param key      the entry name in the map.
     * @param creator  the supplier to use if null.
     * @return the value of the specified entry, or a generated value if null
     */
    public <T> T getOrCreate(String key, Supplier<T> creator) {
        T result = get(key);
        if (result == null) {
            result = creator.get();
            set(key, result);
        }
        return result;
    }

    /**
     * Returns the value to which the specified key is mapped.
     * If the entry is null, sets the entry to {@code initialValue}
     * and returns said value.
     * @param key           the entry name in the map.
     * @param initialValue  the value to use if null.
     * @return the value of the specified entry, or the initial value if null
     */
    public <T> T getOrPut(String key, T initialValue) {
        T result = get(key);
        if (result == null) {
            result = initialValue;
            set(key, result);
        }
        return result;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     * See {@link HashMap#containsKey(Object)} for more details.
     * @param key the key to check.
     * @return whether the key has an associated value.
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    /**
     * Returns a Set view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are reflected in the set, and vice versa.
     * See {@link HashMap#entrySet()} for more details.
     * @return a set view of the mappings contained in this map.
     */
    public Set<Map.Entry<String, Object>> entrySet() {
        return data.entrySet();
    }
}
