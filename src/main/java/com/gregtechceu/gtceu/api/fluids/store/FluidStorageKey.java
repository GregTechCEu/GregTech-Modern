package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.utils.TagUtil;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public final class FluidStorageKey {

    private static final Map<Identifier, FluidStorageKey> keys = new Object2ObjectOpenHashMap<>();

    @Getter
    private final Identifier Identifier;
    @Getter
    private final @Nullable TagKey<Fluid> extraTag;
    @Getter
    private final MaterialIconType iconType;
    private final Function<Material, String> registryNameFunction;
    private final Function<Material, String> translationKeyFunction;
    private final int hashCode;
    @Getter
    private final @Nullable FluidState defaultFluidState;
    @Getter
    private final int registrationPriority;

    public FluidStorageKey(@NotNull Identifier Identifier, @Nullable TagKey<Fluid> extraTag,
                           @NotNull MaterialIconType iconType,
                           @NotNull Function<@NotNull Material, @NotNull String> registryNameFunction,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction,
                           @Nullable FluidState defaultFluidState, int registrationPriority) {
        this.Identifier = Identifier;
        this.extraTag = extraTag;
        this.iconType = iconType;
        this.registryNameFunction = registryNameFunction;
        this.translationKeyFunction = translationKeyFunction;
        this.hashCode = Identifier.hashCode();
        this.defaultFluidState = defaultFluidState;
        this.registrationPriority = registrationPriority;
        if (keys.containsKey(Identifier)) {
            throw new IllegalArgumentException("Cannot create duplicate keys");
        }
        keys.put(Identifier, this);
    }

    public FluidStorageKey(@NotNull Identifier Identifier, @NotNull String tagKey,
                           @NotNull MaterialIconType iconType,
                           @NotNull Function<@NotNull Material, @NotNull String> registryNameFunction,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction,
                           @Nullable FluidState defaultFluidState, int registrationPriority) {
        this(Identifier, TagUtil.createFluidTag(tagKey), iconType,
                registryNameFunction, translationKeyFunction,
                defaultFluidState, registrationPriority);
    }

    public FluidStorageKey(@NotNull Identifier Identifier, @NotNull MaterialIconType iconType,
                           @NotNull Function<@NotNull Material, @NotNull String> registryNameFunction,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction,
                           @Nullable FluidState defaultFluidState, int registrationPriority) {
        this(Identifier, (TagKey<Fluid>) null, iconType,
                registryNameFunction, translationKeyFunction,
                defaultFluidState, registrationPriority);
    }

    public static @Nullable FluidStorageKey getByName(@NotNull Identifier location) {
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

        return Identifier.equals(fluidKey.getIdentifier());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidStorageKey{" + Identifier + '}';
    }
}
