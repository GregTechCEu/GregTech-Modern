package com.lowdragmc.gtceu.api.data.chemical.material.properties;

import com.lowdragmc.gtceu.api.data.chemical.material.Material;

import javax.annotation.Nullable;

public class IngotProperty implements IMaterialProperty<IngotProperty> {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    private Material smeltInto;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    private Material arcSmeltInto;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    private Material macerateInto;

    /**
     * Material which obtained when this material is polarized
     */
    @Nullable
    private Material magneticMaterial;

    public void setSmeltingInto(Material smeltInto) {
        this.smeltInto = smeltInto;
    }

    public Material getSmeltingInto() {
        return this.smeltInto;
    }

    public void setArcSmeltingInto(Material arcSmeltingInto) {
        this.arcSmeltInto = arcSmeltingInto;
    }

    public Material getArcSmeltInto() {
        return this.arcSmeltInto;
    }

    public void setMagneticMaterial(@Nullable Material magneticMaterial) {
        this.magneticMaterial = magneticMaterial;
    }

    @Nullable
    public Material getMagneticMaterial() {
        return magneticMaterial;
    }

    public void setMacerateInto(Material macerateInto) {
        this.macerateInto = macerateInto;
    }

    public Material getMacerateInto() {
        return macerateInto;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.GEM)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltInto == null) smeltInto = properties.getMaterial();
        else smeltInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltInto == null) arcSmeltInto = properties.getMaterial();
        else arcSmeltInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto == null) macerateInto = properties.getMaterial();
        else macerateInto.getProperties().ensureSet(PropertyKey.INGOT, true);

        if (magneticMaterial != null) magneticMaterial.getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
