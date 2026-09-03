package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Material smeltingInto = null;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Material arcSmeltingInto = null;

    /**
     * Specifies a Material into which this Material Macerates into.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Material macerateInto = null;

    /**
     * Material which obtained when this material is polarized.<br>
     * If null, polarization recipes not generated.
     */
    @Getter
    @Setter
    private @Nullable Material magneticMaterial = null;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.GEM)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltingInto != null) smeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltingInto != null) arcSmeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto.isNull()) macerateInto = properties.getMaterial();
        else macerateInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (!magneticMaterial.isNull())
            magneticMaterial.getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
