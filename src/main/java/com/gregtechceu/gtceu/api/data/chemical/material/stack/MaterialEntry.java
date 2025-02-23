package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record MaterialEntry(@NotNull TagPrefix tagPrefix, @NotNull Material material) {

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, GTMaterials.NULL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialEntry that = (MaterialEntry) o;

        if (tagPrefix != that.tagPrefix) return false;
        return Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        int result = tagPrefix.hashCode();
        result = 31 * result + material.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (tagPrefix == TagPrefix.nullPrefix) {
            return material.getResourceLocation().toString();
        }
        var tags = tagPrefix.getItemTags(material);
        if (tags.length == 0) {
            return tagPrefix.name + "/" + material.getName();
        }
        return tags[0].location().toString();
    }

    public static final MaterialEntry NullEntry = new MaterialEntry(TagPrefix.nullPrefix, GTMaterials.NULL);
}
