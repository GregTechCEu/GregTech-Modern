package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class OreProperty implements IMaterialProperty<OreProperty> {

    public static final Codec<OreProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.listOf().optionalFieldOf("byproducts", List.of()).forGetter(val -> val.oreByProducts),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("ore_multiplier", 1).forGetter(val -> val.oreMultiplier),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("byproduct_multiplier", 1)
                    .forGetter(val -> val.byProductMultiplier),
            Codec.BOOL.optionalFieldOf("emissive", false).forGetter(val -> val.emissive),
            Material.CODEC.optionalFieldOf("direct_smelt_result", null).forGetter(val -> val.directSmeltResult),
            Material.CODEC.optionalFieldOf("washed_in", null).forGetter(val -> val.washedIn),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("washed_amount", 100).forGetter(val -> val.washedAmount),
            Material.CODEC.listOf().optionalFieldOf("separated_into", List.of()).forGetter(val -> val.separatedInto))
            .apply(instance, OreProperty::new));

    /**
     * List of Ore byproducts.
     * <p>
     * Default: none, meaning only this property's Material.
     */
    @Getter
    private List<Material> oreByProducts;

    /**
     * Crushed Ore output amount multiplier during Maceration.
     * <p>
     * Default: 1 (no multiplier).
     */
    @Getter
    @Setter
    private int oreMultiplier;

    /**
     * Byproducts output amount multiplier during Maceration.
     * <p>
     * Default: 1 (no multiplier).
     */
    @Getter
    @Setter
    private int byProductMultiplier;

    /**
     * Should ore block use the emissive texture.
     * <p>
     * Default: false.
     */
    @Getter
    @Setter
    private boolean emissive;

    /**
     * Material to which smelting of this Ore will result.
     * <p>
     * Material will have a Dust Property.
     * Default: none.
     */
    @Getter
    @Setter
    @Nullable
    private Material directSmeltResult;

    /**
     * Material in which this Ore should be washed to give additional output.
     * <p>
     * Material will have a Fluid Property.
     * Default: none.
     */
    @Setter
    @Nullable
    private Material washedIn;

    /**
     * The amount of Material that the ore should be washed in
     * in the Chemical Bath.
     * <p>
     * Default 100 mb
     */
    private int washedAmount = 100;

    /**
     * During Electromagnetic Separation, this Ore will be separated
     * into this Material and the Material specified by this field.
     * Limit 2 Materials
     * <p>
     * Material will have a Dust Property.
     * Default: none.
     */
    @Getter
    private List<Material> separatedInto;

    public OreProperty(int oreMultiplier, int byProductMultiplier) {
        this(oreMultiplier, byProductMultiplier, false);
    }

    public OreProperty(int oreMultiplier, int byProductMultiplier, boolean emissive) {
        this.oreByProducts = new ArrayList<>();
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = emissive;
        this.separatedInto = new ArrayList<>();
    }

    /**
     * Default values constructor.
     */
    public OreProperty() {
        this(1, 1);
    }

    public void setWashedIn(@Nullable Material m, int washedAmount) {
        this.washedIn = m;
        this.washedAmount = washedAmount;
    }

    public Pair<Material, Integer> getWashedIn() {
        return Pair.of(this.washedIn, this.washedAmount);
    }

    public void setSeparatedInto(Material... materials) {
        this.separatedInto.addAll(Arrays.asList(materials));
    }

    public void setOreByProducts(Material... materials) {
        this.oreByProducts.addAll(Arrays.asList(materials));
    }

    @Nullable
    public final Material getOreByProduct(int index) {
        if (this.oreByProducts.isEmpty()) return null;
        return this.oreByProducts.get(Mth.clamp(index, 0, this.oreByProducts.size() - 1));
    }

    @NotNull
    public final Material getOreByProduct(int index, @NotNull Material fallback) {
        Material material = getOreByProduct(index);
        return material != null ? material : fallback;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);

        if (directSmeltResult != null) directSmeltResult.getProperties().ensureSet(PropertyKey.DUST, true);
        if (washedIn != null) washedIn.getProperties().ensureSet(PropertyKey.FLUID, true);
        separatedInto.forEach(m -> m.getProperties().ensureSet(PropertyKey.DUST, true));
        oreByProducts.forEach(m -> m.getProperties().ensureSet(PropertyKey.DUST, true));
    }
}
