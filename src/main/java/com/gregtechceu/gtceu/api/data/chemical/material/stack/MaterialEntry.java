package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public record MaterialEntry(@NotNull TagPrefix tagPrefix, @NotNull Material material) {

    public MaterialEntry {
        Preconditions.checkNotNull(tagPrefix, "MaterialEntry TagPrefix cannot be null!");
        Preconditions.checkNotNull(material, "MaterialEntry Material cannot be null!");
    }

    public static final MaterialEntry NULL_ENTRY = new MaterialEntry(TagPrefix.NULL_PREFIX, GTMaterials.NULL);

    private static final Map<String, MaterialEntry> PARSE_CACHE = new WeakHashMap<>();

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, GTMaterials.NULL);
    }

    public boolean isEmpty() {
        return this == NULL_ENTRY || material() == GTMaterials.NULL || tagPrefix().isEmpty();
    }

    public boolean isIgnored() {
        return tagPrefix().isIgnored(material());
    }

    @Override
    public String toString() {
        if (tagPrefix.isEmpty()) {
            return material.getResourceLocation().toString();
        }
        var tags = tagPrefix.getItemTags(material);
        if (tags.length == 0) {
            return tagPrefix.name + "/" + material.getName();
        }
        return tags[0].location().toString();
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
