package com.gregtechceu.gtceu.api.data.chemical.material.properties;

@FunctionalInterface
public interface IMaterialProperty {

    void verifyProperty(MaterialProperties properties);
}
