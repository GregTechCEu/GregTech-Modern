package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public record DeferredMaterialStack(@Nullable Supplier<@NotNull Material> material, long amount) {

    public static DeferredMaterialStack EMPTY = new DeferredMaterialStack(() -> GTMaterials.NULL, 0);

    private static final Map<String, DeferredMaterialStack> PARSE_CACHE = new WeakHashMap<>();

    public static DeferredMaterialStack fromString(CharSequence str) {
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
        final ResourceKey<Material> matKey = ResourceKey.create(GTRegistries.Keys.MATERIAL, GTCEu.id(copyFinal));
        Supplier<Material> mat = () -> GTRegistries.MATERIALS.getOrThrow(matKey);
        cached = new DeferredMaterialStack(mat, count);
        PARSE_CACHE.put(trimmed, cached);
        return cached.copy();
    }

    public DeferredMaterialStack copy() {
        if (isEmpty()) return EMPTY;
        return new DeferredMaterialStack(material, amount);
    }

    public boolean isEmpty() {
        return this.amount < 1 || this.material == null;
    }

    public MaterialStack toMatStack() {
        if (isEmpty()) return MaterialStack.EMPTY;
        return new MaterialStack(this.material.get(), this.amount);
    }
}
