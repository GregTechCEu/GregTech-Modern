package com.gregtechceu.gtceu.api;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.material.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GTCEuAPI {

    public static final int GT_DATA_VERSION = 1;

    /** Will always be available */
    public static GTCEu instance;
    /** Will be available at the Construction stage */
    public static IMaterialRegistryManager materialManager;

    /** Will be available at the Pre-Initialization stage */
    @Getter
    private static boolean highTier;
    private static boolean highTierInitialized;

    public static final Map<ICoilType, Supplier<CoilBlock>> HEATING_COILS = new HashMap<>();
    public static final Map<IFilterType, Supplier<Block>> CLEANROOM_FILTERS = new HashMap<>();
    public static final Map<IBatteryData, Supplier<BatteryBlock>> PSS_BATTERIES = new HashMap<>();

    /**
     * Initializes High-Tier. Internal use only, do not attempt to call this.
     */
    @ApiStatus.Internal
    public static void initializeHighTier() {
        if (highTierInitialized) throw new IllegalStateException("High-Tier is already initialized.");
        highTier = ConfigHolder.INSTANCE.machines.highTierContent ||
                AddonFinder.getAddons().stream().anyMatch(IGTAddon::requiresHighTier) || Platform.isDevEnv();
        highTierInitialized = true;

        if (isHighTier()) GTCEu.LOGGER.info("High-Tier is Enabled.");
        else GTCEu.LOGGER.info("High-Tier is Disabled.");
    }

    public static class RegisterEvent extends Event implements IModBusEvent {

        @Getter
        private final GTRegistry<?, ?> registry;

        public RegisterEvent(GTRegistry<?, ?> registry) {
            this.registry = registry;
        }

        public <K, V> void register(K key, V value) {
            //noinspection unchecked
            ((GTRegistry<K, V>) registry).register(key, value);
        }
    }
}
