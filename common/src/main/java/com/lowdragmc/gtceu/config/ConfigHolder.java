package com.lowdragmc.gtceu.config;

import net.minecraft.world.level.Explosion;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote ConfigHolder
 */
public class ConfigHolder {
    public static RecipeConfigs recipes = new RecipeConfigs();
    public static WorldGenConfigs worldgen = new WorldGenConfigs();
    public static MachineConfigs machines = new MachineConfigs();
    public static ClientConfigs client = new ClientConfigs();

    public static class RecipeConfigs {
        public boolean generateLowQualityGems = true;
        public boolean disableManualCompression = true;
    }

    public static class WorldGenConfigs {
        public boolean allUniqueStoneTypes;
    }
    public static class MachineConfigs {
        public boolean doTerrainExplosion;
        public boolean doesExplosionDamagesTerrain;
        public double overclockDivisor = 2.0;
        public boolean machineSounds = true;
        public boolean steelSteamMultiblocks;
    }

    public static class ClientConfigs {
        public boolean machinesEmissiveTextures = true;
    }

}
