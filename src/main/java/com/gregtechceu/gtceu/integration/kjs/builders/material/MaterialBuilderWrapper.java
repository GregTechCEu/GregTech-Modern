package com.gregtechceu.gtceu.integration.kjs.builders.material;

import com.gregtechceu.gtceu.api.fluid.FluidBuilder;
import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.material.Element;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.material.material.properties.*;
import com.gregtechceu.gtceu.api.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class MaterialBuilderWrapper extends BuilderBase<Material> {

    private final Material.Builder internal;

    public MaterialBuilderWrapper(ResourceLocation id) {
        super(id);
        this.internal = new Material.Builder(id);
    }

    /*
     * Material Types
     */

    @Info("""
            Add a `FluidProperty` to this Material.
            Will be created as a `FluidStorageKeys#LIQUID`, without a Fluid Block.
            """)
    public MaterialBuilderWrapper fluid() {
        internal.fluid();
        return this;
    }

    @Info("""
            Add a `FluidProperty` to this Material.
            Will be created with the specified state a with standard `FluidBuilder` defaults.

            Can be called multiple times to add multiple fluids.
            """)
    public MaterialBuilderWrapper fluid(@NotNull FluidStorageKey key, @NotNull FluidState state) {
        internal.fluid(key, state);
        return this;
    }

    @Info("""
            Add a `FluidProperty` to this Material.

            Can be called multiple times to add multiple fluids.
            """)
    public MaterialBuilderWrapper fluid(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        internal.fluid(key, builder);
        return this;
    }

    @Info("""
            Add a liquid for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper liquid() {
        internal.liquid();
        return this;
    }

    @Info("""
            Add a liquid for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper liquid(@NotNull FluidBuilder builder) {
        internal.liquid(builder);
        return this;
    }

    public MaterialBuilderWrapper liquid(int temp) {
        internal.liquid(temp);
        return this;
    }

    @Info("""
            Add a plasma for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper plasma() {
        internal.plasma();
        return this;
    }

    @Info("""
            Add a plasma for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper plasma(@NotNull FluidBuilder builder) {
        internal.plasma(builder);
        return this;
    }

    public MaterialBuilderWrapper plasma(int temp) {
        internal.plasma(temp);
        return this;
    }

    @Info("""
            Add a gas for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper gas() {
        internal.gas();
        return this;
    }

    @Info("""
            Add a gas for this material.

            @see #fluid(FluidStorageKey, FluidState)
            """)
    public MaterialBuilderWrapper gas(@NotNull FluidBuilder builder) {
        internal.gas(builder);
        return this;
    }

    public MaterialBuilderWrapper gas(int temp) {
        internal.gas(temp);
        return this;
    }

    @Info("""
            Add a `DustProperty` to this Material.
            Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
            """)
    public MaterialBuilderWrapper dust() {
        internal.dust();
        return this;
    }

    @Info(value = """
            Add a `DustProperty` to this Material.
            Will be created with no Burn Time (Furnace Fuel).
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper dust(int harvestLevel) {
        internal.dust(harvestLevel);
        return this;
    }

    @Info(value = """
            Add a `DustProperty` to this Material.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """),
                  @Param(name = "burnTime",
                         value = """
                                 The Burn Time (in ticks) of this Material as a Furnace Fuel.
                                 If this Material already had a Burn Time defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper dust(int harvestLevel, int burnTime) {
        internal.dust(harvestLevel, burnTime);
        return this;
    }

    @Info("""
            Add a `WoodProperty` to this Material.
            Useful for marking a Material as Wood for various additional behaviors.
            Will be created with a Harvest Level of 0, and a Burn Time of 300 (Furnace Fuel).
            """)
    public MaterialBuilderWrapper wood() {
        internal.wood();
        return this;
    }

    @Info(value = """
            Add a `WoodProperty` to this Material.
            Useful for marking a Material as Wood for various additional behaviors.
            Will be created with a Burn Time of 300 (Furnace Fuel).
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper wood(int harvestLevel) {
        internal.wood(harvestLevel);
        return this;
    }

    @Info(value = """
            Add a `WoodProperty` to this Material.
            Useful for marking a Material as Wood for various additional behaviors.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """),
                  @Param(name = "burnTime",
                         value = """
                                 The Burn Time (in ticks) of this Material as a Furnace Fuel.
                                 If this Material already had a Burn Time defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper wood(int harvestLevel, int burnTime) {
        internal.wood(harvestLevel, burnTime);
        return this;
    }

    @Info("""
            Add an `IngotProperty` to this Material.
            Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """)
    public MaterialBuilderWrapper ingot() {
        internal.ingot();
        return this;
    }

    @Info(value = """
            Add an `IngotProperty` to this Material.
            Will be created with no Burn Time (Furnace Fuel).
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper ingot(int harvestLevel) {
        internal.ingot(harvestLevel);
        return this;
    }

    @Info(value = """
            Add an `IngotProperty` to this Material.
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """),
                  @Param(name = "burnTime",
                         value = """
                                 The Burn Time (in ticks) of this Material as a Furnace Fuel.
                                 If this Material already had a Burn Time defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper ingot(int harvestLevel, int burnTime) {
        internal.ingot(harvestLevel, burnTime);
        return this;
    }

    @Info("""
            Add a `GemProperty` to this Material.
            Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """)
    public MaterialBuilderWrapper gem() {
        internal.gem();
        return this;
    }

    @Info(value = """
            Add a `GemProperty` to this Material.
            Will be created with no Burn Time (Furnace Fuel).
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper gem(int harvestLevel) {
        internal.gem(harvestLevel);
        return this;
    }

    @Info(value = """
            Add a `GemProperty` to this Material.
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """),
                  @Param(name = "burnTime",
                         value = """
                                 The Burn Time (in ticks) of this Material as a Furnace Fuel.
                                 If this Material already had a Burn Time defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper gem(int harvestLevel, int burnTime) {
        internal.gem(harvestLevel, burnTime);
        return this;
    }

    @Info("""
            Add a `PolymerProperty` to this Material.
            Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            """)
    public MaterialBuilderWrapper polymer() {
        internal.polymer();
        return this;
    }

    @Info(value = """
            Add a `PolymerProperty` to this Material.
            Will automatically add a `DustProperty` to this Material if it does not already have one.
            Will have a burn time of 0
            """,
          params = {
                  @Param(name = "harvestLevel",
                         value = """
                                 The Harvest Level of this block for Mining.
                                 If this Material also has a `ToolProperty`, this value will
                                 also be used to determine the tool's Mining level.
                                 If this Material already had a Harvest Level defined, it will be overridden.
                                 """)
          })
    public MaterialBuilderWrapper polymer(int harvestLevel) {
        internal.polymer(harvestLevel);
        return this;
    }

    public MaterialBuilderWrapper burnTime(int burnTime) {
        internal.burnTime(burnTime);
        return this;
    }

    @Info("""
            Set the Color of this Material.
            Defaults to 0xFFFFFF unless `MaterialBuilderWrapper#colorAverage()` was called, where
            it will be a weighted average of the components of the Material.

            @param color The RGB-formatted Color.
            """)
    public MaterialBuilderWrapper color(int color) {
        internal.color(color);
        return this;
    }

    @Info("""
            Set the Color of this Material.
            Defaults to 0xFFFFFF unless `MaterialBuilderWrapper#colorAverage()` was called, where
            it will be a weighted average of the components of the Material.

            @param color         The RGB-formatted Color.
            @param hasFluidColor Whether the fluid should be colored or not.
            """)
    public MaterialBuilderWrapper color(int color, boolean hasFluidColor) {
        internal.color(color, hasFluidColor);
        return this;
    }

    @Info("""
            Set the secondary color of this Material.
            Defaults to 0xFFFFFF unless `MaterialBuilderWrapper#colorAverage()` was called, where
            it will be a weighted average of the components of the Material.

            @param color The RGB-formatted Color.
            """)
    public MaterialBuilderWrapper secondaryColor(int color) {
        internal.secondaryColor(color);
        return this;
    }

    public MaterialBuilderWrapper colorAverage() {
        internal.colorAverage();
        return this;
    }

    @Info(value = """
            Set the `MaterialIconSet` of this Material.
            Defaults vary depending on if the Material has a:
            `GemProperty`, it will default to `MaterialIconSet#GEM_VERTICAL`
            `IngotProperty` or `@link DustProperty`, it will default to `MaterialIconSet#DULL`
            `FluidProperty`, it will default to `MaterialIconSet#FLUID`
            </ul>
            Default will be determined by first-found Property in this order, unless specified.
            """,
          params = {
                  @Param(name = "iconSet", value = "The `MaterialIconSet` of this Material.")
          })
    public MaterialBuilderWrapper iconSet(MaterialIconSet iconSet) {
        internal.iconSet(iconSet);
        return this;
    }

    public MaterialBuilderWrapper components(MaterialStackWrapper... components) {
        internal.kjs$components(components);
        return this;
    }

    @Info("""
            Add `MaterialFlags` to this Material.
            Dependent Flags (for example, `MaterialFlags#GENERATE_LONG_ROD` requiring
            `MaterialFlags#GENERATE_ROD`) will be automatically applied.
            """)
    public MaterialBuilderWrapper flags(MaterialFlag... flags) {
        internal.flags(flags);
        return this;
    }

    @Info(value = """
            Add `MaterialFlags` to this Material.
            Dependent Flags (for example, `MaterialFlags#GENERATE_LONG_ROD` requiring
            `MaterialFlags#GENERATE_ROD`) will be automatically applied.
            """,
          params = {
                  @Param(name = "f1",
                         value = "A `Collection` of `MaterialFlag`. Provided this way for easy Flag presets to be applied."),
                  @Param(name = "f2",
                         value = "An Array of `MaterialFlag`. If no `Collection` is required, use `MaterialBuilderWrapper#flags(MaterialFlag...)`.")
          })
    // rename for kjs conflicts
    public MaterialBuilderWrapper appendFlags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        internal.appendFlags(f1, f2);
        return this;
    }

    @Info("""
            Added `TagPrefix` to be ignored by this Material.
            """)
    public MaterialBuilderWrapper ignoredTagPrefixes(TagPrefix... prefixes) {
        internal.ignoredTagPrefixes(prefixes);
        return this;
    }

    public MaterialBuilderWrapper element(Element element) {
        internal.element(element);
        return this;
    }

    public MaterialBuilderWrapper formula(String formula) {
        internal.formula(formula);
        return this;
    }

    @Info("""
            Replaced the old toolStats methods which took many parameters.
            Use `ToolProperty.Builder` instead to create a Tool Property.
            """)
    public MaterialBuilderWrapper toolStats(ToolProperty toolProperty) {
        internal.toolStats(toolProperty);
        return this;
    }

    public MaterialBuilderWrapper rotorStats(int power, int efficiency, float damage, int durability) {
        internal.rotorStats(power, efficiency, damage, durability);
        return this;
    }

    public MaterialBuilderWrapper blastTemp(int temp) {
        internal.blast(temp);
        return this;
    }

    public MaterialBuilderWrapper blast(int temp) {
        internal.blast(temp);
        return this;
    }

    public MaterialBuilderWrapper blast(int temp, BlastProperty.GasTier gasTier) {
        internal.blast(temp, gasTier);
        return this;
    }

    public MaterialBuilderWrapper blast(UnaryOperator<BlastProperty.Builder> b) {
        internal.blast(b);
        return this;
    }

    // Tons of shortcut functions for adding various hazard effects.

    public MaterialBuilderWrapper removeHazard() {
        internal.removeHazard();
        return this;
    }

    public MaterialBuilderWrapper radioactiveHazard(float multiplier) {
        internal.radioactiveHazard(multiplier);
        return this;
    }

    public MaterialBuilderWrapper hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        internal.hazard(trigger, condition);
        return this;
    }

    public MaterialBuilderWrapper hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                                         float progressionMultiplier) {
        internal.hazard(trigger, condition, progressionMultiplier);
        return this;
    }

    public MaterialBuilderWrapper hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                                         float progressionMultiplier, boolean applyToDerivatives) {
        internal.hazard(trigger, condition, progressionMultiplier, applyToDerivatives);
        return this;
    }

    public MaterialBuilderWrapper hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                                         boolean applyToDerivatives) {
        internal.hazard(trigger, condition, applyToDerivatives);
        return this;
    }

    public MaterialBuilderWrapper ore() {
        internal.ore();
        return this;
    }

    public MaterialBuilderWrapper ore(boolean emissive) {
        internal.ore(emissive);
        return this;
    }

    public MaterialBuilderWrapper ore(int oreMultiplier, int byproductMultiplier) {
        internal.ore(oreMultiplier, byproductMultiplier);
        return this;
    }

    public MaterialBuilderWrapper ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
        internal.ore(oreMultiplier, byproductMultiplier, emissive);
        return this;
    }

    public MaterialBuilderWrapper washedIn(Material m) {
        internal.washedIn(m);
        return this;
    }

    public MaterialBuilderWrapper washedIn(Material m, int washedAmount) {
        internal.washedIn(m, washedAmount);
        return this;
    }

    public MaterialBuilderWrapper separatedInto(Material... m) {
        internal.separatedInto(m);
        return this;
    }

    public MaterialBuilderWrapper oreSmeltInto(Material m) {
        internal.oreSmeltInto(m);
        return this;
    }

    public MaterialBuilderWrapper polarizesInto(Material m) {
        internal.polarizesInto(m);
        return this;
    }

    public MaterialBuilderWrapper arcSmeltInto(Material m) {
        internal.arcSmeltInto(m);
        return this;
    }

    public MaterialBuilderWrapper macerateInto(Material m) {
        internal.macerateInto(m);
        return this;
    }

    public MaterialBuilderWrapper ingotSmeltInto(Material m) {
        internal.ingotSmeltInto(m);
        return this;
    }

    public MaterialBuilderWrapper addOreByproducts(Material... byproducts) {
        internal.addOreByproducts(byproducts);
        return this;
    }

    public MaterialBuilderWrapper cableProperties(long voltage, int amperage, int loss) {
        internal.cableProperties(voltage, amperage, loss);
        return this;
    }

    public MaterialBuilderWrapper cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
        internal.cableProperties(voltage, amperage, loss, isSuperCon);
        return this;
    }

    public MaterialBuilderWrapper cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
                                                  int criticalTemperature) {
        internal.cableProperties(voltage, amperage, loss, isSuperCon, criticalTemperature);
        return this;
    }

    public MaterialBuilderWrapper fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        internal.fluidPipeProperties(maxTemp, throughput, gasProof);
        return this;
    }

    public MaterialBuilderWrapper fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
                                                      boolean cryoProof, boolean plasmaProof) {
        internal.fluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof);
        return this;
    }

    public MaterialBuilderWrapper itemPipeProperties(int priority, float stacksPerSec) {
        internal.itemPipeProperties(priority, stacksPerSec);
        return this;
    }

    @Override
    public Material createObject() {
        return internal.buildAndRegister();
    }
}
