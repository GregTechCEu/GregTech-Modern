package com.gregtechceu.gtceu.api.fluid.store;

import com.gregtechceu.gtceu.api.fluid.FluidBuilder;

import net.minecraft.world.level.material.Fluid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface FluidStorage {

    @AllArgsConstructor
    static class FluidEntry {

        @Getter
        private Supplier<? extends Fluid> fluid;
        @Nullable
        @Getter
        @Setter
        private FluidBuilder builder;
        @Getter
        @Setter
        private FluidStorageKey key;
    }

    /**
     * Enqueue a fluid for registration
     *
     * @param key     the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    void enqueueRegistration(FluidStorageKey key, FluidBuilder builder);

    /**
     * @param key the key corresponding with the FluidBuilder
     * @return the fluid builder queued to be registered
     */
    @Nullable
    FluidBuilder getQueuedBuilder(FluidStorageKey key);

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    @Nullable
    Fluid get(FluidStorageKey key);

    @Nullable
    FluidEntry getEntry(FluidStorageKey key);

    /**
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     * @throws IllegalArgumentException if a key is already associated with another fluid
     */
    void store(FluidStorageKey key, Supplier<? extends Fluid> fluid, @Nullable FluidBuilder builder);
}
