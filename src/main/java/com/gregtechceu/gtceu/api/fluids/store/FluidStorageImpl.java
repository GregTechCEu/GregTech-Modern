package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class FluidStorageImpl implements FluidStorage {

    private final Map<FluidStorageKey, FluidEntry> map = new Object2ObjectOpenHashMap<>();
    private Map<FluidStorageKey, FluidBuilder> toRegister = new Object2ObjectOpenHashMap<>();

    private boolean registered = false;

    public FluidStorageImpl() {}

    /**
     * Enqueue a fluid for registration
     *
     * @param key     the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    @Override
    public void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        if (registered) {
            throw new IllegalStateException("Cannot enqueue a builder after registration");
        }

        if (toRegister.containsKey(key)) {
            throw new IllegalArgumentException("FluidStorageKey " + key + " is already queued");
        }
        toRegister.put(key, builder);
    }

    /**
     * @param key the key corresponding with the FluidBuilder
     * @return the fluid builder queued to be registered
     */
    @Override
    public @Nullable FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key) {
        if (registered) {
            throw new IllegalArgumentException("FluidStorage has already been registered");
        }
        return toRegister.get(key);
    }

    /**
     * Register the enqueued fluids
     *
     * @param material the material the fluid is based off of
     */
    @ApiStatus.Internal
    public void registerFluids(@NotNull Material material, GTRegistrate registrate) {
        if (registered) {
            throw new IllegalStateException("FluidStorage has already been registered");
        }

        // If nothing is queued for registration and nothing is manually stored,
        // we need something for the registry to handle this will prevent cases
        // of a material having a fluid property but no fluids actually created
        // for the material.
        if (toRegister.isEmpty() && map.isEmpty()) {
            enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
        }

        toRegister.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> -e.getKey().getRegistrationPriority()))
                .forEach(entry -> {
                    if (map.containsKey(entry.getKey())) {
                        GTCEu.LOGGER.warn("{} already has an associated fluid for material {}", entry.getKey(),
                                material);
                        return;
                    }
                    Supplier<? extends Fluid> fluid = entry.getValue().build(material, entry.getKey(), registrate);
                    if (!storeNoOverwrites(entry.getKey(), fluid, entry.getValue())) {
                        GTCEu.LOGGER.error("{} already has an associated fluid for material {}", material, material);
                    }
                });
        toRegister = null;
        registered = true;
    }

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    @Override
    public @Nullable Fluid get(@NotNull FluidStorageKey key) {
        return map.containsKey(key) ? map.get(key).getFluid().get() : null;
    }

    public @Nullable FluidEntry getEntry(@NotNull FluidStorageKey key) {
        return map.getOrDefault(key, null);
    }

    /**
     * Will do nothing if an existing fluid association would be overwritten.
     *
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     * @return if the associations were successfully updated
     */
    private boolean storeNoOverwrites(@NotNull FluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
                                      @Nullable FluidBuilder builder) {
        if (map.containsKey(key)) {
            return false;
        }
        store(key, fluid, builder);
        return true;
    }

    @Override
    public void store(@NotNull FluidStorageKey key, @NotNull Supplier<? extends Fluid> fluid,
                      @Nullable FluidBuilder builder) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(key + " already has an associated fluid");
        }
        if (builder != null) {
            map.put(key, new FluidEntry(fluid, builder));
        } else {
            map.put(key, new FluidEntry(fluid, null));
        }
    }
}
