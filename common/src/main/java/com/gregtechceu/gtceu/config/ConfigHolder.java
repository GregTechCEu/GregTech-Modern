package com.gregtechceu.gtceu.config;

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
        public boolean generateLowQualityGems = true; // default true
        public boolean disableManualCompression = false; // default false
        public boolean harderRods = false; // default false
        public boolean harderBrickRecipes = false; // default false
        public boolean nerfWoodCrafting = false; // default false
        public boolean hardWoodRecipes = false; // default false
        public boolean hardIronRecipes = true; // default true
        public boolean hardRedstoneRecipes = false; // default false
        public boolean hardToolArmorRecipes = false; // default false
        public boolean hardMiscRecipes = false; // default false
        public boolean hardGlassRecipes = true; // default true
        public boolean nerfPaperCrafting = true; // default true
        public boolean hardAdvancedIronRecipes = false; // default false
        public boolean hardDyeRecipes = false; // default false
        public boolean harderCharcoalRecipe = true; // default true
        public boolean flintAndSteelRequireSteel = true; // default true
        public boolean removeVanillaBlockRecipes = false; // default false
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
