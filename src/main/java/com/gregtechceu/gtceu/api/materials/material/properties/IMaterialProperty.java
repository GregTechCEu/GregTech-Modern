package com.gregtechceu.gtceu.api.materials.material.properties;

@FunctionalInterface
public interface IMaterialProperty<T> {

    void verifyProperty(MaterialProperties properties);
}
