package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class MaterialRegistryEntry extends RegistryEntry<Material, Material> {

    public MaterialRegistryEntry(GTRegistrate owner, DeferredHolder<Material, Material> key) {
        super(owner, key);
    }

    public String getName() {
        return getKey().location().getPath();
    }

    public Material.MaterialInfo getMaterialInfo() {
        return value().getMaterialInfo();
    }

    public MaterialProperties getProperties() {
        return value().getProperties();
    }

    public MaterialFlags getFlags() {
        return value().getFlags();
    }

    /**
     * Retrieves a fluid from the material.
     * Attempts to retrieve with {@link FluidProperty#getPrimaryKey()}, {@link FluidStorageKeys#LIQUID} and
     * {@link FluidStorageKeys#GAS}.
     *
     * @return the fluid
     * @see #getFluid(FluidStorageKey)
     */
    public @Nullable Fluid getFluid() {
        return value().getFluid();
    }

    /**
     * @param key the key for the fluid
     * @return the fluid corresponding with the key
     */
    public @Nullable Fluid getFluid(FluidStorageKey key) {
        return value().getFluid(key);
    }

    /**
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     * @see #getFluid(FluidStorageKey, int)
     */
    public FluidStack getFluid(int amount) {
        return value().getFluid(amount);
    }

    /**
     * @param key    the key for the fluid
     * @param amount the amount the FluidStack should have
     * @return a FluidStack with the fluid and amount
     */
    public FluidStack getFluid(FluidStorageKey key, int amount) {
        return value().getFluid(key, amount);
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return value().hasProperty(key);
    }

    public <T extends IMaterialProperty> @Nullable T getProperty(PropertyKey<T> key) {
        return value().getProperty(key);
    }

    public <T extends IMaterialProperty> void removeProperty(PropertyKey<T> key) {
        value().removeProperty(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty property) {
        value().setProperty(key, property);
    }

    public boolean isSolid() {
        return hasProperty(PropertyKey.INGOT) || hasProperty(PropertyKey.GEM);
    }

    public boolean hasFluid() {
        return hasProperty(PropertyKey.FLUID);
    }

    public int getMaterialARGB() {
        return value().getMaterialARGB();
    }

    /**
     * Gets a specific color layer in ARGB.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialARGB(int index) {
        return value().getMaterialARGB(index);
    }

    public int getMaterialRGB() {
        return value().getMaterialRGB();
    }

    /**
     * Gets a specific color layer.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialRGB(int index) {
        return value().getMaterialRGB(index);
    }

    /**
     * @return a {@code TagKey<Fluid>} with the material's name as the tag key
     * @see #getFluid(FluidStorageKey, int)
     */
    public TagKey<Fluid> getFluidTag() {
        return value().getFluidTag();
    }

    public SizedFluidIngredient asFluidIngredient(int amount) {
        return value().asFluidIngredient(amount);
    }

    public SizedFluidIngredient asSingleFluidIngredient(int amount) {
        return value().asSingleFluidIngredient(amount);
    }
}
