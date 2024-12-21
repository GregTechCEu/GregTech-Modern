package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record MaterialEntry(TagPrefix tagPrefix, @Nullable Material material) {

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, null);
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
        int result = (tagPrefix != null ? tagPrefix.hashCode() : 0);
        result = 31 * result + (material != null ? material.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (tagPrefix == null && material == null) {
            return "Empty MaterialEntry";
        } else if (tagPrefix == null) {
            return material.getResourceLocation().toString();
        } else if (material == null) {
            return tagPrefix.name;
        }
        var tags = tagPrefix.getItemTags(material);
        if (tags.length == 0) {
            return tagPrefix.name + "/" + material.getName();
        }
        return tags[0].location().toString();
    }

    public static final MaterialEntry NullEntry = new MaterialEntry(null, null);
}
