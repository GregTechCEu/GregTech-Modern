package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.HAZARD;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

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
    @Getter
    private final MaterialFlags flags;

    /**
     * Chemical formula of this material
     */
    @Getter
    private String chemicalFormula;

    /**
     * Material specific tags
     */
    @Setter
    @Getter
    private List<TagKey<Item>> itemTags = new ArrayList<>();

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

    @ApiStatus.Internal
    public String getDefaultTranslation() {
        return materialInfo.overriddenName != null ? materialInfo.overriddenName : toEnglishName(getName());
    }

    public String getModid() {
        return materialInfo.resourceLocation.getNamespace();
    }

    /**
     * @param prefix the tagPrefix to check
     * @return if the material should have recipes autogenerated
     */
    public boolean shouldGenerateRecipesFor(@NotNull TagPrefix prefix) {
        return (!this.hasFlag(MaterialFlags.NO_UNIFICATION) ||
                !this.hasFlag(MaterialFlags.DISABLE_MATERIAL_RECIPES)) && !ChemicalHelper.get(prefix, this).isEmpty();
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
        // parse emissive layer value as -(layer + 101)
        if (layerIndex < -100) {
            layerIndex = -layerIndex - 101;
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
            if (material.isEmpty()) continue;
            totalAmount += material.amount();
            totalProtons += material.amount() * material.material().getProtons();
        }
        if (totalAmount == 0) return 0;
        return totalProtons / totalAmount;
    }

    public long getNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.neutrons();
        if (materialInfo.componentList.isEmpty())
            return 55;
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            if (material.isEmpty()) continue;
            totalAmount += material.amount();
            totalNeutrons += material.amount() * material.material().getNeutrons();
        }
        if (totalAmount == 0) return 0;
        return totalNeutrons / totalAmount;
    }

    public long getMass() {
        if (materialInfo.element != null)
            return materialInfo.element.mass();
        if (materialInfo.componentList.isEmpty())
            return 98;
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            if (material.isEmpty()) continue;
            totalAmount += material.amount();
            totalMass += material.amount() * material.material().getMass();
        }
        if (totalAmount == 0) return 0;
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

    // must be named multiply for GroovyScript to allow `material * quantity -> MaterialStack`
    public MaterialStack multiply(long amount) {
        return new MaterialStack(this, amount);
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return properties.hasProperty(key);
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty> void removeProperty(PropertyKey<T> key) {
        properties.removeProperty(key);
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

    public boolean isNull() {
        return this == GTMaterials.NULL;
    }

    @RemapPrefixForJS("kjs$")
    @SuppressWarnings("unused") // API, need to treat all of these as used
    public static class Builder extends BuilderBase<Material> {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;

        private Set<TagPrefix> ignoredTagPrefixes = null;
        private final List<TagKey<Item>> itemTags = new ArrayList<>();

        /*
         * Temporary data used to determine the final material formula tooltip.
         */
        private String formula = null;
        private boolean formatFormula = true;

        /*
         * The temporary list of components for this Material.
         */
        private List<MaterialStack> composition = new ArrayList<>();
        private List<MaterialStackWrapper> compositionSupplier;

        /*
         * Temporary value to use to determine how to calculate default RGB.
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

        /**
         * @param name Set the material's (US english) localized name to this value
         */
        public Builder langValue(String name) {
            materialInfo.setOverriddenName(name);
            return this;
        }

        /*
         * Material Types
         */

        /** @see #liquid */
        public Builder fluid() {
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
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidState state) {
            return fluid(key, new FluidBuilder().state(state));
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Can be called multiple times to add multiple fluids.
         *
         * @see FluidBuilder
         */
        public Builder fluid(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
            properties.ensureSet(PropertyKey.FLUID);
            FluidProperty property = properties.getProperty(PropertyKey.FLUID);
            property.enqueueRegistration(key, builder);
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
        public Builder liquid() {
            return fluid(FluidStorageKeys.LIQUID, FluidState.LIQUID);
        }

        /**
         * Add a liquid for this material.
         * 
         * @throws IllegalArgumentException If a {@link FluidStorageKeys#LIQUID LIQUID} has
         *                                  already been added to this Material.
         *
         * @see FluidBuilder
         */
        public Builder liquid(@NotNull FluidBuilder builder) {
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
        public Builder liquid(int temp) {
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
        public Builder plasma() {
            return fluid(FluidStorageKeys.PLASMA, FluidState.PLASMA);
        }

        /**
         * Add a plasma for this material.
         * 
         * @throws IllegalArgumentException If a {@link FluidStorageKeys#PLASMA PLASMA} has
         *                                  already been added to this Material.
         *
         * @see FluidBuilder
         */
        public Builder plasma(@NotNull FluidBuilder builder) {
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
        public Builder plasma(int temp) {
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
        public Builder gas() {
            return fluid(FluidStorageKeys.GAS, FluidState.GAS);
        }

        /**
         * Add a gas for this material.
         * 
         * @throws IllegalArgumentException If a {@link FluidStorageKeys#GAS GAS} has
         *                                  already been added to this Material.
         *
         * @see FluidBuilder
         */
        public Builder gas(@NotNull FluidBuilder builder) {
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
        public Builder gas(int temp) {
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
        public Builder dust() {
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
        public Builder dust(int harvestLevel) {
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
        public Builder dust(int harvestLevel, int burnTime) {
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
        public Builder wood() {
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
        public Builder wood(int harvestLevel) {
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
        public Builder wood(int harvestLevel, int burnTime) {
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
        public Builder ingot() {
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
        public Builder ingot(int harvestLevel) {
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
        public Builder gem() {
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
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material, or if
         *                                  a {@link GemProperty} has already been added to this Material.
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
        public Builder polymer() {
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
        public Builder polymer(int harvestLevel) {
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
        public Builder polymer(int harvestLevel, int burnTime) {
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
        public Builder color(int color) {
            color(color, true);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to <strong>0xFFFFFF</strong> unless {@link Builder#colorAverage()} was called, where
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
         * Defaults to <strong>0xFFFFFF</strong> unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color The RGB-formatted Color.
         */
        public Builder secondaryColor(int color) {
            this.materialInfo.colors.set(1, color);
            return this;
        }

        /**
         * Set the Color of this Material to be the average of the components specified in {@link #components}.<br>
         * Will default to <strong>0xFFFFFF</strong> if a components list is not specified.
         */
        public Builder colorAverage() {
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
         * @param iconSet The {@link MaterialIconSet} of this Material.
         */
        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        /**
         * Set the components that make up this Material.<br>
         * This information is used for automatic decomposition, chemical formula generation, among other things.
         *
         * @param components An Object array formed as pairs of {@link Material} and Integer, representing the
         *                   Material and the amount of said Material in this Material's composition.
         * @throws IllegalArgumentException if the Object array is malformed.
         */
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

        /**
         * Set the components that make up this Material.<br>
         * This information is used for automatic decomposition, chemical formula generation, among other things.
         *
         * @param components An array of {@link MaterialStack}, each representing the
         *                   Material and the amount of said Material in this Material's composition.
         */
        public Builder componentStacks(MaterialStack... components) {
            composition = Arrays.asList(components);
            return this;
        }

        /**
         * Set the components that make up this Material.<br>
         * This information is used for automatic decomposition, chemical formula generation, among other things.
         *
         * @param components An {@link ImmutableList} of {@link MaterialStack}, each representing the
         *                   Material and the amount of said Material in this Material's composition.
         */
        public Builder componentStacks(ImmutableList<MaterialStack> components) {
            composition = components;
            return this;
        }

        /** @see #componentStacks(MaterialStack...) */
        public Builder kjs$components(MaterialStackWrapper... components) {
            compositionSupplier = Arrays.asList(components);
            return this;
        }

        /** @see #componentStacks(ImmutableList) componentStacks(ImmutableList&lt;MaterialStack&gt;) */
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
         * Remove specific Items from this Material.
         *
         * @param prefixes The list of prefixes to ignore.
         */
        public Builder ignoredTagPrefixes(TagPrefix... prefixes) {
            if (this.ignoredTagPrefixes == null) {
                this.ignoredTagPrefixes = new HashSet<>();
            }
            this.ignoredTagPrefixes.addAll(Arrays.asList(prefixes));
            return this;
        }

        /**
         * Add a custom Item Tag to all items made from this Material.
         *
         * @param key The tag to add.
         */
        public Builder customTags(TagKey<Item> key) {
            this.itemTags.add(key);
            return this;
        }

        /**
         * Set the Element of this Material.<br>
         * Should be effectively singleton; each element should only have 1 Material claiming to represent it.
         *
         * @param element The {@link Element} that this Material represents.
         */
        public Builder element(Element element) {
            this.materialInfo.element = element;
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
        public Builder formula(String formula) {
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
        public Builder formula(String formula, boolean withFormatting) {
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
        public Builder toolStats(ToolProperty toolProperty) {
            properties.setProperty(PropertyKey.TOOL, toolProperty);
            return this;
        }

        /**
         * Add an {@link ArmorProperty} to this Material.<br>
         * Adds Armors to this Material.
         *
         * @see ArmorProperty.Builder
         */
        public Builder armorStats(ArmorProperty armorProperty) {
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
        public Builder rotorStats(int power, int efficiency, float damage, int durability) {
            properties.setProperty(PropertyKey.ROTOR, new RotorProperty(power, efficiency, damage, durability));
            return this;
        }

        /** @see #blast(int) */
        public Builder blastTemp(int temp) {
            return blast(temp);
        }

        /** @see #blast(int, BlastProperty.GasTier) */
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier) {
            return blast(temp, gasTier);
        }

        /** @see #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;) */
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
            return blast(b -> b.temp(temp, gasTier).blastStats(eutOverride));
        }

        /** @see #blast(UnaryOperator) blast(UnaryOperator&lt;BlastProperty.Builder&gt;) */
        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
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
        public Builder blast(int temp) {
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
        public Builder blast(int temp, BlastProperty.GasTier gasTier) {
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
         *
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
        public Builder blast(UnaryOperator<BlastProperty.Builder> b) {
            properties.setProperty(PropertyKey.BLAST, b.apply(new BlastProperty.Builder()).build());
            return this;
        }

        /**
         * Remove the Hazard from this Material.<br>
         * Useful when a component of this Material would automatically apply an undesired hazard.
         */
        public Builder removeHazard() {
            properties.setProperty(HAZARD,
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
        public Builder radioactiveHazard(float multiplier) {
            properties.setProperty(HAZARD, new HazardProperty(HazardProperty.HazardTrigger.ANY,
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
        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, 1, false));
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
        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              float progressionMultiplier) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, progressionMultiplier, false));
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
        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              float progressionMultiplier, boolean applyToDerivatives) {
            properties.setProperty(HAZARD,
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
        public Builder hazard(HazardProperty.HazardTrigger trigger, MedicalCondition condition,
                              boolean applyToDerivatives) {
            properties.setProperty(HAZARD, new HazardProperty(trigger, condition, 1, applyToDerivatives));
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
        public Builder ore() {
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
        public Builder ore(boolean emissive) {
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
        public Builder ore(int oreMultiplier, int byproductMultiplier) {
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
        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
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
        public Builder washedIn(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m);
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
        public Builder washedIn(Material m, int washedAmount) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setWashedIn(m, washedAmount);
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
        public Builder separatedInto(Material... m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setSeparatedInto(m);
            return this;
        }

        /**
         * Sets the Material which this Material's Ore Block smelts to directly in a Furnace.<br>
         * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
         * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
         *
         * @param m The Material which should be output when smelting.
         */
        public Builder oreSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setDirectSmeltResult(m);
            return this;
        }

        /**
         * Adds a Polarizer recipe to this Material's metal parts, outputting the provided Material.<br>
         * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
         * with a harvest level of 2 and no Furnace burn time (if not already set).
         *
         * @param m The Material that this Material will be polarized into.
         */
        public Builder polarizesInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMagneticMaterial(m);
            return this;
        }

        /**
         * Sets the Material that this Material will automatically transform into in any Arc Furnace recipe.<br>
         * Automatically adds an {@link IngotProperty} to this Material if it does not already have one,
         * with a harvest level of 2 and no Furnace burn time (if not already set).
         *
         * @param m The Material that this Material will turn into in any Arc Furnace recipes.
         */
        public Builder arcSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setArcSmeltingInto(m);
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
        public Builder macerateInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setMacerateInto(m);
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
        public Builder ingotSmeltInto(Material m) {
            properties.ensureSet(PropertyKey.INGOT);
            properties.getProperty(PropertyKey.INGOT).setSmeltingInto(m);
            return this;
        }

        /**
         * Adds Ore byproducts to this Material.<br>
         * Automatically adds an {@link OreProperty} to this Material if it does not already have one,
         * with ore and byproduct multipliers of 1 and no emissive textures (if not already set).
         *
         * @param byproducts The list of Materials which serve as byproducts during ore processing.
         */
        public Builder addOreByproducts(Material... byproducts) {
            properties.ensureSet(PropertyKey.ORE);
            properties.getProperty(PropertyKey.ORE).setOreByProducts(byproducts);
            return this;
        }

        /**
         * Add Wires and Cables to this Material.
         *
         * @param voltage  The voltage tier of this Cable. Should conform to standard GregTech voltage tiers.
         * @param amperage The amperage of this Cable. Should be greater than zero.
         * @param loss     The loss-per-block of this Cable. A value of zero here will still have loss as wires.
         */
        public Builder cableProperties(long voltage, int amperage, int loss) {
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
        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
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
        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon,
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
        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
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
        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof,
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
        public Builder itemPipeProperties(int priority, float stacksPerSec) {
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
        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (!properties.hasProperty(PropertyKey.TOOL)) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.getProperty(PropertyKey.TOOL).addEnchantmentForTools(enchant, level);
            return this;
        }

        /**
         * Verify the passed information and finalize the Material.
         *
         * @return The finalized Material.
         */
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
            if (!itemTags.isEmpty()) {
                mat.setItemTags(itemTags);
            }
            if (formula != null) {
                mat.setFormula(formula, formatFormula);
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

        @Setter
        @Getter
        private String overriddenName;

        /**
         * The colors of this Material.
         * if any past index 0 are -1, they aren't used.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        @Getter
        @Setter
        private IntList colors = IntArrayList.of(-1, -1);

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
