package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.google.common.base.Preconditions;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MaterialEntry(@NotNull TagPrefix tagPrefix, @NotNull Material material) {

    public MaterialEntry {
        Preconditions.checkNotNull(tagPrefix, "MaterialEntry TagPrefix cannot be null!");
        Preconditions.checkNotNull(material, "MaterialEntry Material cannot be null!");
    }

    public static final MaterialEntry NULL_ENTRY = new MaterialEntry(TagPrefix.NULL_PREFIX, GTMaterials.NULL);

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, GTMaterials.NULL);
    }

    public boolean isEmpty() {
        return this == NULL_ENTRY || material() == GTMaterials.NULL || tagPrefix().isEmpty();
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
            var values = chars.toString().split(":");
            if (values.length >= 2) {
                var prefix = TagPrefix.get(values[0]);
                if (prefix == null) throw new IllegalArgumentException("Invalid TagPrefix: " + values[0]);
                return new MaterialEntry(prefix, GTMaterials.get(values[1]));
            }
        }
        return null;
    }
}
