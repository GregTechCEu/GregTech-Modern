package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageImpl;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FluidProperty implements IMaterialProperty<FluidProperty>, FluidStorage {

    public static final Codec<FluidProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStorageImpl.CODEC.fieldOf("storage").forGetter(val -> val.storage),
            FluidStorageKey.CODEC.optionalFieldOf("primary_key", null).forGetter(val -> val.primaryKey))
            .apply(instance, FluidProperty::new));

    private final FluidStorageImpl storage;
    @Getter
    @Setter
    private FluidStorageKey primaryKey = null;

    @ApiStatus.Internal
    public FluidProperty(@NotNull FluidStorageImpl storage, @Nullable FluidStorageKey primaryKey) {
        this.storage = storage;
        this.primaryKey = primaryKey;
    }

    public FluidProperty() {
        this.storage = new FluidStorageImpl();
    }

    public FluidProperty(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        this();
        enqueueRegistration(key, builder);
    }

    public @NotNull FluidStorage getStorage() {
        return this;
    }

    @ApiStatus.Internal
    public void registerFluids(@NotNull Material material, @NotNull GTRegistrate registrate) {
        this.storage.registerFluids(material, registrate);
    }

    @Override
    public void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        storage.enqueueRegistration(key, builder);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public void store(@NotNull FluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
                      @Nullable FluidBuilder builder) {
        storage.store(key, fluid, builder);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public @Nullable Fluid get(@NotNull FluidStorageKey key) {
        return storage.get(key);
    }

    @Override
    public @Nullable FluidEntry getEntry(@NotNull FluidStorageKey key) {
        return storage.getEntry(key);
    }

    @Override
    public @Nullable FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key) {
        return storage.getQueuedBuilder(key);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (this.primaryKey == null) {
            throw new IllegalStateException("FluidProperty cannot be empty!");
        }
    }
}
