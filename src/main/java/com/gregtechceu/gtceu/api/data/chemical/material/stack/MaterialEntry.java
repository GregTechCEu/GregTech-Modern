package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public record MaterialEntry(@Nullable TagPrefix tagPrefix, @Nullable Material material) {

    private static final Map<String, MaterialEntry> PARSE_CACHE = new WeakHashMap<>();

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, null);
    }

    public boolean isEmpty() {
        return material() == null || tagPrefix() == null;
    }

    public boolean isIgnored() {
        return tagPrefix() != null && tagPrefix().isIgnored(material());
    }

    public long getMaterialAmount() {
        if (tagPrefix != null) {
            if (material != null) {
                return tagPrefix.getMaterialAmount(material);
            }
            return tagPrefix.materialAmount();
        }
        if (material != null) {
            return GTValues.M;
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull String toString() {
        if (material == null) {
            return "MaterialEntry[empty]";
        }
        if (tagPrefix == null) {
            return material.getResourceLocation().toString();
        }
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
                var prefix = TagPrefix.get(values[0]);
                if (prefix == null) throw new IllegalArgumentException("Invalid TagPrefix: " + values[0]);
                cached = new MaterialEntry(prefix, GTMaterials.get(values[1]));
                PARSE_CACHE.put(str, cached);
                return cached;
            }
        }
        return null;
    }
}
