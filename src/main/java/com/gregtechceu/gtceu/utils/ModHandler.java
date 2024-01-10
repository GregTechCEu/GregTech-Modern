package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import org.jetbrains.annotations.Nullable;

public class ModHandler {

    /**
     * @param material the material to check
     * @return if the material is a wood
     */
    public static boolean isMaterialWood(@Nullable Material material) {
        return material != null && material.hasProperty(PropertyKey.WOOD);
    }
}
