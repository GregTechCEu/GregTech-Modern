package com.gregtechceu.gtceu.common.unification.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class MaterialRegistryImpl extends MaterialRegistry {

    private static int networkIdCounter;

    private final int networkId = networkIdCounter++;
    private final java.lang.String modid;

    private boolean isRegistryClosed = false;
    private Material fallbackMaterial = null;

    protected MaterialRegistryImpl(@NotNull java.lang.String modid) {
        super(modid);
        this.modid = modid;
    }

    @Override
    public void register(Material material) {
        this.register(material.getName(), material);
    }

    @Override
    public void register(@NotNull java.lang.String key, @NotNull Material value) {
        if (isRegistryClosed) {
            GTCEu.LOGGER.error(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the MaterialEvent. Skipping material {}...",
                    key);
            return;
        }
        super.register(key, value);
    }

    @NotNull
    @Override
    public Collection<Material> getAllMaterials() {
        return Collections.unmodifiableCollection(this.registry.values());
    }

    @Override
    public void setFallbackMaterial(@NotNull Material material) {
        this.fallbackMaterial = material;
    }

    @NotNull
    @Override
    public Material getFallbackMaterial() {
        if (this.fallbackMaterial == null) {
            this.fallbackMaterial = MaterialRegistryManager.getInstance().getDefaultFallback();
        }
        return this.fallbackMaterial;
    }

    @Override
    public int getNetworkId() {
        return this.networkId;
    }

    @NotNull
    @Override
    public java.lang.String getModid() {
        return this.modid;
    }

    public void closeRegistry() {
        this.isRegistryClosed = true;
    }
}
