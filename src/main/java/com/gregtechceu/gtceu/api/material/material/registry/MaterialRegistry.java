package com.gregtechceu.gtceu.api.material.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.material.material.IMaterialRegistry;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.data.material.GTMaterials;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public final class MaterialRegistry extends MappedRegistry<Material> implements IMaterialRegistry {

    private final Set<String> usedNamespaces = new HashSet<>();
    private final Map<String, Material> fallbackMaterials = new HashMap<>();

    private boolean isRegistryClosed = false;

    public MaterialRegistry(ResourceKey<Registry<Material>> key) {
        super(key, Lifecycle.stable());
    }

    @Override
    public @NotNull Set<String> getUsedNamespaces() {
        return Collections.unmodifiableSet(usedNamespaces);
    }

    @Override
    public @NotNull Stream<Material> stream() {
        return super.stream();
    }

    // overriding this avoids a mixin.
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean doesSync() {
        return true;
    }

    public Material register(Material material) {
        return register(material.getResourceLocation(), material);
    }

    private Material register(ResourceLocation id, Material material) {
        this.register(ResourceKey.create(this.key(), id), material, RegistrationInfo.BUILT_IN);
        return material;
    }

    @Override
    public Material getMaterial(ResourceLocation name) {
        Material value = get(name);
        return value != null ? value : GTMaterials.NULL;
    }

    @Override
    public ResourceLocation getKey(Material material) {
        return material.getResourceLocation();
    }

    @Override
    public Holder.@NotNull Reference<Material> register(int id,
                                                        @NotNull ResourceKey<Material> key, @NotNull Material value,
                                                        @NotNull RegistrationInfo registrationInfo) {
        if (isRegistryClosed) {
            throw new IllegalStateException(
                    "Materials cannot be registered in the PostMaterialEvent (or after)! Must be added in the RegisterEvent. Skipping material %s..."
                            .formatted(key.location()));
        }
        usedNamespaces.add(key.location().getNamespace());
        return super.register(id, key, value, registrationInfo);
    }

    /**
     * Set the fallback material for a namespace.
     * This is only for manual fallback usage.
     *
     * @param modId    the namespace to set the fallback for
     * @param material the fallback material
     */
    @Override
    public void setFallbackMaterial(@NotNull String modId, @NotNull Material material) {
        fallbackMaterials.put(modId, material);
    }

    /**
     * This is only for manual fallback usage.
     *
     * @param modId the namespace to get the fallback for
     * @return the fallback material, used for when another material does not exist
     */
    @Override
    @NotNull
    public Material getFallbackMaterial(@NotNull String modId) {
        return fallbackMaterials.getOrDefault(modId, getDefaultFallback());
    }

    @NotNull
    public Material getDefaultFallback() {
        return fallbackMaterials.get(GTCEu.MOD_ID);
    }

    @Override
    public boolean isFrozen() {
        return this.gtceu$isFrozen();
    }

    public void close() {
        isRegistryClosed = true;
    }
}
