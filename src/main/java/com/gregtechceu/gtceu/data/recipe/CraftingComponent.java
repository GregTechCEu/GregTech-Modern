package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.V;

public class CraftingComponent {

    public static final Map<String, CraftingComponent> ALL_COMPONENTS = new Object2ReferenceOpenHashMap<>();
    
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
        if (tier < 0 || tier >= values.length)
            throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        var val = values[tier];
        return val == null ? fallback : val;
    }

    public CraftingComponent add(int tier, ItemStack value) {
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public CraftingComponent add(int tier, MaterialEntry value) {
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public CraftingComponent add(int tier, TagPrefix prefix, Material material) {
        return add(tier, new MaterialEntry(prefix, material));
    }

    public CraftingComponent add(int tier, TagKey<Item> value) {
        values[tier] = new CraftingComponentEntry(value);
        return this;
    }

    public void remove(int tier) {
        if (tier < 0 || tier >= values.length) throw new IllegalArgumentException("Tier out of range of ULV-MAX, tier: " + tier);
        values[tier] = null;
    }

    public static CraftingComponent get(String id) {
        if (!ALL_COMPONENTS.containsKey(id)) {
            throw new IllegalArgumentException("No such crafting component: " + id);
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

        public static final Codec<CraftingComponentEntry> CODEC = new Codec<>() {

            private static final Codec<TagKey<Item>> TAG_KEY_CODEC = TagKey.hashedCodec(Registries.ITEM);

            @Override
            public <T> DataResult<Pair<CraftingComponentEntry, T>> decode(DynamicOps<T> ops, T input) {
                final DataResult<Pair<CraftingComponentEntry, T>> firstRead = ItemStack.CODEC.decode(ops, input).map(p -> p.mapFirst(CraftingComponentEntry::new));
                if (firstRead.isSuccess()) {
                    return firstRead;
                }
                final DataResult<Pair<CraftingComponentEntry, T>> secondRead = TAG_KEY_CODEC.decode(ops, input).map(p -> p.mapFirst(CraftingComponentEntry::new));
                if (secondRead.isSuccess()) {
                    return secondRead;
                }
                final DataResult<Pair<CraftingComponentEntry, T>> thirdRead = MaterialEntry.CODEC.decode(ops, input).map(p -> p.mapFirst(CraftingComponentEntry::new));
                if (thirdRead.isSuccess()) {
                    return thirdRead;
                }
                if (firstRead.hasResultOrPartial()) {
                    return firstRead;
                }
                if (secondRead.hasResultOrPartial()) {
                    return secondRead;
                }
                if (thirdRead.hasResultOrPartial()) {
                    return thirdRead;
                }
                return DataResult.error(() -> "Failed to parse crafting component entry. First: " + firstRead.error().orElseThrow().message() +
                        "; Second: " + secondRead.error().orElseThrow().message() +
                        "; Third: " + thirdRead.error().orElseThrow().message());
            }

            @Override
            public <T> DataResult<T> encode(CraftingComponentEntry input, DynamicOps<T> ops, T prefix) {
                if (input.itemStack() != null) return ItemStack.CODEC.encode(input.itemStack(), ops, prefix);
                if (input.itemTag() != null) return TAG_KEY_CODEC.encode(input.itemTag(), ops, prefix);
                return MaterialEntry.CODEC.encode(Objects.requireNonNull(input.materialEntry()), ops, prefix);
            }
        };
    }
}
