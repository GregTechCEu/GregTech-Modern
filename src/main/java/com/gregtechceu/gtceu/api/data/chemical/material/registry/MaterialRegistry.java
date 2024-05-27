package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class MaterialRegistry extends GTRegistry.String<Material> {

    @Getter
    private final GTRegistrate registrate;

    public MaterialRegistry(java.lang.String modId) {
        super(new ResourceLocation(modId, "material"));
        IGTAddon addon = AddonFinder.getAddon(modId);
        this.registrate = addon != null ? addon.getRegistrate() :
                GTCEu.MOD_ID.equals(modId) ? GTRegistration.REGISTRATE : GTRegistrate.create(modId);
    }

    public abstract void register(Material material);

    @NotNull
    public abstract Collection<Material> getAllMaterials();

    /**
     * Set the fallback material for this registry.
     * This is only for manual fallback usage.
     *
     * @param material the fallback material
     */
    public abstract void setFallbackMaterial(@NotNull Material material);

    /**
     * This is only for manual fallback usage.
     *
     * @return the fallback material, used for when another material does not exist
     */
    @NotNull
    public abstract Material getFallbackMaterial();

    /**
     * @return the network ID for this registry
     */
    public abstract int getNetworkId();

    @NotNull
    public abstract java.lang.String getModid();
}
