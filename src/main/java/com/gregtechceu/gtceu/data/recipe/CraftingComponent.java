package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.V;

public class CraftingComponent<T> {

    public static final Map<String, CraftingComponent<?>> ALL_COMPONENTS = new Object2ReferenceOpenHashMap<>();

    private final List<T> values = new ArrayList<>(Collections.nCopies(V.length, null));
    @Setter
    private @NotNull T fallback;

    protected CraftingComponent(@NotNull T fallback) {
        this.fallback = fallback;
    }

    public static <T> CraftingComponent<T> of(@NotNull String id, @NotNull T fallback) {
        var existing = ALL_COMPONENTS.get(id);
        if (existing != null) {
            GTCEu.LOGGER.error("Duplicate crafting component id: {}, check components", id);
            // noinspection unchecked
            return (CraftingComponent<T>) existing;
        }
        var ret = new CraftingComponent<>(fallback);
        ALL_COMPONENTS.put(id, ret);
        return ret;
    }

    public static CraftingComponent<MaterialEntry> of(@NotNull String id, @NotNull TagPrefix prefix,
                                                      @NotNull Material material) {
        return of(id, new MaterialEntry(prefix, material));
    }

    public @NotNull T get(int tier) {
        if (tier < 0 || tier >= values.size())
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        var val = values.get(tier);
        return val == null ? fallback : val;
    }

    public @NotNull CraftingComponent<T> add(int tier, @NotNull T value) {
        values.set(tier, value);
        return this;
    }

    public @NotNull CraftingComponent<T> add(int tier, @NotNull TagPrefix prefix, @NotNull Material material) {
        return add(tier, castValue(new MaterialEntry(prefix, material)));
    }

    public void remove(int tier) {
        if (tier < 0 || tier >= values.size())
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        values.set(tier, null);
    }

    @SuppressWarnings("unchecked")
    private T castValue(@NotNull Object value) {
        return (T) value;
    }

    public static CraftingComponent<?> get(String id) {
        if (!ALL_COMPONENTS.containsKey(id)) {
            throw new IllegalArgumentException("No such crafting component: " + id);
        }
        return ALL_COMPONENTS.get(id);
    }
}
