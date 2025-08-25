package com.gregtechceu.gtceu.common.unification.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MaterialRegistryImpl extends MaterialRegistry {

    private static int networkIdCounter;

    private final int networkId = networkIdCounter++;
    private final java.lang.String modid;

    private boolean isRegistryClosed = false;
    @NotNull
    private Material fallbackMaterial = GTMaterials.NULL;

    protected MaterialRegistryImpl(@NotNull java.lang.String modid) {
        super(modid);
        this.modid = modid;
    }

    @Override
    public void register(Material material) {
        this.register(material.getName(), material);
    }

    @Override
    public <T extends Material> T register(@NotNull java.lang.String key, @NotNull T value) {
        if (isRegistryClosed) {
            GTCEu.LOGGER.error(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the MaterialEvent. Skipping material {}...",
                    key);
            return null;
        }
        super.register(key, value);
        return value;
    }

    @NotNull
    @Override
    public Collection<Material> getAllMaterials() {
        return this.values();
    }

    @Override
    public void setFallbackMaterial(@NotNull Material material) {
        this.fallbackMaterial = material;
    }

    @NotNull
    @Override
    public Material getFallbackMaterial() {
        if (this.fallbackMaterial.isNull()) {
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
