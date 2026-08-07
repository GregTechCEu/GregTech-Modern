package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MaterialFlag {

    private static final Set<MaterialFlag> FLAG_REGISTRY = new HashSet<>();

    private final String name;

    private final Set<MaterialFlag> requiredFlags;
    private final Set<PropertyKey<?>> requiredProperties;

    private MaterialFlag(String name, Set<MaterialFlag> requiredFlags, Set<PropertyKey<?>> requiredProperties) {
        this.name = name;
        this.requiredFlags = requiredFlags;
        this.requiredProperties = requiredProperties;
        FLAG_REGISTRY.add(this);
    }

    protected Set<MaterialFlag> verifyFlag(Material material) {
        requiredProperties.forEach(key -> {
            if (!material.hasProperty(key)) {
                GTCEu.LOGGER.warn("Material {} does not have required property {} for flag {}!",
                        material.getUnlocalizedName(), key.toString(), this.name);
            }
        });

        Set<MaterialFlag> thisAndDependencies = new HashSet<>(requiredFlags);
        requiredFlags.stream()
                .map(f -> f.verifyFlag(material))
                .forEach(thisAndDependencies::addAll);

        return thisAndDependencies;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static MaterialFlag getByName(String name) {
        return FLAG_REGISTRY.stream().filter(f -> f.toString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialFlag that = (MaterialFlag) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static class Builder {

        final String name;

        final Set<MaterialFlag> requiredFlags = new ObjectOpenHashSet<>();
        final Set<PropertyKey<?>> requiredProperties = new ObjectOpenHashSet<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder requireFlags(MaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public Builder requireProps(PropertyKey<?>... propertyKeys) {
            requiredProperties.addAll(Arrays.asList(propertyKeys));
            return this;
        }

        public MaterialFlag build() {
            return new MaterialFlag(name, requiredFlags, requiredProperties);
        }
    }
}
