package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.DeferredMaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@RemapPrefixForJS("kjs$")
public class MaterialBuilder extends AbstractBuilder<Material, Material, GTRegistrate, MaterialBuilder> {

    private final Material.MaterialInfo materialInfo;
    private final MaterialProperties properties;
    private final MaterialFlags flags;

    private @Nullable HolderSet<TagPrefix> ignoredTagPrefixes = null;
    private final List<TagKey<Item>> itemTags = new ArrayList<>();

    /*
     * Temporary data used to determine the final material formula tooltip.
     */
    private @Nullable String formula = null;
    private boolean formatFormula = true;

    private List<DeferredMaterialStack> compositionSupplier = new ArrayList<>();

    /*
     * Temporary value to use to determine how to calculate default RGB.
     */
    private boolean averageRGB = false;

    /**
     * Constructs a {@link Material}. This Builder replaces the old constructors, and
     * no longer uses a class hierarchy, instead using a {@link MaterialProperties} system.
     *
     * @since GTCEu 2.0.0
     */
    public MaterialBuilder(GTRegistrate owner, String name, BuilderCallback callback) {
        super(owner, owner, name, callback, GTRegistries.Keys.MATERIAL);

        if (name.charAt(name.length() - 1) == '_') throw new IllegalArgumentException("Material name cannot end with a '_'!");
        materialInfo = new Material.MaterialInfo(getOwner().makeResourceLocation(name));
        properties = new MaterialProperties();
        flags = new MaterialFlags();
    }

    @Override
    public GTRegistrate getOwner() {
        return (GTRegistrate)super.getOwner();
    }

    /*
     * Material Types
     */

    /**
     * @see #liquid
     */
    public MaterialBuilder fluid() {
        fluid(FluidStorageKeys.LIQUID, new FluidBuilder());
        return this;
    }

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Will be created with the specified state and with standard {@link FluidBuilder} defaults.<br>
     * Can be called multiple times to add multiple fluids.
     * <br>
     * <br>
     * See {@link #fluid(FluidStorageKey, FluidBuilder)} for setting other values.
     */
    public MaterialBuilder fluid(FluidStorageKey key, FluidState state) {
        return fluid(key, new FluidBuilder().state(state));
    }

    /**
     * Add a {@link FluidProperty} to this Material.<br>
     * Can be called multiple times to add multiple fluids.
     *
     * @see FluidBuilder
     */
    public MaterialBuilder fluid(FluidStorageKey key, FluidBuilder builder) {
        properties.ensureSet(PropertyKey.FLUID).enqueueRegistration(key, builder);
        return this;
    }

    public MaterialBuilder primaryFluidKey(FluidStorageKey key) {
        if (properties.ensureSet(PropertyKey.FLUID).getQueuedBuilder(key) == null) throw new IllegalArgumentException("Cannot set %s as primary fluid key: Fluid for %s not registered.".formatted(key, key));
        properties.ensureSet(PropertyKey.FLUID).setPrimaryKey(key);
        return this;
    }

    /**
     * Add a liquid for this Material.
     * <br>
     * <br>
     * Created without a Fluid Block.<br>
     * Temperature will default to:
     * <ul>
     * <li>The EBF temperature of this Material, if it has a {@link BlastProperty}
     * <li><strong>1200K</strong>, if this Material has a {@link DustProperty}
     * <li><strong>293K</strong> otherwise
     * </ul>
     * <br>
     * See {@link #liquid(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#LIQUID LIQUID} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder liquid() {
        return fluid(FluidStorageKeys.LIQUID, FluidState.LIQUID);
    }

    /**
     * Add a liquid for this material.
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#LIQUID LIQUID} has
     *                                  already been added to this Material.
     * @see FluidBuilder
     */
    public MaterialBuilder liquid(FluidBuilder builder) {
        return fluid(FluidStorageKeys.LIQUID, builder.state(FluidState.LIQUID));
    }

    /**
     * Add a liquid for this Material.<br>
     * Created without a Fluid Block.
     * <br>
     * <br>
     * See {@link #liquid(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#LIQUID LIQUID} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder liquid(int temp) {
        return liquid(new FluidBuilder().temperature(temp));
    }

    /**
     * Add a plasma for this Material.
     * <br>
     * <br>
     * Temperature will default to:
     * <ul>
     * <li><strong>10,000K</strong> + the EBF temperature of this Material, if it has a {@link BlastProperty}
     * <li><strong>10,000K</strong> + the temperature of another fluid for this Material (liquid, then gas)
     * <li><strong>10,000K</strong> otherwise
     * </ul>
     * <br>
     * See {@link #plasma(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#PLASMA PLASMA} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder plasma() {
        return fluid(FluidStorageKeys.PLASMA, FluidState.PLASMA);
    }

    /**
     * Add a plasma for this material.
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#PLASMA PLASMA} has
     *                                  already been added to this Material.
     * @see FluidBuilder
     */
    public MaterialBuilder plasma(FluidBuilder builder) {
        return fluid(FluidStorageKeys.PLASMA, builder.state(FluidState.PLASMA));
    }

    /**
     * Add a liquid for this Material.
     * <br>
     * <br>
     * See {@link #plasma(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#PLASMA PLASMA} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder plasma(int temp) {
        return plasma(new FluidBuilder().temperature(temp));
    }

    /**
     * Add a gas for this Material.
     * <br>
     * <br>
     * Temperature will default to:
     * <ul>
     * <li><strong>100K</strong> + the EBF temperature of this Material, if it has a {@link BlastProperty}
     * <li><strong>293K</strong> otherwise
     * </ul>
     * <br>
     * See {@link #gas(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#GAS GAS} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder gas() {
        return fluid(FluidStorageKeys.GAS, FluidState.GAS);
    }

    /**
     * Add a gas for this material.
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#GAS GAS} has
     *                                  already been added to this Material.
     * @see FluidBuilder
     */
    public MaterialBuilder gas(FluidBuilder builder) {
        return fluid(FluidStorageKeys.GAS, builder.state(FluidState.GAS));
    }

    /**
     * Add a gas for this Material.
     * <br>
     * <br>
     * See {@link #gas(FluidBuilder)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link FluidStorageKeys#GAS GAS} has
     *                                  already been added to this Material.
     */
    public MaterialBuilder gas(int temp) {
        return gas(new FluidBuilder().temperature(temp));
    }

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * <br>
     * Sets Harvest Level to <strong>2</strong> if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #dust(int, int)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder dust() {
        properties.ensureSet(PropertyKey.DUST);
        return this;
    }

