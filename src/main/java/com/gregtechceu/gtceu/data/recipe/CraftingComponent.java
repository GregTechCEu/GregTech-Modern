package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.V;

public class CraftingComponent {

    public static final Map<String, CraftingComponent> ALL_COMPONENTS = new Object2ReferenceOpenHashMap<>();

    public static final CraftingComponent EMPTY = CraftingComponent.of("empty", ItemStack.EMPTY);

    private final @Nullable CraftingComponentEntry[] values = new CraftingComponentEntry[V.length];
    @Setter
    private CraftingComponentEntry fallback;

    protected CraftingComponent(CraftingComponentEntry fallback) {
        this.fallback = fallback;
    }

    public static CraftingComponent of(String id, ItemStack fallback) {
        var existing = ALL_COMPONENTS.get(id);
        if (existing != null) {
            GTCEu.LOGGER.error("Duplicate crafting component id: {}, check components", id);
            return existing;
        }
        var ret = new CraftingComponent(new CraftingComponentEntry(fallback));
        ALL_COMPONENTS.put(id, ret);
        return ret;
    }

    public static CraftingComponent of(String id, MaterialEntry fallback) {
        var existing = ALL_COMPONENTS.get(id);
        if (existing != null) {
            GTCEu.LOGGER.error("Duplicate crafting component id: {}, check components", id);
            return existing;
        }
        var ret = new CraftingComponent(new CraftingComponentEntry(fallback));
        ALL_COMPONENTS.put(id, ret);
        return ret;
    }

    public static CraftingComponent of(String id, TagKey<Item> fallback) {
        var existing = ALL_COMPONENTS.get(id);
        if (existing != null) {
            GTCEu.LOGGER.error("Duplicate crafting component id: {}, check components", id);
            return existing;
        }
        var ret = new CraftingComponent(new CraftingComponentEntry(fallback));
        ALL_COMPONENTS.put(id, ret);
        return ret;
    }


    public static CraftingComponent of(String id, TagPrefix prefix, Material material) {
        return of(id, new MaterialEntry(prefix, material));
    }

    public CraftingComponentEntry get(int tier) {
        if (this == EMPTY) return fallback;
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        var val = values[tier];
        return val == null ? fallback : val;
    }

    public CraftingComponent add(int tier, ItemStack value) {
        if (this == EMPTY) return this;
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public CraftingComponent add(int tier, MaterialEntry value) {
        if (this == EMPTY) return this;
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public CraftingComponent add(int tier, TagPrefix prefix, Material material) {
        return add(tier, new MaterialEntry(prefix, material));
    }

    public CraftingComponent add(int tier, TagKey<Item> value) {
        if (this == EMPTY) return this;
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public void remove(int tier) {
        if (this == EMPTY) return;
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        values[tier] = null;
    }

    public static CraftingComponent get(String id) {
        if (!ALL_COMPONENTS.containsKey(id)) {
            GTCEu.LOGGER.error("No such crafting component: {}", id);
            return EMPTY;
        }
        return ALL_COMPONENTS.get(id);
    }

    @Accessors(fluent = true)
    public static class CraftingComponentEntry {
        @Getter
        private final @Nullable ItemStack itemStack;
        @Getter
        private final @Nullable MaterialEntry materialEntry;
        @Getter
        private final @Nullable TagKey<Item> itemTag;

        private CraftingComponentEntry(@Nullable ItemStack itemStack, @Nullable MaterialEntry materialEntry, @Nullable TagKey<Item> itemTag) {
            this.itemStack = itemStack;
            this.materialEntry = materialEntry;
            this.itemTag = itemTag;
        }

        public CraftingComponentEntry(ItemStack stack) {
            this(stack, null, null);
        }

        public CraftingComponentEntry(MaterialEntry materialEntry) {
            this(null, materialEntry, null);
        }

        public CraftingComponentEntry(TagKey<Item> itemTag) {
            this(null, null, itemTag);
        }
    }
}
