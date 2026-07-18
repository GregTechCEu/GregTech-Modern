package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.google.common.collect.ImmutableList;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

// TODO rename either this or the other MaterialEntry so they don't cause constant conflicts
//   for the record, I prefer renaming the one used for unification
public class MaterialEntry extends PlainEntry<Material> {

    public MaterialEntry(GTRegistrate owner, DeferredHolder<Material, Material> key) {
        super(owner, key);
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return get().getMaterialComponents();
    }

    public String getName() {
        return getId().getPath();
    }

    public String getUnlocalizedName() {
        return get().getUnlocalizedName();
    }

    public MutableComponent getLocalizedName() {
        return get().getLocalizedName();
    }

    public boolean isNull() {
        return this.equals(GTMaterials.NULL);
    }

    public boolean is(Material material) {
        // safer to do this this way than `this.get() == material`
        return this.is(material.getEntryWrapper());
    }

    public boolean is(MaterialEntry material) {
        return this.equals(material);
    }

    /**
     * Properties of this Material.
     *
     * @see MaterialProperties
     */
    public MaterialProperties getProperties() {
        return get().getProperties();
    }

    // separator

    public MaterialStack multiply(long amount) {
        return asStack(amount);
    }

    public MaterialStack asStack(long amount) {
        return new MaterialStack(this, amount);
    }

    // separator

    public int getMaterialARGB() {
        return getMaterialARGB(0);
    }

    public int getMaterialSecondaryARGB() {
        return getMaterialARGB(1);
    }

    /**
     * Gets a specific color layer in ARGB.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialARGB(int index) {
        return get().getMaterialARGB(index);
    }

    public int getMaterialRGB() {
        return getMaterialRGB(0);
    }

    public int getMaterialSecondaryRGB() {
        return getMaterialRGB(1);
    }

    /**
     * Gets a specific color layer.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialRGB(int index) {
        return get().getMaterialRGB(index);
    }

    // separator

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return get().hasProperty(key);
    }

    public <T extends IMaterialProperty> @UnknownNullability T getProperty(PropertyKey<T> key) {
        return get().getProperty(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, T property) {
        get().setProperty(key, property);
    }

    public boolean isSolid() {
        return get().isSolid();
    }

    public boolean hasFluid() {
        return get().hasFluid();
    }

    // separator

    /**
     * Retrieves a fluid from the material.
     * Attempts to retrieve with {@link FluidProperty#getPrimaryKey()}, {@link FluidStorageKeys#LIQUID} and
     * {@link FluidStorageKeys#GAS}.
     *
     * @return the fluid
     * @see #getFluid(FluidStorageKey)
     */
    public @UnknownNullability Fluid getFluid() {
        return get().getFluid();
    }

    /**
     * @param key the key for the fluid
     * @return the fluid corresponding with the key
     */
    public Fluid getFluid(FluidStorageKey key) {
        return get().getFluid(key);
    }

    /**
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     * @see #getFluid(FluidStorageKey, int)
     */
    public FluidStack getFluid(int amount) {
        return get().getFluid(amount);
    }

    /**
     * @param key    the key for the fluid
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     */
    public FluidStack getFluid(FluidStorageKey key, int amount) {
        return get().getFluid(key, amount);
    }

    /**
     * @return a {@code TagKey<Fluid>} with the material's name as the tag key
     * @see #getFluid(FluidStorageKey, int)
     */
    public TagKey<Fluid> getFluidTag() {
        return get().getFluidTag();
    }

    public SizedFluidIngredient asFluidIngredient(int amount) {
        return get().asFluidIngredient(amount);
    }

    public SizedFluidIngredient asSingleFluidIngredient(int amount) {
        return get().asSingleFluidIngredient(amount);
    }

    public @UnknownNullability Item getBucket() {
        return get().getBucket();
    }

    // separator

    /**
     * @param prefix the tagPrefix to check
     * @return if the material should have recipes autogenerated
     */
    public boolean shouldGenerateRecipesFor(TagPrefix prefix) {
        return get().shouldGenerateRecipesFor(prefix);
    }

    public void addFlags(MaterialFlag... flags) {
        get().addFlags(flags);
    }

    public boolean hasFlag(MaterialFlag flag) {
        return get().hasFlag(flag);
    }

    public boolean isElement() {
        return get().isElement();
    }

    public @Nullable Element getElement() {
        return get().getElement();
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return get().hasFlags(flags);
    }

    public boolean hasAnyOfFlags(MaterialFlag... flags) {
        return get().hasAnyOfFlags(flags);
    }

    public boolean isRadioactive() {
        return get().isRadioactive();
    }

    public long getProtons() {
        return get().getProtons();
    }

    public long getNeutrons() {
        return get().getNeutrons();
    }

    public long getMass() {
        return get().getMass();
    }

    public int getBlastTemperature() {
        return get().getBlastTemperature();
    }

    // separator

    public MaterialEntry onRegister(NonNullConsumer<Material> callback) {
        if (this.isBound()) {
            // if this is already registered, just run the callback.
            callback.accept(this.value());
        } else {
            getOwner().addRegisterCallback(getName(), key.registryKey(), callback);
        }
        return this;
    }

    public MaterialEntry afterRegister(Runnable callback) {
        if (this.isBound()) {
            // if this is already registered, just run the callback.
            callback.run();
        } else {
            getOwner().addRegisterCallback(key.registryKey(), callback);
        }
        return this;
    }
}
