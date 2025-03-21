package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import lombok.Getter;
import lombok.Setter;

public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    @Getter
    @Setter
    private Material smeltingInto = GTMaterials.NULL;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    @Getter
    @Setter
    private Material arcSmeltingInto = GTMaterials.NULL;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    @Getter
    @Setter
    private Material macerateInto = GTMaterials.NULL;

    /**
     * Material which obtained when this material is polarized
     */
    @Getter
    @Setter
    private Material magneticMaterial = GTMaterials.NULL;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.GEM)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltingInto == GTMaterials.NULL || smeltingInto == null) smeltingInto = properties.getMaterial();
        else smeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltingInto == GTMaterials.NULL || arcSmeltingInto == null) arcSmeltingInto = properties.getMaterial();
        else arcSmeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto == GTMaterials.NULL || macerateInto == null) macerateInto = properties.getMaterial();
        else macerateInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (magneticMaterial != GTMaterials.NULL && magneticMaterial != null)
            magneticMaterial.getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
