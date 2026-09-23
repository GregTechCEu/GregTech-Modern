package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public record MaterialEntry(TagPrefix tagPrefix, Material material) {

    public MaterialEntry {
        Objects.requireNonNull(tagPrefix, "MaterialEntry cannot have null TagPrefix");
        Objects.requireNonNull(material, "MaterialEntry cannot have null Material");
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
        return tags.get(0).location().toString();
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
                var material = GTRegistries.MATERIALS.get(values[1]);
                if (material == null) throw new IllegalArgumentException("Invalid Material: " + values[1]);
                cached = new MaterialEntry(prefix, material);
                PARSE_CACHE.put(str, cached);
                return cached;
            }
        }
        return null;
    }
}
