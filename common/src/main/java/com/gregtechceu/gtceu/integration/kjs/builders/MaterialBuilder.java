package com.gregtechceu.gtceu.integration.kjs.builders;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidType;
import com.gregtechceu.gtceu.api.data.chemical.fluid.FluidTypes;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collection;

@SuppressWarnings("unused")
public class MaterialBuilder extends BuilderBase<Material> {
    public transient Material.Builder builder;

    public MaterialBuilder(ResourceLocation i) {
        super(i);
        this.builder = new Material.Builder(i.getPath());
    }

    /*
     * Material Types
     */

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Will be created as a {@link FluidTypes#LIQUID}, without a Fluid Block.
     *
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    public MaterialBuilder fluid() {
        builder.fluid();
        return this;
    }

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Will be created without a Fluid Block.
     *
     * @param type The {@link FluidType} of this Material, either Fluid or Gas.
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    public MaterialBuilder fluid(FluidType type) {
        return fluid(type, false);
    }

    /**
     * Add a {@link FluidProperty} to this Material.
     *
     * @param type     The {@link FluidType} of this Material.
     * @param hasBlock If true, create a Fluid Block for this Material.
     * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
     */
    public MaterialBuilder fluid(FluidType type, boolean hasBlock) {
        builder.fluid(type, hasBlock);
        return this;
    }

    /**
     * Add a {@link PlasmaProperty} to this Material.<br>
     * Is not required to have a {@link FluidProperty}, and will not automatically apply one.
     *
     * @throws IllegalArgumentException If a {@link PlasmaProperty} has already been added to this Material.
     */
    public MaterialBuilder plasma() {
        builder.plasma();
        return this;
    }

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
     *
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder dust() {
        builder.dust();
        return this;
    }

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder dust(int harvestLevel) {
        return dust(harvestLevel, 0);
    }

    /**
     * Add a {@link DustProperty} to this Material.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder dust(int harvestLevel, int burnTime) {
        builder.dust(harvestLevel, burnTime);
        return this;
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot() {
        builder.ingot();
        return this;
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot(int harvestLevel) {
        return ingot(harvestLevel, 0);
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot(int harvestLevel, int burnTime) {
        builder.ingot(harvestLevel, burnTime);
        return this;
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    public MaterialBuilder gem() {
        builder.gem();
        return this;
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will be created with no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
     */
    public MaterialBuilder gem(int harvestLevel) {
        return gem(harvestLevel, 0);
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     */
    public MaterialBuilder gem(int harvestLevel, int burnTime) {
        builder.gem(harvestLevel, burnTime);
        return this;
    }

    /**
     * Add a {@link PolymerProperty} to this Material.<br>
     * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
     */
    public MaterialBuilder polymer() {
        builder.polymer();
        return this;
    }

    /**
     * Add a {@link PolymerProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * Will have a burn time of 0
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
     */
    public MaterialBuilder polymer(int harvestLevel) {
        builder.polymer(harvestLevel);
        return this;
    }

    public MaterialBuilder burnTime(int burnTime) {
        builder.burnTime(burnTime);
        return this;
    }

    /**
     * Set the Color of this Material.<br>
     * Defaults to 0xFFFFFF unless {@link Material.Builder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     *
     * @param color The RGB-formatted Color.
     */
    public MaterialBuilder color(int color) {
        builder.color(color);
        return this;
    }

    /**
     * Set the Color of this Material.<br>
     * Defaults to 0xFFFFFF unless {@link Material.Builder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     *
     * @param color         The RGB-formatted Color.
     * @param hasFluidColor Whether the fluid should be colored or not.
     */
    public MaterialBuilder color(int color, boolean hasFluidColor) {
        builder.color(color, hasFluidColor);
        return this;
    }

    public MaterialBuilder colorAverage() {
        builder.colorAverage();
        return this;
    }

    /**
     * Set the {@link MaterialIconSet} of this Material.<br>
     * Defaults vary depending on if the Material has a:<br>
     * <ul>
     * <li> {@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
     * <li> {@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
     * <li> {@link FluidProperty}, it will default to either {@link MaterialIconSet#FLUID}
     *      or {@link MaterialIconSet#GAS}, depending on the {@link FluidType}
     * <li> {@link PlasmaProperty}, it will default to {@link MaterialIconSet#FLUID}
     * </ul>
     * Default will be determined by first-found Property in this order, unless specified.
     *
     * @param iconSet The {@link MaterialIconSet} of this Material.
     */
    public MaterialBuilder iconSet(MaterialIconSet iconSet) {
        builder.iconSet(iconSet);
        return this;
    }

    public MaterialBuilder components(Object... components) {
        builder.components(components);
        return this;
    }

    public MaterialBuilder components(MaterialStack... components) {
        builder.components(components);
        return this;
    }

    public MaterialBuilder components(ImmutableList<MaterialStack> components) {
        builder.components(components);
        return this;
    }

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     */
    public MaterialBuilder flags(MaterialFlag... flags) {
        builder.flags(flags);
        return this;
    }

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     *
     * @param f1 A {@link Collection} of {@link MaterialFlag}. Provided this way for easy Flag presets to be applied.
     * @param f2 An Array of {@link MaterialFlag}. If no {@link Collection} is required, use {@link Material.Builder#flags(MaterialFlag...)}.
     */
    // rename for kjs conflicts
    public MaterialBuilder appendFlags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        builder.appendFlags(f1, f2);
        return this;
    }

    public MaterialBuilder element(Element element) {
        builder.element(element);
        return this;
    }

    /**
     * Replaced the old toolStats methods which took many parameters.
     * Use {@link ToolProperty.Builder} instead to create a Tool Property.
     */
    public MaterialBuilder toolStats(ToolProperty toolProperty) {
        builder.toolStats(toolProperty);
        return this;
    }

    public MaterialBuilder rotorStats(float speed, float damage, int durability) {
        builder.rotorStats(speed, damage, durability);
        return this;
    }

    public MaterialBuilder blastTemp(int temp) {
        builder.blastTemp(temp);
        return this;
    }

    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier) {
        builder.blastTemp(temp, gasTier);
        return this;
    }

    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
        builder.blastTemp(temp, gasTier, eutOverride);
        return this;
    }

    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
        builder.blastTemp(temp, gasTier, eutOverride, durationOverride);
        return this;
    }

    public MaterialBuilder ore() {
        builder.ore();
        return this;
    }

    public MaterialBuilder ore(boolean emissive) {
        builder.ore(emissive);
        return this;
    }

    public MaterialBuilder ore(int oreMultiplier, int byproductMultiplier) {
        builder.ore(oreMultiplier, byproductMultiplier);
        return this;
    }

    public MaterialBuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
        builder.ore(oreMultiplier, byproductMultiplier, emissive);
        return this;
    }

    public MaterialBuilder fluidTemp(int temp) {
        builder.fluidTemp(temp);
        return this;
    }

    public MaterialBuilder fluidCustomTexture() {
        builder.fluidCustomTexture();
        return this;
    }

    public MaterialBuilder fluidCustomTexture(ResourceLocation still, ResourceLocation flow) {
        builder.fluidCustomTexture(still, flow);
        return this;
    }

    public MaterialBuilder plasmaCustomTexture() {
        builder.plasmaCustomTexture();
        return this;
    }

    public MaterialBuilder plasmaCustomTexture(ResourceLocation still, ResourceLocation flow) {
        builder.plasmaCustomTexture(still, flow);
        return this;
    }

    public MaterialBuilder plasmaTinted(boolean tinted) {
        builder.plasmaTinted(tinted);
        return this;
    }

    public MaterialBuilder washedIn(Material m) {
        builder.washedIn(m);
        return this;
    }

    public MaterialBuilder washedIn(Material m, int washedAmount) {
        builder.washedIn(m, washedAmount);
        return this;
    }

    public MaterialBuilder separatedInto(Material... m) {
        builder.separatedInto(m);
        return this;
    }

    public MaterialBuilder oreSmeltInto(Material m) {
        builder.oreSmeltInto(m);
        return this;
    }

    public MaterialBuilder polarizesInto(Material m) {
        builder.polarizesInto(m);
        return this;
    }

    public MaterialBuilder arcSmeltInto(Material m) {
        builder.arcSmeltInto(m);
        return this;
    }

    public MaterialBuilder macerateInto(Material m) {
        builder.macerateInto(m);
        return this;
    }

    public MaterialBuilder ingotSmeltInto(Material m) {
        builder.ingotSmeltInto(m);
        return this;
    }

    public MaterialBuilder addOreByproducts(Material... byproducts) {
        builder.addOreByproducts(byproducts);
        return this;
    }

    public MaterialBuilder cableProperties(long voltage, int amperage, int loss) {
        return cableProperties((int) voltage, amperage, loss, false);
    }

    public MaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
        builder.cableProperties(voltage, amperage, loss, isSuperCon);
        return this;
    }

    public MaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature) {
        builder.cableProperties(voltage, amperage, loss, isSuperCon, criticalTemperature);
        return this;
    }

    public MaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
    }

    public MaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        builder.fluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof);
        return this;
    }

    public MaterialBuilder itemPipeProperties(int priority, float stacksPerSec) {
        builder.itemPipeProperties(priority, stacksPerSec);
        return this;
    }

    // TODO Clean this up post 2.5 release
    @Deprecated
    public MaterialBuilder addDefaultEnchant(Enchantment enchant, int level) {
        builder.addDefaultEnchant(enchant, level);
        return this;
    }

    @Override
    public RegistryObjectBuilderTypes<? super Material> getRegistryType() {
        return GregTechKubeJSPlugin.MATERIAL;
    }

    @Override
    public Material createObject() {
        return builder.buildAndRegister();
    }
}
