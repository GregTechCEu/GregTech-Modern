package com.gregtechceu.gtceu.config;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
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
        if (INSTANCE == null) {
            INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        }
    }

    @Configurable
    public RecipeConfigs recipes = new RecipeConfigs();
    @Configurable
    public WorldGenConfigs worldgen = new WorldGenConfigs();
    @Configurable
    public MachineConfigs machines = new MachineConfigs();
    @Configurable
    public ClientConfigs client = new ClientConfigs();
    @Configurable
    @Configurable.Comment("Config options for Tools and Armor")
    public ToolConfigs tools = new ToolConfigs();
    @Configurable
    @Configurable.Comment("Config options for Mod Compatibility")
    public CompatibilityConfigs compat = new CompatibilityConfigs();
    @Configurable
    public DeveloperConfigs dev = new DeveloperConfigs();

    public static class RecipeConfigs {
        @Configurable
        @Configurable.Comment({"Whether to generate Flawed and Chipped Gems for materials and recipes involving them.",
                "Useful for mods like TerraFirmaCraft.", "Default: false"})
        public boolean generateLowQualityGems = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to remove Block/Ingot compression and decompression in the Crafting Table.", "Default: true"})
        public boolean disableManualCompression = true; // default true
        @Configurable
        @Configurable.Comment({"Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.", "Default: false"})
        public boolean harderRods = false; // default false
        @Configurable
        @Configurable.Comment({"Whether to make crafting recipes for Bricks, Firebricks, Nether Bricks, and Coke Bricks harder.", "Default: false"})
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
        @Configurable
        @Configurable.Comment({"Whether to remove Vanilla TNT Recipe from the Crafting Table.", "Default: true"})
        public boolean removeVanillaTNTRecipe = true; // default true
    }

    public static class CompatibilityConfigs {

        @Configurable
        @Configurable.Comment("Config options regarding GTEU compatibility with other energy systems")
        public EnergyCompatConfig energy = new EnergyCompatConfig();

        @Configurable.Comment("Config options regarding GTCEu compatibility with AE2")
        public AE2CompatConfig ae2 = new AE2CompatConfig();

        @Configurable
        @Configurable.Comment({"Whether to hide facades of all blocks in JEI and creative search menu.", "Default: true"})
        public boolean hideFacadesInJEI = true;

        @Configurable
        @Configurable.Comment({"Whether to hide filled cells in JEI and creative search menu.", "Default: true"})
        public boolean hideFilledCellsInJEI = true;

        @Configurable
        @Configurable.Comment({"Whether Gregtech should remove smelting recipes from the vanilla furnace for ingots requiring the Electric Blast Furnace.", "Default: true"})
        public boolean removeSmeltingForEBFMetals = true;

        public static class EnergyCompatConfig {

            @Configurable
            @Configurable.Comment({"Enable Native GTEU to Platform native Energy (RF and alike) on GT Cables and Wires.", "This does not enable nor disable Converters.", "Default: true"})
            public boolean nativeEUToPlatformNative = true;

            @Configurable
            @Configurable.Comment({"Enable GTEU to Platform native (and vice versa) Converters.", "Default: false"})
            public boolean enablePlatformConverters = false;

            @Configurable
            @Configurable.Comment({"Platform native Energy to GTEU ratio for converting FE to EU.", "Only affects converters.", "Default: 4 FE/Energy == 1 EU"})
            @Configurable.Range(min = 1, max = 16)
            public int platformToEuRatio = 4;

            @Configurable
            @Configurable.Comment({"GTEU to Platform native Energy ratio for converting EU to FE.", "Affects native conversion and Converters.", "Default: 4 FE/Energy == 1 EU"})
            @Configurable.Range(min = 1, max = 16)
            public int euToPlatformRatio = 4;
        }

        public static class AE2CompatConfig {
            @Configurable.Comment({"The interval between ME Hatch/Bus interact ME network.", "It may cause lag if the interval is too small.", "Default: 2 sec"})
            @Configurable.Range(min = 1, max = 80)
            public int updateIntervals = 40;

            @Configurable.Comment({"The energy consumption of ME Hatch/Bus.", "Default: 1.0AE/t"})
            @Configurable.DecimalRange(min = 0.0, max = 10.0)
            public double meHatchEnergyUsage = 1.0;
        }
    }

    public static class WorldGenConfigs {
        @Configurable
        @Configurable.Comment({"Rubber Tree spawn chance (% per chunk)", "Default: 0.5"})
        public float rubberTreeSpawnChance = 0.5f;

        @Configurable
        @Configurable.Comment({"Should all Stone Types drop unique Ore Item Blocks?", "Default: false (meaning only Stone, Netherrack, and Endstone)"})
        public boolean allUniqueStoneTypes = false;

        @Configurable
        @Configurable.Comment({"Should Sand-like ores fall?", "This includes gravel, sand, and red sand ores.", "Default: false (no falling ores)"})
        public boolean sandOresFall = false;

        @Configurable
        @Configurable.Comment({ "Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically.", "Default: true", "WARNING: Currently unimplemented." })
        public boolean increaseDungeonLoot = true;
        @Configurable
        @Configurable.Comment({ "Allow GregTech to add additional GregTech Items as loot in various structures.", "Default: true" })
        public boolean addLoot = true;

        @Configurable
        public OreVeinConfigs oreVeins = new OreVeinConfigs();

        public static class OreVeinConfigs {
            @Configurable
            @Configurable.Range(min = 1, max = 32)
            @Configurable.Comment({
                    "The grid size (in chunks) for ore vein generation",
                    "Default: 3"
            })
            public int oreVeinGridSize = 3;

            @Configurable
            @Configurable.Range(min = 0, max = 32 * 16)
            @Configurable.Comment({
                    "The maximum random offset (in blocks) from the grid for generating an ore vein.",
                    "Default: 12"
            })
            public int oreVeinRandomOffset = 12;

            @Configurable
            @Configurable.Comment({"Prevents regular vanilla ores from being generated outside GregTech ore veins", "Default: true"})
            public boolean removeVanillaOreGen = true;

            @Configurable
            @Configurable.Comment({"Prevents vanilla's large ore veins from being generated", "Default: true"})
            public boolean removeVanillaLargeOreVeins = true;


            @Configurable
            @Configurable.Comment({"Multiplier to bedrock ore generation amount", "Default: 1.0f"})
            public float bedrockOreMultiplier = 1.0f;
            @Configurable
            @Configurable.Comment({"Make bedrock ore/fluid veins infinite?", "Default: false"})
            public boolean infiniteBedrockOresFluids = false;

            @Configurable
            @Configurable.Comment({
                    "Sets the maximum number of chunks that may be cached for ore vein generation.",
                    "Higher values may improve world generation performance, but at the cost of more RAM usage.",
                    "If you substantially increase the ore vein grid size, random vein offset, or have very large (custom) veins, you may need to increase this value as well.",
                    "Default: 512 (requires restarting the server / re-opening the world)"
            })
            public int oreGenerationChunkCacheSize = 512;

            @Configurable
            @Configurable.Comment({
                    "Sets the maximum number of chunks for which ore indicators may be cached.",
                    "If you register any custom veins with very large indicator ranges (or modify existing ones that way), you may need to increase this value.",
                    "Default: 2048 (requires restarting the server / re-opening the world)"
            })
            public int oreIndicatorChunkCacheSize = 2048;
        }
    }

    public static class MachineConfigs {
        @Configurable
        @Configurable.Comment({"Whether insufficient energy supply should reset Machine recipe progress to zero.",
                "If true, progress will reset.", "If false, progress will decrease to zero with 2x speed", "Default: false"})
        public boolean recipeProgressLowEnergy = false;

        @Configurable
        @Configurable.Comment({"Whether to require a Wrench, Wirecutter, or other GregTech tools to break machines, casings, wires, and more.", "Default: false"})
        public boolean requireGTToolsForBlocks = false;
        @Configurable
        @Configurable.Comment({"Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava", "Default: false"})
        public boolean doTerrainExplosion = false;
        @Configurable
        @Configurable.Comment({ "Energy use multiplier for electric items.", "Default: 100" })
        public int energyUsageMultiplier = 100;
        @Configurable
        @Configurable.Comment({"Whether machines or boilers damage the terrain when they explode.",
                "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.", "Default: true"})
        public boolean doesExplosionDamagesTerrain = false;
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
        public boolean steelSteamMultiblocks = false;
        @Configurable
        @Configurable.Comment({"Whether to enable the cleanroom, required for various recipes.", "Default: true"})
        public boolean enableCleanroom = true;
        @Configurable
        @Configurable.Comment({"Whether multiblocks should ignore all cleanroom requirements.", "This does nothing if enableCleanroom is false.", "Default: false"})
        public boolean cleanMultiblocks = false;
        @Configurable
        @Configurable.Comment({"Block to replace mined ores with in the miner and multiblock miner.", "Default: minecraft:cobblestone"})
        public String replaceMinedBlocksWith = "minecraft:cobblestone";
        @Configurable
        @Configurable.Comment({"Whether to enable the Maintenance Hatch, required for Multiblocks.", "Default: true"})
        public boolean enableMaintenance = true;
        @Configurable
        @Configurable.Comment({"Whether the machine's circuit slot need to be inserted a real circuit."})
        public boolean ghostCircuit = true;
        @Configurable
        @Configurable.Comment({"Wether to add a \"Bedrock Ore Miner\" (also enables bedrock ore generation)", "Default: false"})
        public boolean doBedrockOres = false;
        @Configurable
        @Configurable.Comment({"What Kind of material should the bedrock ore miner output?", "Default: \"raw\""})
        public String bedrockOreDropTagPrefix = "raw";
        @Configurable
        @Configurable.Comment({"Wether to add a \"Processing Array\"", "Default: true"})
        public boolean doProcessingArray = true;
        @Configurable
        @Configurable.Comment({"Makes nearly every GCYM Multiblock require blocks which set their maximum voltages.",
                "Default: false"})
        public boolean enableTieredCasings = false;
        @Configurable
        @Configurable.Comment({"Minimum distance between Long Distance Item Pipe Endpoints", "Default: 50"})
        public int ldItemPipeMinDistance = 50;
        @Configurable
        @Configurable.Comment({"Minimum distance betweeb Long Distance Fluid Pipe Endpoints", "Default: 50"})
        public int ldFluidPipeMinDistance = 50;

        /**
         * <strong>Addons mods should not reference this config directly.</strong>
         * Use {@link GTCEuAPI#isHighTier()} instead.
         */
        @Configurable
        @Configurable.Comment({"If High Tier (>UV-tier) GT content should be registered.",
                "Items and Machines enabled with this config will have missing recipes by default.",
                "This is intended for modpack developers only, and is not playable without custom tweaks or addons.",
                "Other mods can override this to true, regardless of the config file.",
                "Default: false"})
        public boolean highTierContent = false;
        @Configurable
        @Configurable.Comment({"Whether search for recipes asynchronously.", " Default: true"})
        public boolean asyncRecipeSearching = true;
    }

    public static class ToolConfigs {
        @Configurable
        @Configurable.Comment({ "Random chance for electric tools to take actual damage", "Default: 10%" })
        @Configurable.Range(min = 0, max = 100)
        public int rngDamageElectricTools = 10;
    }

    public static class ClientConfigs {
        @Configurable
        @Configurable.Comment({"Whether or not to enable Emissive Textures for GregTech Machines.", "Default: true"})
        public boolean machinesEmissiveTextures = true;
        @Configurable
        @Configurable.Comment({ "Whether or not sounds should be played when using tools outside of crafting.", "Default: true" })
        public boolean toolUseSounds = true;
        @Configurable
        @Configurable.Comment({ "Whether or not sounds should be played when crafting with tools.", "Default: true" })
        public boolean toolCraftingSounds = true;
        @Configurable
        @Configurable.Comment({"The default color to overlay onto machines.", "#FFFFFF is no coloring (default).",
                "#D2DCFF is the classic blue from GT5."})
        @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,6}")
        @Configurable.Gui.ColorValue
        public String defaultPaintingColor = "#FFFFFF";
        @Configurable
        @Configurable.Comment({"Use VBO cache for multiblock preview.", "Disable it if you have issues with rendering multiblocks.", "Default: true"})
        @Configurable.Gui.ColorValue
        public boolean useVBO = true;
    }

    public static class DeveloperConfigs {
        @Configurable
        @Configurable.Comment({"Debug general events? (will print placed veins to server's debug.log)", "Default: false"})
        public boolean debug = false;
        @Configurable
        @Configurable.Comment({"Debug ore vein placement? (will print placed veins to server's debug.log)", "Default: false (no placement printout in debug.log)"})
        public boolean debugWorldgen = false;
        @Configurable
        @Configurable.Comment({"Dump all registered GT recipes?", "Default: false"})
        public boolean dumpRecipes = false;
        @Configurable
        @Configurable.Comment({"Dump all registered GT models/blockstates/etc?", "Default: false"})
        public boolean dumpAssets = false;
    }
}
