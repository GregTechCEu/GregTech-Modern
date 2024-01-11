package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;

public class PolymerProperty implements IMaterialProperty<PolymerProperty>{


    @Override
    public void verifyProperty(MaterialProperties properties) {

        properties.ensureSet(PropertyKey.DUST, true);
        properties.ensureSet(PropertyKey.INGOT, true);
        properties.ensureSet(PropertyKey.FLUID, true);

        properties.getMaterial().addFlags(MaterialFlags.FLAMMABLE, MaterialFlags.NO_SMASHING, MaterialFlags.DISABLE_DECOMPOSITION);

    }
}
