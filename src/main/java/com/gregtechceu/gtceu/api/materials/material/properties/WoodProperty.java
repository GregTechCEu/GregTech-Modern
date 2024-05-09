package com.gregtechceu.gtceu.api.materials.material.properties;

public class WoodProperty implements IMaterialProperty<WoodProperty> {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
