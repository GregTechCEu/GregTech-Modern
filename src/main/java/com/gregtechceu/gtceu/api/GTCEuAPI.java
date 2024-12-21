package com.gregtechceu.gtceu.api;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.data.chemical.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GTCEuAPI {

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

    public static boolean isModLoaded(java.lang.String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * @return if the current thread is the client thread
     */
    public static boolean isClientThread() {
        return isClientSide() && Minecraft.getInstance().isSameThread();
    }

    /**
     * @return if the FML environment is a client
     */
    public static boolean isClientSide() {
        return FMLEnvironment.dist.isClient();
    }

    public static class Mods {

        public static boolean isJeiLoaded() {
            return !(isEmiLoaded() || isReiLoaded()) && isModLoaded(GTValues.MODID_JEI);
        }

        public static boolean isReiLoaded() {
            // todo: convert to base check after ui rework
            return LDLib.isReiLoaded();
        }

        public static boolean isEmiLoaded() {
            // todo: convert to base check after ui rework
            return LDLib.isEmiLoaded();
        }

        public static boolean isKubeJSLoaded() {
            return isModLoaded(GTValues.MODID_KUBEJS);
        }

        public static boolean isIrisOculusLoaded() {
            return isModLoaded(GTValues.MODID_IRIS) || isModLoaded(GTValues.MODID_OCULUS);
        }

        public static boolean isSodiumRubidiumEmbeddiumLoaded() {
            return isModLoaded(GTValues.MODID_SODIUM) || isModLoaded(GTValues.MODID_RUBIDIUM) ||
                    isModLoaded(GTValues.MODID_EMBEDDIUM);
        }

        public static boolean isAE2Loaded() {
            return isModLoaded(GTValues.MODID_APPENG);
        }

        public static boolean isCuriosLoaded() {
            return isModLoaded(GTValues.MODID_CURIOS);
        }

        public static boolean isShimmerLoaded() {
            return isModLoaded(GTValues.MODID_SHIMMER);
        }

        public static boolean isJAVDLoaded() {
            return isModLoaded(GTValues.MODID_JAVD);
        }

        public static boolean isFTBTeamsLoaded() {
            return isModLoaded(GTValues.MODID_FTB_TEAMS);
        }

        public static boolean isArgonautsLoaded() {
            return isModLoaded(GTValues.MODID_ARGONAUTS);
        }
    }

    public static class RegisterEvent<K, V> extends GenericEvent<V> implements IModBusEvent {

        private final GTRegistry<K, V> registry;

        public RegisterEvent(GTRegistry<K, V> registry, Class<V> clazz) {
            super(clazz);
            this.registry = registry;
        }

        public void register(K key, V value) {
            if (registry != null) registry.register(key, value);
        }

        public static class RL<V> extends RegisterEvent<ResourceLocation, V> {

            public RL(GTRegistry<ResourceLocation, V> registry, Class<V> clazz) {
                super(registry, clazz);
            }
        }

        public static class String<V> extends RegisterEvent<java.lang.String, V> {

            public String(GTRegistry<java.lang.String, V> registry, Class<V> clazz) {
                super(registry, clazz);
            }
        }
    }
}
