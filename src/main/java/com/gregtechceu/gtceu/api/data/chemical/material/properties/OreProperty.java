package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class OreProperty implements IMaterialProperty {

    /**
     * List of Ore byproducts.
     * <p>
     * Default: none, meaning only this property's Material.
     */
    private HolderSet<Material> oreByProducts = null;

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
    private @Nullable Holder<Material> directSmeltResult = null;

    /**
     * Material in which this Ore should be washed to give additional output.
     * <p>
     * Material will have a Fluid Property.
     * Default: none.
     */
    @Setter
    private @Nullable Holder<Material> washedIn = null;

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
    private HolderSet<Material> separatedInto = null;

    public OreProperty(int oreMultiplier, int byProductMultiplier) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = false;
    }

    public OreProperty(int oreMultiplier, int byProductMultiplier, boolean emissive) {
        this.oreMultiplier = oreMultiplier;
        this.byProductMultiplier = byProductMultiplier;
        this.emissive = emissive;
    }

    /**
     * Default values constructor.
     */
    @SuppressWarnings("unused")
    public OreProperty() {
        this(1, 1);
    }

    public void setWashedIn(Holder<Material> m, int washedAmount) {
        this.washedIn = m;
        this.washedAmount = washedAmount;
    }

    public @NotNull ObjectIntPair<@Nullable Material> getWashedIn() {
        return ObjectIntPair.of(this.washedIn == null ? null : washedIn.value(), this.washedAmount);
    }

    @SafeVarargs
    public final void setSeparatedInto(Holder<Material>... materials) {
        separatedInto = HolderSet.direct(Arrays.stream(materials).toList());
    }

    public HolderSet<Material> getOreByProducts() {
        if (oreByProducts == null) return HolderSet.empty();
        return oreByProducts;
    }

    /**
     * Set the ore byproducts for this property
     *
     * @param materials the materials to use as byproducts
     */
    @SafeVarargs
    public final void setOreByProducts(@NotNull Holder<Material> @NotNull... materials) {
        setOreByProducts(Arrays.asList(materials));
    }

    /**
     * Set the ore byproducts for this property
     *
     * @param materials the materials to use as byproducts
     */
    public void setOreByProducts(@NotNull Collection<Holder<Material>> materials) {
        oreByProducts = HolderSet.direct(new ArrayList<>(materials));
    }

    /**
     * Add ore byproducts to this property
     *
     * @param materials the materials to add as byproducts
     */
    @SafeVarargs
    public final void addOreByProducts(@NotNull Holder<Material> @NotNull... materials) {
        List<Holder<Material>> toAdd = new ArrayList<>(Arrays.stream(materials).toList());
        if (oreByProducts != null) toAdd.addAll(oreByProducts.stream().toList());
        oreByProducts = HolderSet.direct(toAdd);
    }

    public final @Nullable Material getOreByProduct(int index) {
        if (this.oreByProducts == null || this.oreByProducts.size() == 0) return null;
        return this.oreByProducts.get(Mth.clamp(index, 0, this.oreByProducts.size() - 1)).value();
    }

    @NotNull
    public final Material getOreByProduct(int index, @NotNull Material fallback) {
        Material material = getOreByProduct(index);
        return material != null ? material : fallback;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);

        if (directSmeltResult != null)
            directSmeltResult.value().getProperties().ensureSet(PropertyKey.DUST, true);
        if (washedIn != null)
            washedIn.value().getProperties().ensureSet(PropertyKey.FLUID, true);
        separatedInto.forEach(m -> m.value().getProperties().ensureSet(PropertyKey.DUST, true));
        oreByProducts.forEach(m -> m.value().getProperties().ensureSet(PropertyKey.DUST, true));
    }
}
