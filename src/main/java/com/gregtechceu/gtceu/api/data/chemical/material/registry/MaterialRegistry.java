package com.gregtechceu.gtceu.api.data.chemical.material.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.core.mixins.MappedRegistryAccessor;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public final class MaterialRegistry extends DefaultedMappedRegistry<Material> {

    private final Set<String> usedNamespaces = new HashSet<>();
    private boolean isRegistryClosed = false;

    public MaterialRegistry(ResourceKey<Registry<Material>> key) {
        super("gtceu:null", key, Lifecycle.stable(), false);
    }

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

    public Material getMaterial(String name) {
        return getMaterial(GTCEu.id(name));
    }

    public Material getMaterial(ResourceLocation name) {
        return this.get(name);
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

    public boolean isFrozen() {
        return ((MappedRegistryAccessor) (Object) this).gtceu$isFrozen();
    }

    public void close() {
        isRegistryClosed = true;
    }
}
