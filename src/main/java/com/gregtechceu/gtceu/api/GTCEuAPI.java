package com.gregtechceu.gtceu.api;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

public class GTCEuAPI {

    /** Will always be available */
    public static GTCEu instance;
    /** Will be available at the Construction stage */
    public static IMaterialRegistryManager materialManager;

    /** Will be available at the Pre-Initialization stage */
    @Getter
    private static boolean highTier;
    private static boolean highTierInitialized;

    /**
     * Initializes High-Tier. Internal use only, do not attempt to call this.
     */
    @ApiStatus.Internal
    public static void initializeHighTier() {
        if (highTierInitialized) throw new IllegalStateException("High-Tier is already initialized.");
        highTier = ConfigHolder.INSTANCE.machines.highTierContent || AddonFinder.getAddons().stream().anyMatch(IGTAddon::requiresHighTier) || Platform.isDevEnv();
        highTierInitialized = true;

        if (isHighTier()) GTCEu.LOGGER.info("High-Tier is Enabled.");
        else GTCEu.LOGGER.info("High-Tier is Disabled.");
    }
}
