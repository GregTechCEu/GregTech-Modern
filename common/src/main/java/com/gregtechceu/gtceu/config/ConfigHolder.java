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
        public boolean generateLowQualityGems = true;
        public boolean disableManualCompression = false;
        public boolean harderRods = false;
        public boolean harderBrickRecipes = false;
        public boolean harderEBFControllerRecipe = false;
        public boolean nerfWoodCrafting = false;
        public boolean hardWoodRecipes = false;
        public boolean hardIronRecipes = true;
        public boolean hardRedstoneRecipes = false;
        public boolean hardToolArmorRecipes = false;
        public boolean hardMiscRecipes = false;
        public boolean hardGlassRecipes = true;
        public boolean nerfPaperCrafting = true;
        public boolean hardAdvancedIronRecipes = false;
        public boolean hardDyeRecipes = false;
        public boolean harderCharcoalRecipe = true;

        public boolean flintAndSteelRequireSteel = true;
        public boolean disableConcreteInWorld = false;
        public boolean removeVanillaBlockRecipes = false;
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
