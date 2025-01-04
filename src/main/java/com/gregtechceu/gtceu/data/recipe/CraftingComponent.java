package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.GTValues.V;

public class CraftingComponent {

    private final Object[] values = new Object[V.length];
    @Setter
    private @NotNull Object fallback;

    public CraftingComponent(@NotNull Object fallback) {
        checkType(fallback);
        this.fallback = fallback;
    }

    public @NotNull Object get(int tier) {
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        var val = values[tier];
        return val == null ? fallback : val;
    }

    public @NotNull CraftingComponent add(int tier, @NotNull Object value) {
        checkType(value);
        values[tier] = value;
        return this;
    }

    public void remove(int tier) {
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        values[tier] = null;
    }

    private void checkType(@NotNull Object o) {
        if ((o instanceof TagKey<?> tag)) {
            if (!tag.isFor(BuiltInRegistries.ITEM.key())) {
                throw new IllegalArgumentException("TagKey must be of type TagKey<Item>");
            }
        } else if (!(o instanceof ItemStack || o instanceof UnificationEntry)) {
            throw new IllegalArgumentException("Object is not of type ItemStack, UnificationEntry or TagKey<Item>");
        }
    }
}
