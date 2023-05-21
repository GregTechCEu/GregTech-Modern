package com.gregtechceu.gtceu.config;

import com.gregtechceu.gtceu.GTCEu;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote ConfigHolder
 */
@Config(id = GTCEu.MOD_ID)
public class ConfigHolder {
    public static ConfigHolder INSTANCE;

    public static void init() {
        INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
    }

    @Configurable
    public RecipeConfigs recipes = new RecipeConfigs();
    @Configurable
    public WorldGenConfigs worldgen = new WorldGenConfigs();
    @Configurable
    public MachineConfigs machines = new MachineConfigs();
    @Configurable
    public ClientConfigs client = new ClientConfigs();

    public static class RecipeConfigs {
        @Configurable
        @Configurable.Comment({"Whether to generate Flawed and Chipped Gems for materials and recipes involving them.",
                "Useful for mods like TerraFirmaCraft.", "Default: false"})
        public boolean generateLowQualityGems = true; // default true
        @Configurable
        @Configurable.Comment({"Whether to remove Block/Ingot compression and decompression in the Crafting Table.", "Default: false"})
        public boolean disableManualCompression = true; // default true
        @Configurable
        @Configurable.Comment({"Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.", "Default: false"})
        public boolean harderRods = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make crafting recipes for Bricks, Firebricks, and Coke Bricks harder.", "Default: false"})
        public boolean harderBrickRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to nerf Wood crafting to 2 Planks from 1 Log, and 2 Sticks from 2 Planks.", "Default: false"})
        public boolean nerfWoodCrafting = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make Wood related recipes harder.", "Excludes sticks and planks.", "Default: false"})
        public boolean hardWoodRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Recipes for Buckets, Cauldrons, Hoppers, and Iron Bars" +
                " require Iron Plates, Rods, and more.", "Default: true"})
        public boolean hardIronRecipes = true; // default true
        @Configurable
        @Configurable.Comment({"Whether to make Redstone related recipes harder.", "Default: false"})
        public boolean hardRedstoneRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make Vanilla Tools and Armor recipes harder.", "Excludes Flint and Steel, and Buckets.", "Default: false"})
        public boolean hardToolArmorRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make miscellaneous recipes harder.", "Default: false"})
        public boolean hardMiscRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make Glass related recipes harder. Default: true"})
        public boolean hardGlassRecipes = true; // default true
        @Configurable
        @Configurable.Comment({"Whether to nerf the Paper crafting recipe.", "Default: true"})
        public boolean nerfPaperCrafting = true; // default true
        @Configurable
        @Configurable.Comment({"Recipes for items like Iron Doors, Trapdoors, Anvil" +
                " require Iron Plates, Rods, and more.", "Default: false"})
        public boolean hardAdvancedIronRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make coloring blocks like Concrete or Glass harder.", "Default: false"})
        public boolean hardDyeRecipes = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to remove charcoal smelting recipes from the vanilla furnace.", "Default: true"})
        public boolean harderCharcoalRecipe = true; // default true
        @Configurable
        @Configurable.Comment({"Whether to make the Flint and Steel recipe require steel parts.", "Default: true."})
        public boolean flintAndSteelRequireSteel = true; // default true
        @Configurable
        @Configurable.Comment({"Whether to remove Vanilla Block Recipes from the Crafting Table.", "Default: false"})
        public boolean removeVanillaBlockRecipes = false; // default false
    }

    public static class WorldGenConfigs {
        @Configurable
        @Configurable.Comment({"Should all Stone Types drop unique Ore Item Blocks?", "Default: false (meaning only Stone, Netherrack, and Endstone"})
        public boolean allUniqueStoneTypes;
    }
    public static class MachineConfigs {
        @Configurable
        @Configurable.Comment({"Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava", "Default: false"})
        public boolean doTerrainExplosion;
        @Configurable
        @Configurable.Comment({"Whether machines or boilers damage the terrain when they explode.",
                "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.", "Default: true"})
        public boolean doesExplosionDamagesTerrain;
        @Configurable
        @Configurable.Comment({"Divisor for Recipe Duration per Overclock.", "Default: 2.0"})
        @Configurable.DecimalRange(min = 2.0, max = 3.0)
        @Configurable.Gui.NumberFormat("0.0#")
        public double overclockDivisor = 2.0;
        @Configurable
        @Configurable.Comment({"Whether to play machine sounds while machines are active.", "Default: true"})
        public boolean machineSounds = true;
        @Configurable
        @Configurable.Comment({"Whether Steam Multiblocks should use Steel instead of Bronze.", "Default: false"})
        public boolean steelSteamMultiblocks;
    }

    public static class ClientConfigs {
        @Configurable
        @Configurable.Comment({"Whether or not to enable Emissive Textures for GregTech Machines.", "Default: true"})
        public boolean machinesEmissiveTextures = true;
    }

}
