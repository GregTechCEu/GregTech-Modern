package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public record MaterialEntry(TagPrefix tagPrefix, Material material) {

    public MaterialEntry(Holder<TagPrefix> tagPrefixHolder, Holder<Material> materialHolder) {
        this(tagPrefixHolder.value(), materialHolder.value());
    }

    public MaterialEntry(TagPrefix tagPrefix, Holder<Material> materialHolder) {
        this(tagPrefix, materialHolder.value());
    }

    public MaterialEntry(Holder<TagPrefix> tagPrefixHolder, Material material) {
        this(tagPrefixHolder.value(), material);
    }

    public MaterialEntry(TagPrefix tagPrefix, Material material) {
        this.tagPrefix = Objects.requireNonNull(tagPrefix, "MaterialEntry TagPrefix cannot be null!");
        this.material = Objects.requireNonNull(material, "MaterialEntry Material cannot be null!");
    }

    private static final Map<String, MaterialEntry> PARSE_CACHE = new WeakHashMap<>();

    public boolean isIgnored() {
        return tagPrefix().isIgnored(material());
    }

    public long getMaterialAmount() {
        return tagPrefix.getMaterialAmount(material);
    }

    @Override
    public String toString() {
        var tags = tagPrefix.getItemTags(material);
        if (tags.isEmpty()) {
            return tagPrefix.name + "/" + material.getName();
        }
        return tags.getFirst().location().toString();
    }

    public static @Nullable MaterialEntry of(Object o) {
        if (o instanceof MaterialEntry entry) return entry;
        if (o instanceof CharSequence chars) {
            var str = chars.toString().trim();
            var cached = PARSE_CACHE.get(str);
            if (cached != null) return cached;

            var values = str.split(":", 2);
            if (values.length > 1) {
                var prefix = GTRegistries.TAG_PREFIXES.get(GTCEu.id(values[0]));
                if (prefix == null) throw new IllegalArgumentException("Invalid TagPrefix: " + values[0]);
                var material = GTRegistries.MATERIALS.get(GTCEu.id(values[1]));
                if (material == null) throw new IllegalArgumentException("Invalid material: " + values[1]);
                cached = new MaterialEntry(prefix, material);
                PARSE_CACHE.put(str, cached);
                return cached;
            }
        }
        return null;
    }
}
