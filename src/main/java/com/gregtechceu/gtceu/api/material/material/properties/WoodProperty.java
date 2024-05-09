package com.gregtechceu.gtceu.api.material.material.properties;

public class WoodProperty implements IMaterialProperty<WoodProperty> {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
