package com.lowdragmc.lowdraglib.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class Builder<T, B extends Builder<T, B>> {

    protected final List<String[]> aisles = new ArrayList<>();
    protected final Map<Character, T> symbolMap = new HashMap<>();

    public B aisle(String... aisle) {
        aisles.add(aisle);
        return (B) this;
    }

    public B where(char symbol, T value) {
        symbolMap.put(symbol, value);
        return (B) this;
    }

    public T[][][] bakeArray(Class<T> componentType, T fallback) {
        int xSize = aisles.size();
        int ySize = aisles.stream().mapToInt(a -> a.length).max().orElse(0);
        int zSize = aisles.stream()
                .flatMap(a -> List.of(a).stream())
                .mapToInt(String::length)
                .max()
                .orElse(0);
        T[][][] result = (T[][][]) Array.newInstance(componentType, xSize, ySize, zSize);
        for (int x = 0; x < xSize; x++) {
            String[] aisle = aisles.get(x);
            for (int y = 0; y < ySize; y++) {
                String row = y < aisle.length ? aisle[y] : "";
                for (int z = 0; z < zSize; z++) {
                    char symbol = z < row.length() ? row.charAt(z) : ' ';
                    result[x][y][z] = symbolMap.getOrDefault(symbol, fallback);
                }
            }
        }
        return result;
    }

    public B shallowCopy() {
        try {
            B copy = (B) getClass().getDeclaredConstructor().newInstance();
            copy.aisles.addAll(aisles);
            copy.symbolMap.putAll(symbolMap);
            return copy;
        } catch (ReflectiveOperationException ignored) {
            return (B) this;
        }
    }
}
