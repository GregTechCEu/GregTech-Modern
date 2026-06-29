package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;

public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    @Getter
    @Setter
    @NotNull
    private Holder<Material> smeltingInto = GTMaterials.NULL;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    @Getter
    @Setter
    @NotNull
    private Holder<Material> arcSmeltingInto = GTMaterials.NULL;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    @Getter
    @Setter
    @NotNull
    private Holder<Material> macerateInto = GTMaterials.NULL;

    /**
     * Material which obtained when this material is polarized
     */
    @Getter
    @Setter
    @NotNull
    private Holder<Material> magneticMaterial = GTMaterials.NULL;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.GEM)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltingInto.get().isNull()) smeltingInto = properties.getMaterial();
        else smeltingInto.get().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (arcSmeltingInto.get().isNull()) arcSmeltingInto = properties.getMaterial();
        else arcSmeltingInto.get().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (macerateInto.get().isNull()) macerateInto = properties.getMaterial();
        else macerateInto.get().getProperties().ensureSet(PropertyKey.INGOT, true);

        if (!magneticMaterial.get().isNull())
            magneticMaterial.get().getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
