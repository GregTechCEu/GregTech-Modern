package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class MaterialProperties {

    private static final Set<PropertyKey<?>> baseTypes = new HashSet<>(Arrays.asList(
            PropertyKey.FLUID, PropertyKey.DUST,
            PropertyKey.INGOT, PropertyKey.GEM, PropertyKey.EMPTY));

    @SuppressWarnings("unused")
    public static void addBaseType(PropertyKey<?> baseTypeKey) {
        baseTypes.add(baseTypeKey);
    }

    private final Map<PropertyKey<? extends IMaterialProperty>, IMaterialProperty> propertyMap;
    @Getter
    @Setter
    private Material material;

    public MaterialProperties() {
        propertyMap = new HashMap<>();
    }

    public boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return key.cast(propertyMap.get(key));
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return propertyMap.containsKey(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty value) {
        if (value == null) throw new IllegalArgumentException("Material Property must not be null!");
        if (!key.getType().isInstance(value))
            throw new IllegalArgumentException("Material Property must be of the same type as the property key!");
        if (hasProperty(key))
            throw new IllegalArgumentException("Material Property " + key.toString() + " already registered!");
        propertyMap.put(key, value);
        propertyMap.remove(PropertyKey.EMPTY);
    }

    public <T extends IMaterialProperty> void removeProperty(PropertyKey<T> property) {
        if (!hasProperty(property))
            throw new IllegalArgumentException("Material Property " + property.toString() + " not present!");
        propertyMap.remove(property);
        if (propertyMap.isEmpty())
            propertyMap.put(PropertyKey.EMPTY, PropertyKey.EMPTY.constructDefault());
    }

    public <T extends IMaterialProperty> void ensureSet(PropertyKey<T> key, boolean verify) {
        if (!hasProperty(key)) {
            propertyMap.put(key, key.constructDefault());
            propertyMap.remove(PropertyKey.EMPTY);
            if (verify) verify();
        }
    }

    public <T extends IMaterialProperty> void ensureSet(PropertyKey<T> key) {
        ensureSet(key, false);
    }

    public void verify() {
        List<IMaterialProperty> oldList;
        do {
            oldList = new ArrayList<>(propertyMap.values());
            oldList.forEach(p -> p.verifyProperty(this));
        } while (oldList.size() != propertyMap.size());

        if (propertyMap.keySet().stream().noneMatch(baseTypes::contains)) {
            if (propertyMap.isEmpty()) {
                if (GTCEu.isDev()) {
                    GTCEu.LOGGER.debug("Creating empty placeholder Material {}", material);
                }
                propertyMap.put(PropertyKey.EMPTY, PropertyKey.EMPTY.constructDefault());
            } else
                throw new IllegalArgumentException("Material must have at least one of: " + baseTypes + " specified!");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        propertyMap.forEach((k, v) -> sb.append(k.toString()).append("\n"));
        return sb.toString();
    }
}
