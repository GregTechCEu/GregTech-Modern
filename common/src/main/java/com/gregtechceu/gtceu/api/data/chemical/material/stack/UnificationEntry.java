package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import javax.annotation.Nullable;
import java.util.Objects;

public class UnificationEntry {

    public final TagPrefix tagPrefix;
    @Nullable
    public final Material material;

    public UnificationEntry(TagPrefix tagPrefix, @Nullable Material material) {
        this.tagPrefix = tagPrefix;
        this.material = material;
    }

    public UnificationEntry(TagPrefix tagPrefix) {
        this.tagPrefix = tagPrefix;
        this.material = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnificationEntry that = (UnificationEntry) o;

        if (tagPrefix != that.tagPrefix) return false;
        return Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        int result = tagPrefix.hashCode();
        result = 31 * result + (material != null ? material.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return tagPrefix.name + (material != null ? material.toCamelCaseString() : "");
    }

    public static class EmptyMapMarkerEntry extends UnificationEntry {

        public EmptyMapMarkerEntry() {
            super(null);
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "EMPTY UNIFICATION ENTRY";
        }
    }
}
