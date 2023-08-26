package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public record MaterialStackWrapper(Supplier<Material> material, long amount) {
    private static final Map<String, MaterialStackWrapper> PARSE_CACHE = new WeakHashMap<>();

    public static MaterialStackWrapper fromString(CharSequence str) {
        String trimmed = str.toString().trim();
        String copy = trimmed;

        var cached = PARSE_CACHE.get(trimmed);

        if (cached != null) {
            return cached.isEmpty() ? null : cached.copy();
        }

        var count = 1;
        var spaceIndex = copy.indexOf(' ');

        if (spaceIndex >= 2 && copy.indexOf('x') == spaceIndex - 1) {
            count = Integer.parseInt(copy.substring(0, spaceIndex - 1));
            copy = copy.substring(spaceIndex + 1);
        }

        final String copyFinal = copy;
        cached = new MaterialStackWrapper(() -> GTMaterials.get(copyFinal), count);
        PARSE_CACHE.put(trimmed, cached);
        return cached.copy();
    }

    public MaterialStackWrapper copy() {
        return new MaterialStackWrapper(material, amount);
    }

    public boolean isEmpty() {
        return this.material == null || this.amount < 1;
    }

    public MaterialStack toMatStack() {
        return new MaterialStack(this.material.get(), this.amount);
    }
}
