package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MaterialEntry(@NotNull TagPrefix tagPrefix, @NotNull Material material) {

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
                return new MaterialEntry(TagPrefix.get(values[0]), GTMaterials.get(values[1]));
            }
        }
        return null;
    }
}
