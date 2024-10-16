package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface FluidStorage {

    @AllArgsConstructor
    public static class FluidEntry {

        @SuppressWarnings({ "unchecked", "deprecation" })
        public static final Codec<FluidEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid")
                        .xmap(fluid1 -> (Supplier<Fluid>) () -> fluid1, Supplier::get)
                        .forGetter(val -> (Supplier<Fluid>) val.fluid),
                FluidBuilder.CODEC.optionalFieldOf("builder", null).forGetter(val -> val.builder))
                .apply(instance, FluidEntry::new));

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
