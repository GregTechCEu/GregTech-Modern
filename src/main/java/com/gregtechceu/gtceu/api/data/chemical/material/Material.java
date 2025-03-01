package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.HAZARD;

public class Material implements Comparable<Material> {

    /**
     * Basic Info of this Material.
     *
     * @see MaterialInfo
     */
    @NotNull
    @Getter
    private final MaterialInfo materialInfo;

    /**
     * Properties of this Material.
     *
     * @see MaterialProperties
     */
    @NotNull
    @Getter
    private final MaterialProperties properties;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    @NotNull
    private final MaterialFlags flags;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    private String calculateChemicalFormula() {
        if (chemicalFormula != null) return this.chemicalFormula;
        if (materialInfo.element != null) {
            String[] split = materialInfo.element.symbol().split("-");
            String result;
            if (split.length > 1) {
                split[1] = FormattingUtil.toSmallUpNumbers(split[1]);
                result = split[0] + split[1];
            } else result = materialInfo.element.symbol();
            return result;
        }
        if (!materialInfo.componentList.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialInfo.componentList)
                components.append(component.toString());
            return components.toString();
        }
        return "";
    }

    public String getChemicalFormula() {
        return chemicalFormula;
    }

    public Material setFormula(String formula) {
        return setFormula(formula, true);
    }

    public Material setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? FormattingUtil.toSmallDownNumbers(formula) : formula;
        return this;
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    public Material setComponents(MaterialStack... components) {
        this.materialInfo.setComponents(components);
        this.chemicalFormula = this.calculateChemicalFormula();
        return this;
    }

    private Material(@NotNull MaterialInfo materialInfo, @NotNull MaterialProperties properties,
                     @NotNull MaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;
        this.properties.setMaterial(this);
        verifyMaterial();
    }

    // thou shall not call
    protected Material(ResourceLocation resourceLocation) {
        materialInfo = new MaterialInfo(resourceLocation);
        materialInfo.iconSet = MaterialIconSet.DULL;
        properties = new MaterialProperties();
        flags = new MaterialFlags();
    }

    protected void registerMaterial() {
        GTCEuAPI.materialManager.getRegistry(getModid()).register(this);
    }

    public String getName() {
        return materialInfo.resourceLocation.getPath();
    }

    public String getModid() {
        return materialInfo.resourceLocation.getNamespace();
    }

    public void addFlags(MaterialFlag... flags) {
        if (!GTCEuAPI.materialManager.canModifyMaterials())
            throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
        this.flags.addFlags(flags).verify(this);
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean isElement() {
        return materialInfo.element != null;
    }

    @Nullable
    public Element getElement() {
        return materialInfo.element;
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    public boolean hasAnyOfFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).anyMatch(this::hasFlag);
    }

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() &&
                !hasFlag(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
                !hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !hasFlag(MaterialFlags.DISABLE_DECOMPOSITION)) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialInfo.componentList) {
                Material material = materialStack.material();
                onlyMetalMaterials &= material.hasProperty(PropertyKey.INGOT);
            }
            // allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING);
            } else {
                flags.addFlags(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING);
            }
        }
    }

    /**
     * Retrieves a fluid from the material.
     * Attempts to retrieve with {@link FluidProperty#getPrimaryKey()}, {@link FluidStorageKeys#LIQUID} and
     * {@link FluidStorageKeys#GAS}.
     *
     * @return the fluid
     * @see #getFluid(FluidStorageKey)
     */
    public Fluid getFluid() {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        Fluid fluid = prop.get(prop.getPrimaryKey());
        if (fluid != null) return fluid;

        fluid = getFluid(FluidStorageKeys.LIQUID);
        if (fluid != null) return fluid;

        return getFluid(FluidStorageKeys.GAS);
    }

    /**
     * @param key the key for the fluid
     * @return the fluid corresponding with the key
     */
    public Fluid getFluid(@NotNull FluidStorageKey key) {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        return prop.get(key);
    }

    /**
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     * @see #getFluid(FluidStorageKey, int)
     */
    public FluidStack getFluid(int amount) {
        return new FluidStack(getFluid(), amount);
    }

    /**
     * @param key    the key for the fluid
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     */
    public FluidStack getFluid(@NotNull FluidStorageKey key, int amount) {
        return new FluidStack(getFluid(key), amount);
    }

    /**
     * @return a {@code TagKey<Fluid>} with the material's name as the tag key
     * @see #getFluid(FluidStorageKey, int)
     */
    public TagKey<Fluid> getFluidTag() {
        return TagUtil.createFluidTag(this.getName());
    }

    /**
     * Retrieves a fluid builder from the material.
     * <br/>
     * NOTE: only available before the fluids are registered.
     * <br/>
     * Attempts to retrieve with {@link FluidProperty#getPrimaryKey()}, {@link FluidStorageKeys#LIQUID} and
     * {@link FluidStorageKeys#GAS}.
     *
     * @return the fluid builder
     */
    public FluidBuilder getFluidBuilder() {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        FluidStorageKey key = prop.getPrimaryKey();
        FluidBuilder fluid = null;

        if (key != null) fluid = prop.getStorage().getQueuedBuilder(key);
        if (fluid != null) return fluid;

        fluid = getFluidBuilder(FluidStorageKeys.LIQUID);
        if (fluid != null) return fluid;

        return getFluidBuilder(FluidStorageKeys.GAS);
    }

    /**
     * NOTE: only available before the fluids are registered.
     *
     * @param key the key for the fluid
     * @return the fluid corresponding with the key
     */
    public FluidBuilder getFluidBuilder(@NotNull FluidStorageKey key) {
        FluidProperty prop = getProperty(PropertyKey.FLUID);
        if (prop == null) {
            throw new IllegalArgumentException("Material " + getResourceLocation() + " does not have a Fluid!");
        }

        return prop.getStorage().getQueuedBuilder(key);
    }

    public MaterialToolTier getToolTier() {
        ToolProperty prop = getProperty(PropertyKey.TOOL);
        if (prop == null)
            throw new IllegalArgumentException("Material " + materialInfo.resourceLocation + " does not have a tool!");
        return prop.getTier(this);
    }

    public Fluid getHotFluid() {
        AlloyBlastProperty prop = properties.getProperty(PropertyKey.ALLOY_BLAST);
        return prop == null ? null : prop.getFluid();
    }

    public FluidStack getHotFluid(int amount) {
        AlloyBlastProperty prop = properties.getProperty(PropertyKey.ALLOY_BLAST);
        return prop == null ? null : new FluidStack(prop.getFluid(), amount);
    }

    public Item getBucket() {
        Fluid fluid = getFluid();
        return fluid.getBucket();
    }

    public int getBlockHarvestLevel() {
        if (!hasProperty(PropertyKey.DUST))
            throw new IllegalArgumentException("Material " + materialInfo.resourceLocation +
                    " does not have a harvest level! Is probably a Fluid");
        int harvestLevel = getProperty(PropertyKey.DUST).getHarvestLevel();
        return harvestLevel > 0 ? harvestLevel - 1 : harvestLevel;
    }

    public int getToolHarvestLevel() {
        if (!hasProperty(PropertyKey.TOOL))
            throw new IllegalArgumentException("Material " + materialInfo.resourceLocation +
                    " does not have a tool harvest level! Is probably not a Tool Material");
        return getProperty(PropertyKey.TOOL).getHarvestLevel();
    }

    public void setMaterialARGB(int materialRGB) {
        materialInfo.colors.set(0, materialRGB);
    }

    public void setMaterialSecondaryARGB(int materialRGB) {
        materialInfo.colors.set(1, materialRGB);
    }

    public int getLayerARGB(int layerIndex) {
        // get 2nd digit as positive if emissive layer
        if (layerIndex < -100) {
            layerIndex = (Math.abs(layerIndex) % 100) / 10;
        }
        if (layerIndex > materialInfo.colors.size() - 1 || layerIndex < 0) return -1;
        int layerColor = getMaterialARGB(layerIndex);
        if (layerColor != -1 || layerIndex == 0) return layerColor;
        else return getMaterialARGB(0);
    }

    public int getMaterialARGB() {
        return materialInfo.colors.getInt(0) | 0xff000000;
    }

    public int getMaterialSecondaryARGB() {
        return materialInfo.colors.getInt(1) | 0xff000000;
    }

    /**
     * Gets a specific color layer in ARGB.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialARGB(int index) {
        return materialInfo.colors.getInt(index) | 0xff000000;
    }

    public int getMaterialRGB() {
        return materialInfo.colors.getInt(0);
    }

    /**
     * Gets a specific color layer.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialRGB(int index) {
        return materialInfo.colors.getInt(index);
    }

    public int getMaterialSecondaryRGB() {
        return materialInfo.colors.getInt(1);
    }

    public boolean hasFluidColor() {
        return materialInfo.hasFluidColor;
    }

    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        materialInfo.iconSet = materialIconSet;
    }

    public MaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    public boolean isRadioactive() {
        if (materialInfo.element != null)
            return materialInfo.element.halfLifeSeconds() >= 0;
        for (MaterialStack material : materialInfo.componentList)
            if (material.material().isRadioactive()) return true;
        return false;
    }

    public long getProtons() {
        if (materialInfo.element != null)
            return materialInfo.element.protons();
        if (materialInfo.componentList.isEmpty())
            return Math.max(1, 43);
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount();
            totalProtons += material.amount() * material.material().getProtons();
        }
        return totalProtons / totalAmount;
    }

    public long getNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.neutrons();
        if (materialInfo.componentList.isEmpty())
            return 55;
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount();
            totalNeutrons += material.amount() * material.material().getNeutrons();
        }
        return totalNeutrons / totalAmount;
    }

    public long getMass() {
        if (materialInfo.element != null)
            return materialInfo.element.mass();
        if (materialInfo.componentList.size() == 0)
            return 98;
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount();
            totalMass += material.amount() * material.material().getMass();
        }
        return totalMass / totalAmount;
    }

    public int getBlastTemperature() {
        BlastProperty prop = properties.getProperty(PropertyKey.BLAST);
        return prop == null ? 0 : prop.getBlastTemperature();
    }

    public String toCamelCaseString() {
        return FormattingUtil.lowerUnderscoreToUpperCamel(getName());
    }

    @NotNull
    public ResourceLocation getResourceLocation() {
        return materialInfo.resourceLocation;
    }

    public String getUnlocalizedName() {
        return materialInfo.resourceLocation.toLanguageKey("material");
    }

    public MutableComponent getLocalizedName() {
        return Component.translatable(getUnlocalizedName());
    }

    @Override
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    public String toString() {
        return materialInfo.resourceLocation.toString();
    }

    // must be named multiply for GroovyScript to allow `mat * quantity -> MaterialStack`
    public MaterialStack multiply(long amount) {
        return new MaterialStack(this, amount);
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return getProperty(key) != null;
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty property) {
        if (!GTCEuAPI.materialManager.canModifyMaterials()) {
            throw new IllegalStateException("Cannot add properties to a Material when registry is frozen!");
        }
        properties.setProperty(key, property);
        properties.verify();
    }

    public boolean isSolid() {
        return hasProperty(PropertyKey.INGOT) || hasProperty(PropertyKey.GEM);
    }

    public boolean hasFluid() {
        return hasProperty(PropertyKey.FLUID);
    }

    public void verifyMaterial() {
        properties.verify();
        flags.verify(this);
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    @RemapPrefixForJS("kjs$")
    public static class Builder extends BuilderBase<Material> {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;
        private Set<TagPrefix> ignoredTagPrefixes = null;

        private String formula = null;

        /*
         * The temporary list of components for this Material.
         */
        private List<MaterialStack> composition = new ArrayList<>();
        private List<MaterialStackWrapper> compositionSupplier;

        /*
         * Temporary value to use to determine how to calculate default RGB
         */
        private boolean averageRGB = false;

        /**
         * Constructs a {@link Material}. This Builder replaces the old constructors, and
         * no longer uses a class hierarchy, instead using a {@link MaterialProperties} system.
         *
         * @param resourceLocation The Name of this Material. Will be formatted as
         *                         "material.<name>" for the Translation Key.
         * @since GTCEu 2.0.0
         */
        public Builder(ResourceLocation resourceLocation) {
            super(resourceLocation);
            String name = resourceLocation.getPath();
            if (name.charAt(name.length() - 1) == '_')
                throw new IllegalArgumentException("Material name cannot end with a '_'!");
            materialInfo = new MaterialInfo(resourceLocation);
            properties = new MaterialProperties();
            flags = new MaterialFlags();
        }

        /*
         * Material Types
         */

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created as a {@link FluidStorageKeys#LIQUID}, without a Fluid Block.
         *
         * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
         */
        public Builder fluid() {
            fluid(FluidStorageKeys.LIQUID, new FluidBuilder());
            return this;
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created with the specified state a with standard {@link FluidBuilder} defaults.
         * <p>
         * Can be called multiple times to add multiple fluids.
         */
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidState state) {
            return fluid(key, new FluidBuilder().state(state));
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * <p>
         * Can be called multiple times to add multiple fluids.
         */
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
            properties.ensureSet(PropertyKey.FLUID);
            FluidProperty property = properties.getProperty(PropertyKey.FLUID);
            property.enqueueRegistration(key, builder);
            return this;
        }

        /**
         * Add a liquid for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder liquid() {
            return fluid(FluidStorageKeys.LIQUID, FluidState.LIQUID);
        }

        /**
         * Add a liquid for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder liquid(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.LIQUID, builder.state(FluidState.LIQUID));
        }

        public Builder liquid(int temp) {
            return liquid(new FluidBuilder().temperature(temp));
        }

        /**
         * Add a plasma for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder plasma() {
            return fluid(FluidStorageKeys.PLASMA, FluidState.PLASMA);
        }

        /**
         * Add a plasma for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder plasma(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.PLASMA, builder.state(FluidState.PLASMA));
        }

        public Builder plasma(int temp) {
            return plasma(new FluidBuilder().temperature(temp));
        }

        /**
         * Add a gas for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder gas() {
            return fluid(FluidStorageKeys.GAS, FluidState.GAS);
        }

        /**
         * Add a gas for this material.
         *
         * @see #fluid(FluidStorageKey, FluidState)
         */
        public Builder gas(@NotNull FluidBuilder builder) {
            return fluid(FluidStorageKeys.GAS, builder.state(FluidState.GAS));
        }

        public Builder gas(int temp) {
            return gas(new FluidBuilder().temperature(temp));
        }

        /**
         * Add a {@link DustProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
         *
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust() {
            properties.ensureSet(PropertyKey.DUST);
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
        public Builder dust(int harvestLevel) {
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
        public Builder dust(int harvestLevel, int burnTime) {
            properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            return this;
        }

        /**
         * Add a {@link WoodProperty} to this Material.<br>
         * Useful for marking a Material as Wood for various additional behaviors.<br>
         * Will be created with a Harvest Level of 0, and a Burn Time of 300 (Furnace Fuel).
         *
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder wood() {
            return wood(0, 300);
        }

        /**
         * Add a {@link WoodProperty} to this Material.<br>
         * Useful for marking a Material as Wood for various additional behaviors.<br>
         * Will be created with a Burn Time of 300 (Furnace Fuel).
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining Level.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder wood(int harvestLevel) {
            return wood(harvestLevel, 300);
        }

        /**
         * Add a {@link WoodProperty} to this Material.<br>
         * Useful for marking a Material as Wood for various additional behaviors.<br>
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining Level.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder wood(int harvestLevel, int burnTime) {
            properties.setProperty(PropertyKey.DUST, new DustProperty(harvestLevel, burnTime));
            properties.ensureSet(PropertyKey.WOOD);
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot() {
            properties.ensureSet(PropertyKey.INGOT);
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
         *                     diamonds.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel) {
            return ingot(harvestLevel, 0);
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest
         *                     diamonds.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
         *                     If this Material already had a Burn Time defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel, int burnTime) {
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
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
         */
        public Builder gem() {
            properties.ensureSet(PropertyKey.GEM);
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
        public Builder gem(int harvestLevel) {
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
        public Builder gem(int harvestLevel, int burnTime) {
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
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
         */
        public Builder polymer() {
            properties.ensureSet(PropertyKey.POLYMER);
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
        public Builder polymer(int harvestLevel) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) dust(harvestLevel, 0);
            else if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
            properties.ensureSet(PropertyKey.POLYMER);
            return this;
        }

        public Builder burnTime(int burnTime) {
            DustProperty prop = properties.getProperty(PropertyKey.DUST);
            if (prop == null) {
                dust();
                prop = properties.getProperty(PropertyKey.DUST);
            }
            prop.setBurnTime(burnTime);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color The RGB-formatted Color.
         */
        public Builder color(int color) {
            color(color, true);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color         The RGB-formatted Color.
         * @param hasFluidColor Whether the fluid should be colored or not.
         */
        public Builder color(int color, boolean hasFluidColor) {
            this.materialInfo.colors.set(0, color);
            this.materialInfo.hasFluidColor = hasFluidColor;
            return this;
        }

        /**
         * Set the secondary color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color The RGB-formatted Color.
         */
        public Builder secondaryColor(int color) {
            this.materialInfo.colors.set(1, color);
            return this;
        }

        public Builder colorAverage() {
            this.averageRGB = true;
            return this;
        }

        /**
         * Set the {@link MaterialIconSet} of this Material.<br>
         * Defaults vary depending on if the Material has a:<br>
         * <ul>
         * <li>{@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
         * <li>{@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
         * <li>{@link FluidProperty}, it will default to {@link MaterialIconSet#FLUID}
         * </ul>
         * Default will be determined by first-found Property in this order, unless specified.
         *
         * @param iconSet The {@link MaterialIconSet} of this Material.
         */
        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        public Builder components(Object... components) {
            Preconditions.checkArgument(
                    components.length % 2 == 0,
                    "Material Components list malformed!");

            for (int i = 0; i < components.length; i += 2) {
                if (components[i] == null) {
                    throw new IllegalArgumentException(
                            "Material in Components List is null for Material " + this.materialInfo.resourceLocation);
                }
                composition.add(new MaterialStack(
                        components[i] instanceof CharSequence chars ? GTMaterials.get(chars.toString()) :
                                (Material) components[i],
                        ((Number) components[i + 1]).longValue()));
            }
            return this;
        }

        public Builder componentStacks(MaterialStack... components) {
            composition = Arrays.asList(components);
            return this;
        }

        public Builder componentStacks(ImmutableList<MaterialStack> components) {
            composition = components;
            return this;
        }

        public Builder kjs$components(MaterialStackWrapper... components) {
            compositionSupplier = Arrays.asList(components);
            return this;
        }

        public Builder kjs$components(ImmutableList<MaterialStackWrapper> components) {
            compositionSupplier = components;
            return this;
        }

        /**
         * Add {@link MaterialFlags} to this Material.<br>
         * Dependent Flags (for example, {@link MaterialFlags#GENERATE_LONG_ROD} requiring
         * {@link MaterialFlags#GENERATE_ROD}) will be automatically applied.
         */
        public Builder flags(MaterialFlag... flags) {
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
         *           {@link Builder#flags(MaterialFlag...)}.
         */
        // rename for kjs conflicts
        public Builder appendFlags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
            this.flags.addFlags(f1.toArray(new MaterialFlag[0]));
            this.flags.addFlags(f2);
            return this;
        }

        /**
         * Add {@link TagPrefixes} to be ignored by this Material.<br>
         */
        public Builder ignoredTagPrefixes(TagPrefix... prefixes) {
            if (this.ignoredTagPrefixes == null) {
                this.ignoredTagPrefixes = new HashSet<>();
            }
            this.ignoredTagPrefixes.addAll(Arrays.asList(prefixes));
            return this;
        }

        public Builder element(Element element) {
            this.materialInfo.element = element;
            return this;
        }

        public Builder formula(String formula) {
            this.formula = formula;
            return this;
        }

        /**
         * Replaced the old toolStats methods which took many parameters.
         * Use {@link ToolProperty.Builder} instead to create a Tool Property.
         */
        public Builder toolStats(ToolProperty toolProperty) {
            properties.setProperty(PropertyKey.TOOL, toolProperty);
            return this;
        }

        public Builder rotorStats(int power, int efficiency, float damage, int durability) {
            properties.setProperty(PropertyKey.ROTOR, new RotorProperty(power, efficiency, damage, durability));
            return this;
        }

        public Builder blastTemp(int temp) {
            return blast(temp);
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier) {
            return blast(temp, gasTier);
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
            return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride));
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride, durationOverride));
        }

        public Builder blast(int temp) {
            properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp));
            return this;
        }

        public Builder blast(int temp, BlastProperty.GasTier gasTier) {
            properties.setProperty(PropertyKey.BLAST, new BlastProperty(temp, gasTier));
            return this;
        }

        public Builder blast(UnaryOperator<BlastProperty.Builder> b) {
            properties.setProperty(PropertyKey.BLAST, b.apply(new BlastProperty.Builder()).build());
            return this;
        }

        // Tons of shortcut functions for adding various hazard effects.

        public Builder removeHazard() {
            properties.setProperty(HAZARD,
                    new HazardProperty(HazardProperty.HazardTrigger.NONE, GTMedicalConditions.NONE,
                            0, false));
            return this;
        }

        public Builder radioactiveHazard(float multiplier) {
            properties.setProperty(HAZARD, new HazardProperty(HazardProperty.HazardTrigger.ANY,
                    GTMedicalConditions.CARCINOGEN, multiplier, true));
            return this;
        }

        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, 1, false));
            return this;
        }

        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              float progressionMultiplier) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, progressionMultiplier, false));
            return this;
        }

        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              float progressionMultiplier, boolean applyToDerivatives) {
            properties.setProperty(HAZARD,
                    new HazardProperty(trigger, condition, progressionMultiplier, applyToDerivatives));
            return this;
        }

        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              boolean applyToDerivatives) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, 1, applyToDerivatives));
            return this;
        }

        public Builder ore() {
            properties.ensureSet(PropertyKey.ORE);
            return this;
        }

        public Builder ore(boolean emissive) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(1, 1, emissive));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            properties.setProperty(PropertyKey.ORE, new OreProperty(oreMultiplier, byproductMultiplier, emissive));
            return this;
        }

        public Builder washedIn(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m);
            return this;
        }

        public Builder washedIn(Material m, int washedAmount) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m, washedAmount);
            return this;
        }

        public Builder separatedInto(Material... m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setSeparatedInto(m);
            return this;
        }

        public Builder oreSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setDirectSmeltResult(m);
            return this;
        }

        public Builder polarizesInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMagneticMaterial(m);
            return this;
        }

        public Builder arcSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setArcSmeltingInto(m);
            return this;
        }

        public Builder macerateInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMacerateInto(m);
            return this;
        }

        public Builder ingotSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setSmeltingInto(m);
            return this;
        }

        public Builder addOreByproducts(Material... byproducts) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setOreByProducts(byproducts);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss) {
            cableProperties(voltage, amperage, loss, false);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
            properties.ensureSet(PropertyKey.DUST);
            properties.setProperty(PropertyKey.WIRE, new WireProperties(voltage, amperage, loss, isSuperCon));
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
                                       int criticalTemperature) {
            properties.ensureSet(PropertyKey.DUST);
            properties.setProperty(PropertyKey.WIRE,
                    new WireProperties(voltage, amperage, loss, isSuperCon, criticalTemperature));
            return this;
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
            return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
                                           boolean cryoProof, boolean plasmaProof) {
            properties.setProperty(PropertyKey.FLUID_PIPE,
                    new FluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            return this;
        }

        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.setProperty(PropertyKey.ITEM_PIPE, new ItemPipeProperties(priority, stacksPerSec));
            return this;
        }

        @Deprecated
        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (!properties.hasProperty(PropertyKey.TOOL)) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.getProperty(PropertyKey.TOOL).addEnchantmentForTools(enchant, level);
            return this;
        }

        @HideFromJS
        public Material buildAndRegister() {
            materialInfo.componentList = composition.isEmpty() && this.compositionSupplier != null ?
                    ImmutableList.copyOf(compositionSupplier.stream().map(MaterialStackWrapper::toMatStack)
                            .toArray(MaterialStack[]::new)) :
                    ImmutableList.copyOf(composition);
            if (!properties.hasProperty(HAZARD)) {
                for (MaterialStack materialStack : materialInfo.componentList) {
                    Material material = materialStack.material();
                    if (material.hasProperty(HAZARD) && material.getProperty(HAZARD).applyToDerivatives) {
                        properties.setProperty(HAZARD, material.getProperty(HAZARD));
                        break;
                    }
                }
            }
            if (properties.hasProperty(HAZARD) &&
                    properties.getProperty(HAZARD).hazardTrigger == HazardProperty.HazardTrigger.NONE) {
                properties.removeProperty(HAZARD);
            }

            var mat = new Material(materialInfo, properties, flags);
            if (formula != null) {
                mat.setFormula(formula);
            }
            materialInfo.verifyInfo(properties, averageRGB);
            mat.registerMaterial();
            if (ignoredTagPrefixes != null) {
                ignoredTagPrefixes.forEach(p -> p.setIgnored(mat));
            }
            return mat;
        }

        @Override
        @HideFromJS
        public Material register() {
            return value = buildAndRegister();
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    @Accessors(chain = true)
    private static class MaterialInfo {

        /**
         * The modid and unlocalized name of this Material.
         * <p>
         * Required.
         */
        private final ResourceLocation resourceLocation;

        /**
         * The colors of this Material.
         * if any past index 0 are -1, they aren't used.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        @Getter
        @Setter
        private IntList colors = new IntArrayList(List.of(-1, -1));

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        @Getter
        @Setter
        private boolean hasFluidColor = true;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty.
         * - DULL if has DustProperty or IngotProperty.
         */
        @Getter
        @Setter
        private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        @Getter
        @Setter
        private ImmutableList<MaterialStack> componentList;

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        @Getter
        @Setter
        private Element element;

        private MaterialInfo(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }

        private void verifyInfo(MaterialProperties p, boolean averageRGB) {
            // Verify IconSet
            if (iconSet == null) {
                if (p.hasProperty(PropertyKey.GEM)) {
                    iconSet = MaterialIconSet.GEM_VERTICAL;
                } else if (p.hasProperty(PropertyKey.DUST) || p.hasProperty(PropertyKey.INGOT) ||
                        p.hasProperty(PropertyKey.POLYMER)) {
                            iconSet = MaterialIconSet.DULL;
                        } else
                    if (p.hasProperty(PropertyKey.FLUID)) {
                        iconSet = MaterialIconSet.FLUID;
                    } else iconSet = MaterialIconSet.DULL;
            }

            // Verify MaterialRGB
            if (colors.getInt(0) == -1) {
                if (!averageRGB || componentList.isEmpty())
                    colors.set(0, 0xFFFFFF);
                else {
                    long colorTemp = 0;
                    long divisor = 0;
                    for (MaterialStack stack : componentList) {
                        colorTemp += stack.material().getMaterialARGB() * stack.amount();
                        divisor += stack.amount();
                    }
                    colors.set(0, GTMath.saturatedCast(colorTemp / divisor));
                }
            }
        }

        public MaterialInfo setComponents(MaterialStack... components) {
            this.componentList = ImmutableList.copyOf(Arrays.stream(components).toList());
            return this;
        }
    }
}