    /**
     * Add a {@link DustProperty} to this Material.<br>
     * <br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #dust(int, int)} for setting your own value(s).
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
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require an Iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
     *                     Diamonds.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder dust(int harvestLevel, int burnTime) {
        properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
        return this;
    }

    /**
     * Add a {@link WoodProperty} to this Material.<br>
     * Useful for marking a Material as Wood for various additional behaviors.
     * <br>
     * <br>
     * Sets Harvest Level to <strong>2</strong> if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     *
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder wood() {
        return wood(0, 300);
    }

    /**
     * Add a {@link WoodProperty} to this Material.<br>
     * Useful for marking a Material as Wood for various additional behaviors.
     * <br>
     * <br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require an Iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
     *                     Diamonds.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder wood(int harvestLevel) {
        return wood(harvestLevel, 300);
    }

    /**
     * Add a {@link WoodProperty} to this Material.<br>
     * Useful for marking a Material as Wood for various additional behaviors.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining Level.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
     * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
     */
    public MaterialBuilder wood(int harvestLevel, int burnTime) {
        properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
        properties.ensureSet(PropertyKey.WOOD);
        return this;
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Harvest Level to <strong>2</strong> if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #ingot(int, int)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material, or if
     *                                  an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot() {
        properties.ensureSet(PropertyKey.INGOT);
        return this;
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #ingot(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require an Iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
     *                     Diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material, or if
     *                                  an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot(int harvestLevel) {
        return ingot(harvestLevel, 0);
    }

    /**
     * Add an {@link IngotProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require an Iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
     *                     Diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material, or if
     *                                  an {@link IngotProperty} has already been added to this Material.
     */
    public MaterialBuilder ingot(int harvestLevel, int burnTime) {
        DustProperty prop = properties.getProperty(PropertyKey.DUST);
        if (prop == null) dust(harvestLevel, burnTime);
        else {
            if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
            if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
        }
        properties.ensureSet(PropertyKey.INGOT);
        return this;
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Harvest Level to <strong>2</strong> if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #gem(int, int)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material, or if
     *                                  a {@link GemProperty} has already been added to this Material.
     */
    public MaterialBuilder gem() {
        properties.ensureSet(PropertyKey.GEM);
        return this;
    }

    /**
     * Add a {@link GemProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Burn Time (Furnace Fuel) to 0 if not already set.
     * <br>
     * <br>
     * See {@link #gem(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require an Iron tool.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
     *                     Diamonds.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material, or if
     *                                  a {@link GemProperty} has already been added to this Material.
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
     * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material, or if
     *                                  a {@link GemProperty} has already been added to this Material.
     */
    public MaterialBuilder gem(int harvestLevel, int burnTime) {
        DustProperty prop = properties.getProperty(PropertyKey.DUST);
        if (prop == null) dust(harvestLevel, burnTime);
        else {
            if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
            if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
        }
        properties.ensureSet(PropertyKey.GEM);
        return this;
    }

    /**
     * Add a {@link PolymerProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Harvest Level to <strong>2</strong> if not already set.<br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #polymer(int, int)} for setting your own value(s).
     *
     * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
     */
    public MaterialBuilder polymer() {
        properties.ensureSet(PropertyKey.POLYMER);
        return this;
    }

    /**
     * Add a {@link PolymerProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     * <br>
     * <br>
     * Sets Burn Time (Furnace Fuel) to <strong>0</strong> if not already set.
     * <br>
     * <br>
     * See {@link #polymer(int, int)} for setting your own value(s).
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
     */
    public MaterialBuilder polymer(int harvestLevel) {
        return polymer(harvestLevel, 0);
    }

    /**
     * Add a {@link PolymerProperty} to this Material.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param harvestLevel The Harvest Level of this block for Mining.<br>
     *                     If this Material also has a {@link ToolProperty}, this value will
     *                     also be used to determine the tool's Mining level.<br>
     *                     If this Material already had a Harvest Level defined, it will be overridden.
     * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                     If this Material already had a Burn Time defined, it will be overridden.
     * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
     */
    public MaterialBuilder polymer(int harvestLevel, int burnTime) {
        DustProperty prop = properties.getProperty(PropertyKey.DUST);
        if (prop == null) dust(harvestLevel, burnTime);
        else if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
        properties.ensureSet(PropertyKey.POLYMER);
        return this;
    }

    /**
     * Set the burn time of this Material as a Furnace Fuel.<br>
     * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
     *
     * @param burnTime The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
     *                 If this Material already had a Burn Time defined, it will be overridden.
     */
    public MaterialBuilder burnTime(int burnTime) {
        DustProperty prop = properties.getProperty(PropertyKey.DUST);
        if (prop == null) {
            prop = properties.ensureSet(PropertyKey.DUST);
        }
        prop.setBurnTime(burnTime);
        return this;
    }

    /**
     * Set the Color of this Material.<br>
     * Defaults to <strong>0xFFFFFF</strong> unless {@link #colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     * <br>
     * <br>
     * Will automatically color the Fluid of the Material.
     * <br>
     * <br>
     * See {@link #color(int, boolean)} to set an override of the Fluid's color.
     *
     * @param color The RGB-formatted Color.
     */
    public MaterialBuilder color(int color) {
        color(color, true);
        return this;
    }

    /**
     * Set the Color of this Material.<br>
     * Defaults to <strong>0xFFFFFF</strong> unless {@link MaterialBuilder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     *
     * @param color         The RGB-formatted Color.
     * @param hasFluidColor Whether the fluid should be colored or not.
     */
    public MaterialBuilder color(int color, boolean hasFluidColor) {
        this.materialInfo.getColors().set(0, color);
        this.materialInfo.setHasFluidColor(hasFluidColor);
        return this;
    }

    /**
     * Set the secondary color of this Material.<br>
     * Defaults to <strong>0xFFFFFF</strong> unless {@link MaterialBuilder#colorAverage()} was called, where
     * it will be a weighted average of the components of the Material.
     *
     * @param color The RGB-formatted Color.
     */
    public MaterialBuilder secondaryColor(int color) {
        this.materialInfo.getColors().set(1, color);
        return this;
    }

    /**
     * Set the Color of this Material to be the average of the components specified in {@link #components}.<br>
     * Will default to <strong>0xFFFFFF</strong> if a components list is not specified.
     */
    public MaterialBuilder colorAverage() {
        this.averageRGB = true;
        return this;
    }

    /**
     * Set the {@link MaterialIconSet} of this Material.<br>
     * <br>
     * Defaults vary depending on if the Material has a:
     * <ul>
     * <li>{@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
     * <li>{@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
     * <li>{@link FluidProperty}, it will default to {@link MaterialIconSet#FLUID}
     * </ul>
     * <br>
     * Default will be determined by first-found Property in this order, unless specified.
     *
     * @param iconSet A holder containing the {@link MaterialIconSet} of this Material.
     */
    public MaterialBuilder iconSet(Holder<MaterialIconSet> iconSet) {
        materialInfo.setIconSet(iconSet);
        return this;
    }

    /**
     * Set the components that make up this Material.<br>
     * This information is used for automatic decomposition, chemical formula generation, among other things.
     *
     * @param components An Object array formed as pairs of Holder<Material> and Integer, representing the
     *                   Material and the amount of said Material in this Material's composition.
     * @throws IllegalArgumentException if the Object array is malformed.
     */
    @SuppressWarnings("unchecked")
    public MaterialBuilder components(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0,
                "Material Components list malformed!");

        Validate.noNullElements(components,
                "Material components array for %s had null element".formatted(getName()));

        for (int i = 0; i < components.length; i += 2) {
            compositionSupplier.add(new DeferredMaterialStack(((Holder<Material>)components[i])::value,
                    ((Number) components[i + 1]).longValue()));
        }
        return this;
    }

    /**
     * Set the components that make up this Material.<br>
     * This information is used for automatic decomposition, chemical formula generation, among other things.
     *
     * @param components An array of {@link DeferredMaterialStack}, each representing the
     *                   Material and the amount of said Material in this Material's composition.
     */
    public MaterialBuilder componentStacks(DeferredMaterialStack... components) {
        compositionSupplier = Arrays.asList(components);
        return this;
    }

    /**
     * Set the components that make up this Material.<br>
     * This information is used for automatic decomposition, chemical formula generation, among other things.
     *
     * @param components An {@link ImmutableList} of {@link DeferredMaterialStack}, each representing the
     *                   Material and the amount of said Material in this Material's composition.
     */
    public MaterialBuilder componentStacks(ImmutableList<DeferredMaterialStack> components) {
        compositionSupplier = components;
        return this;
    }

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     */
    public MaterialBuilder flags(MaterialFlag... flags) {
        this.flags.addFlags(flags);
        return this;
    }

    /**
     * Add {@link MaterialFlags} to this Material.<br>
     * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
     * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
     *
     * @param f1 A {@link Collection} of {@link MaterialFlag}. Provided this way for easy Flag presets to be
     *           applied.
     * @param f2 An Array of {@link MaterialFlag}. If no {@link Collection} is required, use
     *           {@link MaterialBuilder#flags(MaterialFlag...)}.
     */
    // rename for kjs conflicts
    public MaterialBuilder appendFlags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        this.flags.addFlags(f1.toArray(new MaterialFlag[0]));
        this.flags.addFlags(f2);
        return this;
    }

    /**
     * Remove specific Items from this Material.
     *
     * @param prefixes The list of prefixes to ignore.
     */
    @SafeVarargs
    public final MaterialBuilder ignoredTagPrefixes(Holder<TagPrefix>... prefixes) {
        List<Holder<TagPrefix>> ignored = new ArrayList<>(Arrays.asList(prefixes));
        if (ignoredTagPrefixes != null) ignored.addAll(this.ignoredTagPrefixes.stream().toList());
        ignoredTagPrefixes = HolderSet.direct(ignored);
        return this;
    }

    /**
     * Add a custom Item Tag to all items made from this Material.
     *
     * @param key The tag to add.
     */
    public MaterialBuilder customTags(TagKey<Item> key) {
        this.itemTags.add(key);
        return this;
    }

    /**
     * Set the Element of this Material.<br>
     * Should be effectively singleton; each element should only have 1 Material claiming to represent it.
     *
     * @param element The {@link Element} that this Material represents.
     */
    public MaterialBuilder element(Holder<Element> element) {
        this.materialInfo.setElement(element);
        return this;
    }

    /**
     * Set the Formula of this Material.
     * <br>
     * <br>
     * Will override the automatically generated formula.<br>
     * Will automatically format numbers as subscripts.
     *
     * @param formula The formula for this Material.
     */
    public MaterialBuilder formula(String formula) {
        this.formula = formula;
        return this;
    }

    /**
     * Set the Formula of this Material.
     * <br>
     * <br>
     * Will override the automatically generated formula.<br>
     *
     * @param formula        The formula for this Material.
     * @param withFormatting Whether numbers should be formatted as subscripts.
     */
    public MaterialBuilder formula(String formula, boolean withFormatting) {
        this.formula = formula;
        this.formatFormula = withFormatting;
        return this;
    }

    /**
     * Add a {@link ToolProperty} to this Material.<br>
     * Adds GregTech and Vanilla-substitute tools to this Material.<br>
     * Will automatically add an {@link IngotProperty} to this Material if it does not already have one.
     *
     * @see ToolProperty.Builder
     */
    public MaterialBuilder toolStats(ToolProperty toolProperty) {
        properties.setProperty(PropertyKey.TOOL, toolProperty);
        return this;
    }

    /**
     * Add an {@link ArmorProperty} to this Material.<br>
     * Adds Armors to this Material.
     *
     * @see ArmorProperty.Builder
     */
    public MaterialBuilder armorStats(ArmorProperty armorProperty) {
        properties.setProperty(PropertyKey.ARMOR, armorProperty);
        return this;
    }

    /**
     * Adds a {@link RotorProperty} to this Material, generating Turbine Rotors.<br>
     * Will automatically add an {@link IngotProperty} to this Material if it does not already have one.
     *
     * @param power      The power of Turbine rotors made of this Material, used as a power multiplier with
     *                   the Rotor Holder's tier.
     * @param efficiency The efficiency of Turbine rotors made of this Material, used with the efficiency of the
     *                   Rotor Holder: <code>rotorEfficiency * holderEfficiency / 100</code>
     * @param damage     The damage running turbines with this Rotor should deal to the player when the Rotor
     *                   Holder UI is opened.
     * @param durability The durability of Turbine Rotors made of this Material.
     */
    // dear god please refactor me
    public MaterialBuilder rotorStats(int power, int efficiency, float damage, int durability) {
        properties.setProperty(PropertyKey.ROTOR, new RotorProperty(power, efficiency, damage, durability));
        return this;
    }

    /**
     * @see #blast(int)
     */
    public MaterialBuilder blastTemp(int temp) {
        return blast(temp);
    }

    /**
     * @see #blast(int, BlastProperty.GasTier)
     */
    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier) {
        return blast(temp, gasTier);
    }

