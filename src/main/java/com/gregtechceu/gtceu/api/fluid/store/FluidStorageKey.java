package com.gregtechceu.gtceu.api.fluid.store;

import com.gregtechceu.gtceu.api.fluid.FluidState;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.info.MaterialIconType;

import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public final class FluidStorageKey {

    private static final Map<ResourceLocation, FluidStorageKey> keys = new Object2ObjectOpenHashMap<>();

    @Getter
    private final ResourceLocation resourceLocation;
    @Getter
    private final String tagKey;
    @Getter
    private final MaterialIconType iconType;
    private final Function<Material, String> registryNameFunction;
    private final Function<Material, String> translationKeyFunction;
    private final int hashCode;
    @Getter
    private final FluidState defaultFluidState;
    @Getter
    private final int registrationPriority;

    public FluidStorageKey(@NotNull ResourceLocation resourceLocation, @NotNull String tagKey,
                           @NotNull MaterialIconType iconType,
                           @NotNull Function<@NotNull Material, @NotNull String> registryNameFunction,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction,
                           @Nullable FluidState defaultFluidState, int registrationPriority) {
        this.resourceLocation = resourceLocation;
        this.tagKey = tagKey;
        this.iconType = iconType;
        this.registryNameFunction = registryNameFunction;
        this.translationKeyFunction = translationKeyFunction;
        this.hashCode = resourceLocation.hashCode();
        this.defaultFluidState = defaultFluidState;
        this.registrationPriority = registrationPriority;
        if (keys.containsKey(resourceLocation)) {
            throw new IllegalArgumentException("Cannot create duplicate keys");
        }
        keys.put(resourceLocation, this);
    }

    public static @Nullable FluidStorageKey getByName(@NotNull ResourceLocation location) {
        return keys.get(location);
    }

    public static Collection<FluidStorageKey> allKeys() {
        return keys.values();
    }

    /**
     * @param baseName the base name of the fluid
     * @return the registry name to use
     */
    public @NotNull String getRegistryNameFor(@NotNull Material baseName) {
        return registryNameFunction.apply(baseName);
    }

    /**
     * @return the translation key for fluids with this key
     */
    public @NotNull String getTranslationKeyFor(@NotNull Material material) {
        return this.translationKeyFunction.apply(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidStorageKey fluidKey = (FluidStorageKey) o;

        return resourceLocation.equals(fluidKey.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidStorageKey{" + resourceLocation + '}';
    }
}
