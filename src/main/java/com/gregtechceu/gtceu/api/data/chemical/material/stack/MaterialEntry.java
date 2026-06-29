package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public record MaterialEntry(@NotNull TagPrefix tagPrefix, @NotNull Material material) {

    public MaterialEntry {
        Preconditions.checkNotNull(tagPrefix, "MaterialEntry TagPrefix cannot be null!");
        Preconditions.checkNotNull(material, "MaterialEntry Material cannot be null!");
    }

    public MaterialEntry(Holder<TagPrefix> tagPrefix, Holder<Material> material) {
        this(tagPrefix.get(), material.get());
    }

    public MaterialEntry(TagPrefix tagPrefix, Holder<Material> material) {
        this(tagPrefix, material.get());
    }

    public MaterialEntry(Holder<TagPrefix> tagPrefix, Material material) {
        this(tagPrefix.get(), material);
    }

    public static final MaterialEntry NULL_ENTRY = new MaterialEntry(TagPrefix.NULL_PREFIX, GTMaterials.NULL);

    private static final Map<String, MaterialEntry> PARSE_CACHE = new WeakHashMap<>();

    public MaterialEntry(TagPrefix tagPrefix) {
        this(tagPrefix, GTMaterials.NULL);
    }

    public boolean isEmpty() {
        return this == NULL_ENTRY || material().isNull() || tagPrefix().isEmpty();
    }

    public boolean isIgnored() {
        return tagPrefix().isIgnored(material());
    }

    public long getMaterialAmount() {
        if (!tagPrefix.isEmpty()) {
            if (!material.isNull()) {
                return tagPrefix.getMaterialAmount(material);
            }
            return tagPrefix.materialAmount();
        }
        if (!material.isNull()) {
            return GTValues.M;
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull String toString() {
        if (tagPrefix.isEmpty()) {
            return material.getResourceLocation().toString();
        }
        var tags = tagPrefix.getItemTags(material);
        if (tags.isEmpty()) {
            return tagPrefix.getName() + "/" + material.getName();
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

                cached = new MaterialEntry(prefix, GTRegistries.MATERIALS.get(GTCEu.id(values[1])));
                PARSE_CACHE.put(str, cached);
                return cached;
            }
        }
        return null;
    }
}
