package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public record MaterialStackWrapper(Supplier<Material> material, long amount) {

    public static MaterialStackWrapper EMPTY = new MaterialStackWrapper(() -> GTMaterials.NULL, 0);

    private static final Map<String, MaterialStackWrapper> PARSE_CACHE = new WeakHashMap<>();

    public static MaterialStackWrapper fromString(CharSequence str) {
        String trimmed = str.toString().trim();
        String copy = trimmed;

        var cached = PARSE_CACHE.get(trimmed);

        if (cached != null) {
            return cached.copy();
        }

        var count = 1;
        var spaceIndex = copy.indexOf(' ');

        if (spaceIndex >= 2 && copy.indexOf('x') == spaceIndex - 1) {
            count = Integer.parseInt(copy.substring(0, spaceIndex - 1));
            copy = copy.substring(spaceIndex + 1);
        }

        final String copyFinal = copy;
        Supplier<Material> mat = () -> GTMaterials.get(copyFinal);
        cached = new MaterialStackWrapper(mat, count);
        PARSE_CACHE.put(trimmed, cached);
        return cached.copy();
    }

    public MaterialStackWrapper copy() {
        if (isEmpty()) return EMPTY;
        return new MaterialStackWrapper(material, amount);
    }

    public boolean isEmpty() {
        return this.amount < 1 || this.material == null;
    }

    public MaterialStack toMatStack() {
        if (isEmpty()) return MaterialStack.EMPTY;
        return new MaterialStack(this.material.get(), this.amount);
    }
}
