package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.V;

public class CraftingComponent {

    public static final Map<String, CraftingComponent> ALL_COMPONENTS = new Object2ReferenceOpenHashMap<>();

    public static final CraftingComponent EMPTY = CraftingComponent.of("empty", ItemStack.EMPTY);

    private final Object[] values = new Object[V.length];
    @Setter
    private @NotNull Object fallback;

    protected CraftingComponent(@NotNull Object fallback) {
        checkType(fallback);
        this.fallback = fallback;
    }

    public static CraftingComponent of(@NotNull String id, @NotNull Object fallback) {
        var existing = ALL_COMPONENTS.get(id);
        if (existing != null) {
            GTCEu.LOGGER.error("Duplicate crafting component id: {}, check components", id);
            return existing;
        }
        var ret = new CraftingComponent(fallback);
        ALL_COMPONENTS.put(id, ret);
        return ret;
    }

    public static CraftingComponent of(@NotNull String id, @NotNull TagPrefix prefix, @NotNull Material material) {
        return of(id, new MaterialEntry(prefix, material));
    }

    public @NotNull Object get(int tier) {
        if (this == EMPTY) return ItemStack.EMPTY;
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        var val = values[tier];
        return val == null ? fallback : val;
    }

    public @NotNull CraftingComponent add(int tier, @NotNull Object value) {
        if (this == EMPTY) return this;
        checkType(value);
        values[tier] = value;
        return this;
    }

    public @NotNull CraftingComponent add(int tier, @NotNull TagPrefix prefix, @NotNull Material material) {
        return add(tier, new MaterialEntry(prefix, material));
    }

    public void remove(int tier) {
        if (this == EMPTY) return;
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        values[tier] = null;
    }

    private void checkType(@NotNull Object o) {
        if ((o instanceof TagKey<?> tag)) {
            if (!tag.isFor(BuiltInRegistries.ITEM.key())) {
                throw new IllegalArgumentException("TagKey must be of type TagKey<Item>");
            }
        } else if (!(o instanceof ItemStack || o instanceof MaterialEntry)) {
            throw new IllegalArgumentException("Object is not of type ItemStack, MaterialEntry or TagKey<Item>");
        }
    }

    public static CraftingComponent get(String id) {
        if (!ALL_COMPONENTS.containsKey(id)) {
            GTCEu.LOGGER.error("No such crafting component: {}", id);
            return EMPTY;
        }
        return ALL_COMPONENTS.get(id);
    }
}
