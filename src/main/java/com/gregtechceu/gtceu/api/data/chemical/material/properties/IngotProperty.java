package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.Holder;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Holder<Material> smeltingInto = null;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Holder<Material> arcSmeltingInto = null;

    /**
     * Specifies a Material into which this Material Macerates into.<br>
     * If null, material remains the same.
     */
    @Getter
    @Setter
    private @Nullable Holder<Material> macerateInto = null;

    /**
     * Material which obtained when this material is polarized.<br>
     * If null, polarization recipes not generated.
     */
    @Getter
    @Setter
    private @Nullable Holder<Material> magneticMaterial = null;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.GEM)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltingInto != null) smeltingInto.value().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltingInto != null) arcSmeltingInto.value().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto != null) macerateInto.value().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (magneticMaterial != null) magneticMaterial.value().getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
