package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import net.minecraft.core.Holder;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public record LazyMaterialStack(Supplier<Material> material, long amount) {

    public static LazyMaterialStack EMPTY = new LazyMaterialStack(GTMaterials.NULL, 0);

    private static final Map<String, LazyMaterialStack> PARSE_CACHE = new WeakHashMap<>();

    public LazyMaterialStack {
        material = GTMemoizer.memoize(material);
    }

    public LazyMaterialStack copy() {
        if (isEmpty()) return EMPTY;
        return new LazyMaterialStack(material, amount);
    }

    public boolean isEmpty() {
        return this.amount < 1 || this.material == null;
    }

    public MaterialStack resolve() {
        if (isEmpty()) return MaterialStack.EMPTY;
        return new MaterialStack(this.material.get(), this.amount);
    }

    public static LazyMaterialStack fromString(CharSequence str) {
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
        Holder<Material> mat = GTMaterials.get(GTCEu.id(copyFinal));
        cached = new LazyMaterialStack(mat, count);
        PARSE_CACHE.put(trimmed, cached);
        return cached.copy();
    }
}
