package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    @Getter
    @Setter
    @Nullable
    private Material smeltingInto = null;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    @Getter
    @Setter
    @Nullable
    private Material arcSmeltingInto = null;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    @Getter
    @Setter
    @Nullable
    private Material macerateInto = null;

    /**
     * Material which obtained when this material is polarized
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

        if (smeltingInto == null) smeltingInto = properties.getMaterial();
        else smeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltingInto == null) arcSmeltingInto = properties.getMaterial();
        else arcSmeltingInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto == null) macerateInto = properties.getMaterial();
        else macerateInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (magneticMaterial != null)
            magneticMaterial.getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
