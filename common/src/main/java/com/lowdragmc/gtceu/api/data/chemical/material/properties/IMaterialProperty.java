package com.lowdragmc.gtceu.api.data.chemical.material.properties;

@FunctionalInterface
public interface IMaterialProperty<T> {

    void verifyProperty(MaterialProperties properties);
}
