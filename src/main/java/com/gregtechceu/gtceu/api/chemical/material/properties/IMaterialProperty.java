package com.gregtechceu.gtceu.api.chemical.material.properties;

@FunctionalInterface
public interface IMaterialProperty<T> {

    void verifyProperty(MaterialProperties properties);
}
