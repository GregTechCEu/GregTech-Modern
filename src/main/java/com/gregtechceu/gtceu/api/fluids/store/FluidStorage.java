package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import net.minecraft.world.level.material.Fluid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface FluidStorage {

    @AllArgsConstructor
    public static class FluidEntry {

        @Getter
        private Supplier<? extends Fluid> fluid;
        @Nullable
        @Getter
        @Setter
        private FluidBuilder builder;
    }

    /**
     * Enqueue a fluid for registration
     *
     * @param key     the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    public void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder);

    /**
     * @param key the key corresponding with the FluidBuilder
     * @return the fluid builder queued to be registered
     */
    public @Nullable FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key);

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    public @Nullable Fluid get(@NotNull FluidStorageKey key);

    public @Nullable FluidEntry getEntry(@NotNull FluidStorageKey key);

    /**
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     * @throws IllegalArgumentException if a key is already associated with another fluid
     */
    void store(@NotNull FluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid, @Nullable FluidBuilder builder);
}