    /**
     * @see #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;)
     */
    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
        return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride));
    }

    /**
     * @see #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;)
     */
    public MaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
        return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride, durationOverride));
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at 120 EU/t and a duration based off of the Material's composition.
     * <br>
     * <br>
     * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
     * Ingot.<br>
     * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
     * EBF recipe.
     * <br>
     * <br>
     * See {@link #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;)} for setting your own
     * value(s).
     *
     * @param temp The temperature of the recipe in the EBF.
     */
    public MaterialBuilder blast(int temp) {
        properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp));
        return this;
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at 120 EU/t and a duration based off of the Material's composition.
     * <br>
     * <br>
     * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
     * Ingot.<br>
     * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
     * EBF recipe.
     * <br>
     * <br>
     * See {@link #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;)} for setting your own
     * value(s).
     *
     * @param temp    The temperature of the recipe in the EBF.
     * @param gasTier The {@link BlastProperty.GasTier} of the Recipe. Will generate a second EBF recipe
     *                using the specified gas of the tier for a speed bonus.
     */
    public MaterialBuilder blast(int temp, BlastProperty.GasTier gasTier) {
        properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp, gasTier));
        return this;
    }

    /**
     * Add an EBF Temperature and recipe to this Material.<br>
     * Will generate a Dust -> Ingot EBF recipe at <strong>120 EU/t</strong> and a duration based off of the
     * Material's composition.
     * <br>
     * <br>
     * If the temperature is above <strong>1750K</strong>, it will automatically add a Vacuum Freezer recipe and Hot
     * Ingot.<br>
     * If the temperature is below <strong>1000K</strong>, it will automatically add a PBF recipe in addition to the
     * EBF recipe.
     * <br>
     * <br>
     * <p>
     * Sample usage:
     *
     * <pre>{@code
     *     .blast(b -> b
     *         .temp(1750)
     *         .blastStats(VA[HV], 300)
     *      )
     *      // ...
     * }</pre>
     */
    public MaterialBuilder blast(UnaryOperator<BlastProperty.Builder> b) {
        properties.setProperty(PropertyKey.BLAST, b.apply(new BlastProperty.Builder()).build());
        return this;
    }

    /**
     * Remove the Hazard from this Material.<br>
     * Useful when a component of this Material would automatically apply an undesired hazard.
     */
    public MaterialBuilder removeHazard() {
        properties.setProperty(PropertyKey.HAZARD,
                new HazardProperty(HazardProperty.HazardTrigger.NONE, GTMedicalConditions.NONE,
                        0, false));
        return this;
    }

    /**
     * Set a radioactive Hazard for this Material.<br>
     * Applies as a {@link GTMedicalConditions#CARCINOGEN carcinogenic} hazard with any trigger.
     * <br>
     * <br>
     * Overrides the Hazard if one was already set.
     *
     * @param multiplier Multiplier for how quickly the condition will progress.
     */
    public MaterialBuilder radioactiveHazard(float multiplier) {
        properties.setProperty(PropertyKey.HAZARD, new HazardProperty(HazardProperty.HazardTrigger.ANY,
                GTMedicalConditions.CARCINOGEN, multiplier, true));
        return this;
    }

    /**
     * Set a Hazard for this Material.
     * <br>
     * <br>
     * Overrides the Hazard if one was already set.<br>
     * Sets progression multiplier to <strong>1</strong>.<br>
     * Will not apply the Hazard to derivative materials, i.e. materials with this Material in its components list.
     *
     * @param trigger   The trigger type for this hazard.
     * @param condition The condition applied by this hazard.
     */
    public MaterialBuilder hazard(HazardProperty.HazardTrigger trigger, Holder<MedicalCondition> condition) {
        properties.setProperty(PropertyKey.HAZARD, new HazardProperty(trigger, condition, 1, false));
        return this;
    }

    /**
     * Set a Hazard for this Material.
     * <br>
     * <br>
     * Overrides the Hazard if one was already set.<br>
     * Will not apply the Hazard to derivative materials, i.e. materials with this Material in its components list.
     *
     * @param trigger               The trigger type for this hazard.
     * @param condition             The condition applied by this hazard.
     * @param progressionMultiplier Multiplier for how quickly the condition will progress.
     */
    public MaterialBuilder hazard(HazardProperty.HazardTrigger trigger, Holder<MedicalCondition> condition,
                                  float progressionMultiplier) {
        properties.setProperty(PropertyKey.HAZARD,
                new HazardProperty(trigger, condition, progressionMultiplier, false));
        return this;
    }

    /**
     * Set a Hazard for this Material.<br>
     * Overrides the Hazard if one was already set.
     *
     * @param trigger               The trigger type for this hazard.
     * @param condition             The condition applied by this hazard.
     * @param progressionMultiplier Multiplier for how quickly the condition will progress.
     * @param applyToDerivatives    Whether the Hazard should be applied to materials with this Material in its
     *                              components list.
     */
    public MaterialBuilder hazard(HazardProperty.HazardTrigger trigger, Holder<MedicalCondition> condition,
                                  float progressionMultiplier, boolean applyToDerivatives) {
        properties.setProperty(PropertyKey.HAZARD,
                new HazardProperty(trigger, condition, progressionMultiplier, applyToDerivatives));
        return this;
    }

    /**
     * Set a Hazard for this Material.
     * <br>
     * <br>
     * Overrides the Hazard if one was already set.<br>
     * Sets progression multiplier to <strong>1</strong>.
     *
     * @param trigger            The trigger type for this hazard.
     * @param condition          The condition applied by this hazard.
     * @param applyToDerivatives Whether the Hazard should be applied to materials with this Material in its
     *                           components list.
     */
    public MaterialBuilder hazard(HazardProperty.HazardTrigger trigger, Holder<MedicalCondition> condition,
                                  boolean applyToDerivatives) {
        properties.setProperty(PropertyKey.HAZARD, new HazardProperty(trigger, condition, 1, applyToDerivatives));
        return this;
    }

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br>
     * <br>
     * Sets Ore Multiplier to 1 if not already set.<br>
     * Sets Byproduct Multiplier to 1 if not already set.<br>
     * Sets Emissive Textures to false if not already set.
     * <br>
     * <br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     */
    public MaterialBuilder ore() {
        properties.ensureSet(PropertyKey.ORE);
        return this;
    }

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br>
     * <br>
     * Sets Ore Multiplier to 1 if not already set.<br>
     * Sets Byproduct Multiplier to 1 if not already set.
     * <br>
     * <br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     *
     * @param emissive Whether this Material's Ore Block should use emissive textures on the ore-vein texture
     *                 overlay.
     */
    public MaterialBuilder ore(boolean emissive) {
        properties.setProperty(PropertyKey.ORE, new OreProperty(1, 1, emissive));
        return this;
    }

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.<br>
     * <br>
     * Sets Emissive Textures to false if not already set.
     * <br>
     * <br>
     * See {@link #ore(int, int, boolean)} for setting your own value(s).
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     */
    public MaterialBuilder ore(int oreMultiplier, int byproductMultiplier) {
        properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier));
        return this;
    }

    /**
     * Add an {@link OreProperty} to this Material.<br>
     * Automatically adds a {@link DustProperty} to this Material.
     *
     * @param oreMultiplier       Crushed output multiplier when the Ore Block is macerated.
     * @param byproductMultiplier Byproduct multiplier on some ore processing steps.
     * @param emissive            Whether this Material's Ore Block should use emissive textures on the ore-vein
     *                            texture overlay.
     */
    public MaterialBuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
        properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
        return this;
    }

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore, using <strong>100L</strong> of the
     * Fluid.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Material that is used as a Chemical Bath fluid for ore processing.
     *          This Material will be given a {@link FluidProperty} if it does not already have one,
     *          of type LIQUID and no Fluid block.
     */
    public MaterialBuilder washedIn(Material m) {
        properties.ensureSet(PropertyKey.ORE).setWashedIn(m);
        return this;
    }

    /**
     * Adds a Chemical Bath ore processing step to this Material's Ore.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m            The Material that is used as a Chemical Bath fluid for ore processing.
     *                     This Material will be given a {@link FluidProperty} if it does not already have one,
     *                     of type LIQUID and no Fluid block.
     * @param washedAmount The amount of the above Fluid required to wash the Ore.
     */
    public MaterialBuilder washedIn(Material m, int washedAmount) {
        properties.ensureSet(PropertyKey.ORE).setWashedIn(m, washedAmount);
        return this;
    }

    /**
     * Adds an Electromagnetic Separator recipe to this Material's Purified Dust, which outputs the passed
     * Materials.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Materials which should be output by the Electromagnetic Separator in addition to a normal Dust
     *          of this Material.
     */
    public MaterialBuilder separatedInto(Material... m) {
        properties.ensureSet(PropertyKey.ORE).setSeparatedInto(m);
        return this;
    }

    /**
     * Sets the Material which this Material's Ore Block smelts to directly in a Furnace.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param m The Material which should be output when smelting.
     */
    public MaterialBuilder oreSmeltInto(Material m) {
        properties.ensureSet(PropertyKey.ORE).setDirectSmeltResult(m);
        return this;
    }

    /**
     * Adds a Polarizer recipe to this Material's metal parts, outputting the provided Material.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material will be polarized into.
     */
    public MaterialBuilder polarizesInto(Holder<Material> m) {
        properties.ensureSet(PropertyKey.INGOT).setMagneticMaterial(m);
        return this;
    }

    /**
     * Sets the Material that this Material will automatically transform into in any Arc Furnace recipe.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m A {@link Holder} containing the Material that this Material will turn into in any Arc Furnace recipes.
     */
    public MaterialBuilder arcSmeltInto(Holder<Material> m) {
        properties.ensureSet(PropertyKey.INGOT).setArcSmeltingInto(m);
        return this;
    }

    /**
     * Sets the Material that this Material's Ingot should macerate directly into.<br>
     * A good example is Magnetic Iron, which when macerated, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material's Ingot should macerate directly into.
     */
    public MaterialBuilder macerateInto(Holder<Material> m) {
        properties.ensureSet(PropertyKey.INGOT).setMacerateInto(m);
        return this;
    }

    /**
     * Sets the Material that this Material's Ingot should smelt directly into in a Furnace.<br>
     * A good example is Magnetic Iron, which when smelted, will turn back into normal Iron.<br>
     * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
     * with a harvest level of 2 and no Furnace burn time (if not already set).
     *
     * @param m The Material that this Material's Ingot should smelt directly into.
     */
    public MaterialBuilder ingotSmeltInto(Holder<Material> m) {
        properties.ensureSet(PropertyKey.INGOT).setSmeltingInto(m);
        return this;
    }

    /**
     * Sets the Material that this Material's items can turn into in a polarizer.
     */
    public MaterialBuilder magneticMaterial(Holder<Material> m) {
        properties.ensureSet(PropertyKey.INGOT).setMagneticMaterial(m);
        return this;
    }

    /**
     * Adds Ore byproducts to this Material.<br>
     * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
     * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
     *
     * @param byproducts The list of Materials which serve as byproducts during ore processing.
     */
    public MaterialBuilder addOreByproducts(Material... byproducts) {
        properties.ensureSet(PropertyKey.ORE).setOreByProducts(byproducts);
        return this;
    }

    /**
     * Add Wires and Cables to this Material.
     *
     * @param voltage  The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage The amperage of this Cable. Should be greater than zero.
     * @param loss     The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     */
    public MaterialBuilder cableProperties(long voltage, int amperage, int loss) {
        cableProperties(voltage, amperage, loss, false);
        return this;
    }

    /**
     * Add Wires and/or Cables to this Material.
     *
     * @param voltage    The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage   The amperage of this Cable. Should be greater than zero.
     * @param loss       The loss-per-block of this Cable. A value of zero here will still have loss as wires.
     * @param isSuperCon Whether this Material is a Superconductor. If so, Cables will NOT be generated and
     *                   the Wires will have zero cable loss, ignoring the loss parameter.
     */
    public MaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
        properties.ensureSet(PropertyKey.DUST);
        properties.setProperty(PropertyKey.WIRE, new WireProperties(voltage, amperage, loss, isSuperCon));
        return this;
    }

    /**
     * Add Wires and/or Cables to this Material.
     *
     * @param voltage             The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
     * @param amperage            The amperage of this Cable. Should be greater than zero.
     * @param loss                The loss-per-block of this Cable. A value of zero here will still have loss as
     *                            wires.
     * @param isSuperCon          Whether this Material is a Superconductor. If so, Cables will NOT be generated and
     *                            the Wires will have zero cable loss, ignoring the loss parameter.
     * @param criticalTemperature The critical temperature of this Material's Wires, if it is a Superconductor.
     *                            Not currently utilized and intended for addons to use.
     */
    public MaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
                                           int criticalTemperature) {
        properties.ensureSet(PropertyKey.DUST);
        properties.setProperty(PropertyKey.WIRE,
                new WireProperties(voltage, amperage, loss, isSuperCon, criticalTemperature));
        return this;
    }

    /**
     * Add Fluid Pipes to this Material.
     *
     * @param maxTemp    The maximum temperature of Fluid that this Pipe can handle before causing damage to the
     *                   Pipe.
     * @param throughput The rate at which Fluid can flow through this Pipe.
     * @param gasProof   Whether this Pipe can hold Gases. If not, some Gas will be lost as it travels through the
     *                   Pipe.
     */
    public MaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
    }

    /**
     * Add Fluid Pipes to this Material.
     *
     * @param maxTemp     The maximum temperature of Fluid that this Pipe can handle before causing damage to the
     *                    Pipe.
     * @param throughput  The rate at which Fluid can flow through this Pipe.
     * @param gasProof    Whether this Pipe can hold Gases. If not, some Gas will be lost as it travels through the
     *                    Pipe.
     * @param acidProof   Whether this Pipe can hold Acids. If not, the Pipe may lose fluid or cause damage.
     * @param cryoProof   Whether this Pipe can hold Cryogenic Fluids (below 120K). If not, the Pipe may lose fluid
     *                    or cause damage.
     * @param plasmaProof Whether this Pipe can hold Plasmas. If not, the Pipe may lose fluid or cause damage.
     */
    public MaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
                                               boolean cryoProof, boolean plasmaProof) {
        properties.setProperty(PropertyKey.FLUID_PIPE,
                new FluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
        return this;
    }

    /**
     * Add Item Pipes to this Material.
     *
     * @param priority     Priority of this Item Pipe, used for the standard routing mode.
     * @param stacksPerSec How many stacks of items can be moved per second (20 ticks).
     */
    public MaterialBuilder itemPipeProperties(int priority, float stacksPerSec) {
        properties.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, stacksPerSec));
        return this;
    }

    /**
     * Specify a default enchantment for tools made from this Material to have upon creation.
     *
     * @param enchant The default enchantment to apply to all tools made from this Material.
     * @param level   The level that the enchantment starts at when created.
     */
    @Deprecated
    public MaterialBuilder addDefaultEnchant(ResourceKey<Enchantment> enchant, int level) {
        if (!properties.hasProperty(PropertyKey.TOOL)) // cannot assign default here
            throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
        Objects.requireNonNull(properties.getProperty(PropertyKey.TOOL)).addEnchantmentForTools(enchant, level);
        return this;
    }

    @Override
    protected MaterialRegistryEntry createEntryWrapper(DeferredHolder<Material, Material> delegate) {
        return new MaterialRegistryEntry(getOwner(), delegate);
    }

    public MaterialRegistryEntry register() {
        return (MaterialRegistryEntry)super.register();
    }

    @Override
    protected Material createEntry() {

        materialInfo.setComponents(compositionSupplier);

        if (!properties.hasProperty(PropertyKey.HAZARD)) {
            for (MaterialStack materialStack : materialInfo.getComponentList()) {
                Material material = materialStack.material();
                HazardProperty property = material.getProperty(PropertyKey.HAZARD);
                if (property != null && property.applyToDerivatives) {
                    properties.setProperty(PropertyKey.HAZARD, property);
                    break;
                }
            }
        }

        HazardProperty hazardProperty = properties.getProperty(PropertyKey.HAZARD);
        if (hazardProperty != null && hazardProperty.hazardTrigger == HazardProperty.HazardTrigger.NONE) {
            properties.removeProperty(PropertyKey.HAZARD);
        }

        var mat = new Material(materialInfo, properties, flags);
        if (!itemTags.isEmpty()) {
            mat.getItemTags().addAll(itemTags);
        }
        if (formula != null) {
            mat.setFormula(formula, formatFormula);
        }
        materialInfo.verifyInfo(properties, averageRGB);
        if (ignoredTagPrefixes != null) {
            ignoredTagPrefixes.forEach(p -> p.value().setIgnored(mat));
        }

        return mat;
    }
}
