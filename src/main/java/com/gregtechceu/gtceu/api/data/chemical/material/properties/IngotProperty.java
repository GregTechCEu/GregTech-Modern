package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lombok.*;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
@AllArgsConstructor
public class IngotProperty implements IMaterialProperty<IngotProperty> {
    public static final Codec<IngotProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Material.CODEC.optionalFieldOf("smelt_into", null).forGetter(val -> val.smeltingInto),
        Material.CODEC.optionalFieldOf("arc_smelt_into", null).forGetter(val -> val.arcSmeltingInto),
        Material.CODEC.optionalFieldOf("macerate_into", null).forGetter(val -> val.macerateInto),
        Material.CODEC.optionalFieldOf("magnetic_material", null).forGetter(val -> val.magneticMaterial)
    ).apply(instance, IngotProperty::new));


    /**
     * Specifies a material into which this material parts turn when heated
     */
    @Getter
    @Setter
    private Material smeltingInto;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    @Getter
    @Setter
    private Material arcSmeltingInto;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    @Getter
    @Setter
    private Material macerateInto;

    /**
     * Material which obtained when this material is polarized
     */
    @Nullable
    @Getter
    @Setter
    private Material magneticMaterial;

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

        if (magneticMaterial != null) magneticMaterial.getProperties().ensureSet(PropertyKey.INGOT, true);
    }
}
