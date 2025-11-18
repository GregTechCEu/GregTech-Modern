package com.gregtechceu.gtceu.config;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;

import net.minecraft.commands.Commands;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GTCEu.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
            }
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
    @Configurable.Comment("Config options for Game Mechanics")
    public GameplayConfigs gameplay = new GameplayConfigs();
    @Configurable
    @Configurable.Comment("Config options for Mod Compatibility")
    public CompatibilityConfigs compat = new CompatibilityConfigs();
    @Configurable
    public DeveloperConfigs dev = new DeveloperConfigs();

    public static class RecipeConfigs {

        @Configurable
        @Configurable.Comment({ "Whether to generate Flawed and Chipped Gems for materials and recipes involving them.",
                "Useful for mods like TerraFirmaCraft.", "Default: false" })
        public boolean generateLowQualityGems = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to remove Block/Ingot compression and decompression in the Crafting Table.",
                "Default: true" })
        public boolean disableManualCompression = true; // default true
        @Configurable
        @Configurable.Comment({
                "Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.",
                "Default: true" })
        public boolean harderRods = true; // default true
        @Configurable
        @Configurable.Comment({
                "Whether to make crafting recipes for Bricks, Firebricks, Nether Bricks, and Coke Bricks harder.",
                "Default: false" })
        public boolean harderBrickRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to nerf Wood crafting to 2 Planks from 1 Log, and 2 Sticks from 2 Planks.",
                "Default: false" })
        public boolean nerfWoodCrafting = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to make Wood related recipes harder.", "Excludes sticks and planks.",
                "Default: false" })
        public boolean hardWoodRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Recipes for Buckets, Cauldrons, Hoppers, and Iron Bars" +
                " require Iron Plates, Rods, and more.", "Default: true" })
        public boolean hardIronRecipes = true; // default true
        @Configurable
        @Configurable.Comment({ "Whether to make Redstone related recipes harder.", "Default: false" })
        public boolean hardRedstoneRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to make Vanilla Tools and Armor recipes harder.",
                "Excludes Flint and Steel, and Buckets.", "Default: false" })
        public boolean hardToolArmorRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to make miscellaneous recipes harder.", "Default: false" })
        public boolean hardMiscRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to make Glass related recipes harder. Default: true" })
        public boolean hardGlassRecipes = true; // default true
        @Configurable
        @Configurable.Comment({ "Whether to nerf the Paper crafting recipe.", "Default: true" })
        public boolean nerfPaperCrafting = true; // default true
        @Configurable
        @Configurable.Comment({ "Recipes for items like Iron Doors, Trapdoors, Anvil" +
                " require Iron Plates, Rods, and more.", "Default: true" })
        public boolean hardAdvancedIronRecipes = true; // default true
        @Configurable
        @Configurable.Comment({ "Whether to make coloring blocks like Concrete or Glass harder.", "Default: false" })
        public boolean hardDyeRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to remove charcoal smelting recipes from the vanilla furnace.",
                "Default: true" })
        public boolean harderCharcoalRecipe = true; // default true
        @Configurable
        @Configurable.Comment({ "Whether to make the Flint and Steel recipe require steel parts.", "Default: true." })
        public boolean flintAndSteelRequireSteel = true; // default true
        @Configurable
        @Configurable.Comment({ "Whether to remove Vanilla Block Recipes from the Crafting Table.", "Default: false" })
        public boolean removeVanillaBlockRecipes = false; // default false
        @Configurable
        @Configurable.Comment({ "Whether to remove Vanilla TNT Recipe from the Crafting Table.", "Default: true" })
        public boolean removeVanillaTNTRecipe = true; // default true
        @Configurable
        @Configurable.Comment({ "How many Multiblock Casings to make per craft. Either 1, 2, or 3.", "Default: 2" })
        @Configurable.Range(min = 1, max = 3)
        public int casingsPerCraft = 2;
        @Configurable
        @Configurable.Comment({
                "Whether to nerf the output amounts of the first circuit in a set to 1 (from 2) and SoC to 2 (from 4).",
                "Default: false" })
        public boolean harderCircuitRecipes = false;
        @Configurable
        @Configurable.Comment({ "Whether to nerf machine controller recipes.", "Default: false" })
        public boolean hardMultiRecipes = false; // default false
        @Configurable
        @Configurable.Comment({
                "Whether tools should have enchants or not. Like the flint sword getting fire aspect.",
                "Default: false" })
        public boolean enchantedTools = false;

        @Configurable
        @Configurable.Comment({ "Whether to enable macerator decomposition recycling", "Default: true" })
        public boolean enableMaceratorRecycling = true;
        @Configurable
        @Configurable.Comment({ "Percentage yield of macerator decomposition recycling outputs, 1.0 means 100%",
                "Default: 1.0f" })
        @Configurable.DecimalRange(min = 0.0f, max = 1.0f)
        public float maceratorRecyclingYield = 1.0f;
        @Configurable
        @Configurable.Comment({ "Whether to enable arc furnace decomposition recycling", "Default: true" })
        public boolean enableArcRecycling = true;
        @Configurable
        @Configurable.Comment({ "Percentage yield of arc furnace decomposition recycling outputs, 1.0 means 100%",
                "Default: 1.0f" })
        @Configurable.DecimalRange(min = 0.0f, max = 1.0f)
        public float arcRecyclingYield = 1.0f;
        @Configurable
        @Configurable.Comment({ "Whether to enable extractor decomposition recycling", "Default: true" })
        public boolean enableExtractorRecycling = true;
        @Configurable
        @Configurable.Comment({ "Percentage yield of extractor decomposition recycling outputs, 1.0 means 100%",
                "Default: 1.0f" })
        @Configurable.DecimalRange(min = 0.0f, max = 1.0f)
        public float extractorRecyclingYield = 1.0f;
    }

    public static class CompatibilityConfigs {

        @Configurable
        @Configurable.Comment("Config options regarding GTEU compatibility with other energy systems")
        public EnergyCompatConfig energy = new EnergyCompatConfig();

        @Configurable
        @Configurable.Comment("Config options regarding GTCEu compatibility with AE2")
        public AE2CompatConfig ae2 = new AE2CompatConfig();

        @Configurable
        @Configurable.Comment("Config options regarding GTCEu compatibility with minimap mods")
        public MinimapCompatConfig minimap = new MinimapCompatConfig();

        @Configurable
        @Configurable.Comment({ "Whether to hide facades of all blocks in JEI and creative search menu.",
                "Default: true" })
        // todo: implement or purge
        public boolean hideFacadesInRecipeViewer = true;

        @Configurable
        @Configurable.Comment({ "Whether to hide filled cells in JEI and creative search menu.", "Default: true" })
        // todo: implement or purge
        public boolean hideFilledCellsInRecipeViewer = true;

        @Configurable
        @Configurable.Comment({ "Whether to hide the ore processing diagrams in JEI", "Default: false" })
        public boolean hideOreProcessingDiagrams = false;

        @Configurable
        @Configurable.Comment({
                "Whether Gregtech should remove smelting recipes from the vanilla furnace for ingots requiring the Electric Blast Furnace.",
                "Default: true" })
        // todo: implement or purge
        public boolean removeSmeltingForEBFMetals = true;

        @Configurable
        @Configurable.Comment({ "Whether dimension markers should show the dimension tier value.", "Default: false" })
        public boolean showDimensionTier = false;

        @Configurable
        @Configurable.Comment({ "Whether Create compatibility will be available.", "Default: true" })
        public boolean createCompat = true;

        public static class EnergyCompatConfig {

            @Configurable
            @Configurable.Comment({ "Enable Native GTEU to Forge Energy (RF and alike) on GT Cables and Wires.",
                    "This does not enable nor disable Converters.", "Default: true" })
            public boolean nativeEUToFE = true;

            @Configurable
            @Configurable.Comment({ "Enable GTEU to FE (and vice versa) Converters.", "Default: false" })
            public boolean enableFEConverters = false;

            @Configurable
            @Configurable.Comment({ "Forge Energy to GTEU ratio for converting FE to EU.", "Only affects converters.",
                    "Default: 4 FE == 1 EU" })
            @Configurable.Range(min = 1, max = Integer.MAX_VALUE)
            public int feToEuRatio = 4;

            @Configurable
            @Configurable.Comment({ "GTEU to Forge Energy ratio for converting EU to FE.",
                    "Affects native conversion and Converters.", "Default: 4 FE == 1 EU" })
            @Configurable.Range(min = 1, max = Integer.MAX_VALUE)
            public int euToFeRatio = 4;
        }

        public static class AE2CompatConfig {

            @Configurable
            @Configurable.Comment({ "The interval between ME Hatch/Bus interact ME network.",
                    "It may cause lag if the interval is too small.", "Default: 2 sec" })
            @Configurable.Range(min = 1) // Do Not Set a Maximum, if someone wants >80 ticks let them.
            public int updateIntervals = 40;

            @Configurable
            @Configurable.Comment({ "The energy consumption of ME Hatch/Bus.", "Default: 4.0AE/t" })
            @Configurable.DecimalRange(min = 0.0, max = Integer.MAX_VALUE)
            public double meHatchEnergyUsage = 4.0;
        }

        public static class MinimapCompatConfig {

            @Configurable
            @Configurable.Comment({
                    "Toggle specific map mod integration on/off (need to restart for this to take effect)" })
            public Toggle toggle = new Toggle();

            @Configurable
            @Configurable.Comment({ "The radius, in blocks, that picking up a surface rock will search for veins in.",
                    "-1 to disable.", "Default: 24" })
            @Configurable.Range(min = -1)
            public int surfaceRockProspectRange = 24;
            @Configurable
            @Configurable.Comment({ "The radius, in blocks, that clicking an ore block will search for veins in.",
                    "-1 to disable", "Default: 24" })
            @Configurable.Range(min = -1)
            public int oreBlockProspectRange = 24;

            @Configurable
            @Configurable.Comment("The map scale at which displayed ores will stop scaling.")
            @Configurable.DecimalRange(min = 0.1, max = 16)
            // todo: implement or purge
            public float oreScaleStop = 1;

            @Configurable
            @Configurable.Comment("The size, in pixels, of ore icons on the map")
            @Configurable.Range(min = 4)
            public int oreIconSize = 32;

            @Configurable
            @Configurable.Comment("The string prepending ore names in the ore vein tooltip")
            public String oreNamePrefix = "- ";

            @Configurable
            @Configurable.Comment({ "The color to draw a box around the ore icon with.",
                    "Accepts either an ARGB hex color prefixed with # or the string 'material' to use the ore's material color" })
            public String borderColor = "#00000000";

            @Configurable
            @Configurable.Comment({ "Which part of the screen to anchor buttons to", "Default: \"BOTTOM_LEFT\"" })
            public Anchor buttonAnchor = Anchor.BOTTOM_LEFT;

            @Configurable
            @Configurable.Comment({ "Which direction the buttons will go", "Default: \"VERTICAL\"" })
            public Direction direction = Direction.VERTICAL;

            @Configurable
            @Configurable.Comment({ "How horizontally far away from the anchor to place the buttons", "Default: 20" })
            public int xOffset = 20;

            @Configurable
            @Configurable.Comment({ "How vertically far away from the anchor to place the buttons", "Default: 0" })
            public int yOffset = 0;

            public static class Toggle {

                @Configurable
                @Configurable.Comment({ "FTB Chunks integration enabled" })
                public boolean ftbChunksIntegration = false;

                @Configurable
                @Configurable.Comment({ "Journey Map integration enabled" })
                public boolean journeyMapIntegration = true;

                @Configurable
                @Configurable.Comment({ "Xaerox's map integration enabled" })
                public boolean xaerosMapIntegration = true;
            }

            public enum Anchor {

                TOP_LEFT,
                TOP_CENTER,
                TOP_RIGHT,
                RIGHT_CENTER,
                BOTTOM_RIGHT,
                BOTTOM_CENTER,
                BOTTOM_LEFT,
                LEFT_CENTER;

                public boolean isCentered() {
                    return this == TOP_CENTER || this == RIGHT_CENTER || this == BOTTOM_CENTER || this == LEFT_CENTER;
                }

                public Direction usualDirection() {
                    return switch (this) {
                        case TOP_CENTER, BOTTOM_CENTER -> Direction.HORIZONTAL;
                        case RIGHT_CENTER, LEFT_CENTER -> Direction.VERTICAL;
                        default -> null;
                    };
                }
            }

            public enum Direction {
                VERTICAL,
                HORIZONTAL
            }

            public int getBorderColor(int materialColor) {
                if (borderColor.equals("material")) {
                    return materialColor;
                }
                // please java may I have an unsigned int
                try {
                    long tmp = Long.decode(borderColor);
                    if (tmp > 0x7FFFFFFF) {
                        tmp -= 0x100000000L;
                    }
                    return (int) tmp;
                } catch (NumberFormatException e) {
                    return 0x00000000;
                }
            }
        }
    }

    public static class WorldGenConfigs {

        @Configurable
        @Configurable.Comment({ "Rubber Tree spawn chance (decimal % per chunk)", "Default: 0.5" })
        @Configurable.DecimalRange(min = 0f, max = 1f)
        public float rubberTreeSpawnChance = 0.5f;

        @Configurable
        @Configurable.Comment({ "Should Sand-like ores fall?", "This includes gravel, sand, and red sand ores.",
                "Default: false (no falling ores)" })
        public boolean sandOresFall = false;

        @Configurable
        @Configurable.Comment({
                "Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically.",
                "Default: true", "WARNING: Currently unimplemented." })
        public boolean increaseDungeonLoot = true;
        @Configurable
        @Configurable.Comment({ "Allow GregTech to add additional GregTech Items as loot in various structures.",
                "Default: true" })
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
            @Configurable.Comment({ "Prevents regular vanilla ores from being generated outside GregTech ore veins",
                    "Default: true" })
            public boolean removeVanillaOreGen = true;
            @Configurable
            @Configurable.Comment({ "Prevents vanilla's large ore veins from being generated", "Default: true" })
            public boolean removeVanillaLargeOreVeins = true;
            @Configurable
            @Configurable.Comment({ "Distance between bedrock ore veins in chunks, if enabled.", "Default: 16" })
            public int bedrockOreDistance = 16;
            @Configurable
            @Configurable.Comment({ "Make bedrock ore/fluid veins infinite?", "Default: false" })
            public boolean infiniteBedrockOresFluids = false;
            @Configurable
            @Configurable.Comment({ "Generate ores indicators above ore veins", "Default: true" })
            public boolean oreIndicators = true;
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
        @Configurable.Comment({
                "Whether to require a Wrench, Wirecutter, or other GregTech tools to break machines, casings, wires, and more.",
                "Default: true" })
        public boolean requireGTToolsForBlocks = true;
        @Configurable
        @Configurable.Comment({
                "Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava",
                "Default: false" })
        public boolean shouldWeatherOrTerrainExplosion = false;
        @Configurable
        @Configurable.Comment({ "Energy use multiplier for electric items.", "Default: 100" })
        public int energyUsageMultiplier = 100;

        @Configurable
        @Configurable.Comment({ "Energy use multiplier for prospectors.", "Default: 100" })
        public int prospectorEnergyUseMultiplier = 100;
        @Configurable
        @Configurable.Comment({ "Whether machines or boilers damage the terrain when they explode.",
                "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.",
                "Default: true" })
        public boolean doesExplosionDamagesTerrain = true;
        @Configurable
        @Configurable.Comment({
                "Enables Safe Active Transformers, removing their ability to explode if unformed while transmitting/receiving power.",
                "Default: false" })
        public boolean harmlessActiveTransformers = false;
        @Configurable
        @Configurable.Comment({ "Whether to play machine sounds while machines are active.", "Default: true" })
        public boolean machineSounds = true;
        @Configurable
        @Configurable.Comment({ "Duration in ticks that batching will try to reach.", "Default: 100" })
        public int batchDuration = 100;
        @Configurable
        @Configurable.Comment({ "Whether Steam Multiblocks should use Steel instead of Bronze.", "Default: false" })
        public boolean steelSteamMultiblocks = false;
        @Configurable
        @Configurable.Comment({ "Whether to enable the cleanroom, required for various recipes.", "Default: true" })
        public boolean enableCleanroom = true;
        @Configurable
        @Configurable.Comment({ "Whether multiblocks should ignore all cleanroom requirements.",
                "This does nothing if enableCleanroom is false.", "Default: false" })
        public boolean cleanMultiblocks = false;
        @Configurable
        @Configurable.Comment({
                "Whether the miner should attempt to replace the block mined with a cobbled version of the ore",
                "Default: true" })
        public boolean replaceWithCobbleVersion = true;
        @Configurable
        @Configurable.Comment({ "Block to replace mined ores with in the miner and multiblock miner.",
                "Default: minecraft:cobblestone" })
        public String replaceMinedBlocksWith = "minecraft:cobblestone";
        @Configurable
        @Configurable.Comment({ "Whether to enable Assembly Line research for recipes.", "Default: true" })
        public boolean enableResearch = true;
        @Configurable
        @Configurable.Comment({ "Whether to enable the Maintenance Hatch, required for Multiblocks.", "Default: true" })
        public boolean enableMaintenance = true;
        @Configurable
        @Configurable.Comment({
                "How often to check for maintenance, rolling a 1/6000 chance every X ticks (before secondary effects like Configurable Maintenance Hatch).",
                "In default settings, this equates to a 5% chance every hour of a machine running.",
                "Default: 1000 (ticks)" })
        public int maintenanceCheckRate = 1000;

        @Configurable
        @Configurable.Comment({
                "Whether to enable World Accelerators, which accelerate ticks for surrounding Tile Entities, Crops, etc.",
                "Default: true" })
        public boolean enableWorldAccelerators = true;

        @Configurable
        @Configurable.Comment({ "List of TileEntities that the World Accelerator should not accelerate.",
                "GregTech TileEntities are always blocked.",
                "Entries must be in a fully qualified format. For example: appeng.tile.networking.TileController",
                "Default: none" })
        public String[] worldAcceleratorBlacklist = new String[0];

        @Configurable
        @Configurable.Comment({
                "Whether to use GT6-style pipe and cable connections, meaning they will not auto-connect " +
                        "unless placed directly onto another pipe or cable.",
                "Default: true" })
        public boolean gt6StylePipesCables = true;
        @Configurable
        @Configurable.Comment({ "Whether the machine's circuit slot need to be inserted a real circuit." })
        public boolean ghostCircuit = true;
        @Configurable
        @Configurable.Comment({ "Whether to add a \"Bedrock Ore Miner\" (also enables bedrock ore generation)",
                "Default: false" })
        public boolean doBedrockOres = false;
        @Configurable
        @Configurable.Comment({ "What Kind of material should the bedrock ore miner output?", "Default: \"raw\"" })
        public String bedrockOreDropTagPrefix = "raw";
        @Configurable
        @Configurable.Range(min = 120, max = 800)
        @Configurable.Comment({ "The base amount of ticks per block for electric singleblock ore miners",
                "Default: 320" })
        public int minerSpeed = 320;
        @Configurable
        @Configurable.Comment({ "Makes nearly every GCYM Multiblock require blocks which set their maximum voltages.",
                "Default: false" })
        // todo: implement or purge
        public boolean enableTieredCasings = false;
        @Configurable
        @Configurable.Comment({ "Minimum distance between Long Distance Item Pipe Endpoints", "Default: 50" })
        public int ldItemPipeMinDistance = 50;
        @Configurable
        @Configurable.Comment({ "Minimum distance betweeb Long Distance Fluid Pipe Endpoints", "Default: 50" })
        public int ldFluidPipeMinDistance = 50;

        @Configurable
        @Configurable.Comment({ "Whether ONLY owners can open a machine gui", "Default: false" })
        public boolean onlyOwnerGUI = false;
        @Configurable
        @Configurable.Comment({ "Whether ONLY owners can break a machine", "Default: false" })
        public boolean onlyOwnerBreak = false;
        @Configurable
        @Configurable.Comment({ "Minimum op level to bypass the ownership checks", "Default: 2" })
        @Configurable.Range(min = Commands.LEVEL_ALL, max = Commands.LEVEL_OWNERS)
        public int ownerOPBypass = Commands.LEVEL_GAMEMASTERS;

        /**
         * <strong>Addons mods should not reference this config directly.</strong>
         * Use {@link GTCEuAPI#isHighTier()} instead.
         */
        @Configurable
        @Configurable.Comment({ "If High Tier (>UV-tier) GT content should be registered.",
                "Items and Machines enabled with this config will have missing recipes by default.",
                "This is intended for modpack developers only, and is not playable without custom tweaks or addons.",
                "Other mods can override this to true, regardless of the config file.",
                "Default: false" })
        public boolean highTierContent = false;

        @Configurable
        @Configurable.Comment({ "Whether the Assembly Line should require the item inputs to be in order.",
                "Default: true" })
        public boolean orderedAssemblyLineItems = true;
        @Configurable
        @Configurable.Comment({ "Whether the Assembly Line should require the fluid inputs to be in order.",
                "(Requires Ordered Assembly Line Item Inputs to be enabled.)", "Default: true" })
        public boolean orderedAssemblyLineFluids = true;

        @Configurable
        @Configurable.Comment({
                "Default maximum parallel of steam multiblocks",
                "Default: 8"
        })
        public int steamMultiParallelAmount = 8;

        @Configurable
        @Configurable.Comment("Whether the Drums can input fluids from the output side (bottom).")
        public boolean allowDrumsInputFluidsFromOutputSide = false;

        @Configurable
        @Configurable.Comment("Small Steam Boiler Options")
        public SmallBoilers smallBoilers = new SmallBoilers();
        @Configurable
        @Configurable.Comment("Large Steam Boiler Options")
        public LargeBoilers largeBoilers = new LargeBoilers();

        public static class SmallBoilers {

            @Configurable
            @Configurable.Comment({ "The amount of steam a Steam Solid Boiler produces per second at max temperature.",
                    "Default: 120" })
            public int solidBoilerBaseOutput = 120;
            @Configurable
            @Configurable.Comment({
                    "The amount of steam a High Pressure Steam Solid Boiler produces per second at max temperature.",
                    "Default: 300" })
            public int hpSolidBoilerBaseOutput = 300;
            @Configurable
            @Configurable.Comment({ "The amount of steam a Steam Liquid Boiler produces per second at max temperature.",
                    "Default: 240" })
            public int liquidBoilerBaseOutput = 240;
            @Configurable
            @Configurable.Comment({
                    "The amount of steam a High Pressure Steam Liquid Boiler produces per second at max temperature.",
                    "Default: 600" })
            public int hpLiquidBoilerBaseOutput = 600;
            @Configurable
            @Configurable.Comment({ "The amount of steam a Steam Solar Boiler produces per second at max temperature.",
                    "Default: 120" })
            public int solarBoilerBaseOutput = 120;
            @Configurable
            @Configurable.Comment({
                    "The amount of steam a High Pressure Steam Solar Boiler produces per second at max temperature.",
                    "Default: 360" })
            public int hpSolarBoilerBaseOutput = 360;
        }

        public static class LargeBoilers {

            @Configurable
            @Configurable.Comment({ "The conversion rate between water and steam in Large Boilers.", "Default: 160" })
            public int steamPerWater = 160;
            @Configurable
            @Configurable.Comment({ "The max temperature of the Large Bronze Boiler.", "Default: 800" })
            public int bronzeBoilerMaxTemperature = 800;
            @Configurable
            @Configurable.Comment({ "The heat speed of the Large Bronze Boiler.", "Default: 1" })
            public int bronzeBoilerHeatSpeed = 1;
            @Configurable
            @Configurable.Comment({ "The max temperature of the Large Steel Boiler.", "Default: 1800" })
            public int steelBoilerMaxTemperature = 1800;
            @Configurable
            @Configurable.Comment({ "The heat speed of the Large Steel Boiler.", "Default: 1" })
            public int steelBoilerHeatSpeed = 1;
            @Configurable
            @Configurable.Comment({ "The max temperature of the Large Titanium Boiler.", "Default: 3200" })
            public int titaniumBoilerMaxTemperature = 3200;
            @Configurable
            @Configurable.Comment({ "The heat speed of the Large Titanium Boiler.", "Default: 1" })
            public int titaniumBoilerHeatSpeed = 1;
            @Configurable
            @Configurable.Comment({ "The max temperature of the Large Tungstensteel Boiler.", "Default: 6400" })
            public int tungstensteelBoilerMaxTemperature = 6400;
            @Configurable
            @Configurable.Comment({ "The heat speed of the Large Tungstensteel Boiler.", "Default: 2" })
            public int tungstensteelBoilerHeatSpeed = 2;
        }
    }

    public static class ToolConfigs {

        @Configurable
        @Configurable.Comment({ "Random chance for electric tools to take actual damage", "Default: 10%" })
        @Configurable.Range(min = 0, max = 100)
        public int rngDamageElectricTools = 10;
        @Configurable
        @Configurable.Comment({ "Amount of blocks that can be spray painted at once", "Default: 16" })
        @Configurable.Range(min = 1, max = 512)
        public int sprayCanChainLength = 16;
        @Configurable
        @Configurable.Comment({ "Delay in ticks between each log being broken when tree felling", "Default: 2" })
        @Configurable.Range(min = 1, max = 400)
        public int treeFellingDelay = 2;
        @Configurable
        @Configurable.Comment("NanoSaber Options")
        public NanoSaber nanoSaber = new NanoSaber();
        @Configurable
        @Configurable.Comment("NightVision Goggles Voltage Tier. Default: 1 (LV)")
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierNightVision = 1;
        @Configurable
        @Configurable.Comment("NanoSuit Voltage Tier. Default: 3 (HV)")
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierNanoSuit = 3;
        @Configurable
        @Configurable.Comment({ "Advanced NanoSuit Chestplate Voltage Tier.", "Default: 3 (HV)" })
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierAdvNanoSuit = 3;
        @Configurable
        @Configurable.Comment({ "QuarkTech Suit Voltage Tier.", "Default: 5 (IV)" })
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierQuarkTech = 5;
        @Configurable
        @Configurable.Comment({ "Advanced QuarkTech Suit Chestplate Voltage Tier.", "Default: 5 (LuV)" })
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierAdvQuarkTech = 6;
        @Configurable
        @Configurable.Comment({ "Electric Impeller Jetpack Voltage Tier.", "Default: 2 (MV)" })
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierImpeller = 2;
        @Configurable
        @Configurable.Comment({ "Advanced Electric Jetpack Voltage Tier.", "Default: 3 (HV)" })
        @Configurable.Range(min = 0, max = 14)
        public int voltageTierAdvImpeller = 3;

        public static class NanoSaber {

            @Configurable
            @Configurable.DecimalRange(min = 0, max = 100)
            @Configurable.Comment({ "The additional damage added when the NanoSaber is powered.", "Default: 20.0" })
            public double nanoSaberDamageBoost = 20;
            @Configurable
            @Configurable.DecimalRange(min = 0, max = 100)
            @Configurable.Comment({ "The base damage of the NanoSaber.", "Default: 5.0" })
            public double nanoSaberBaseDamage = 5;
            @Configurable
            @Configurable.Comment({ "Should Zombies spawn with charged, active NanoSabers on hard difficulty?",
                    "Default: true" })
            public boolean zombieSpawnWithSabers = true;
            @Configurable
            @Configurable.Range(min = 1, max = 512)
            @Configurable.Comment({ "The EU/t consumption of the NanoSaber.", "Default: 64" })
            public int energyConsumption = 64;
        }
    }

    public static class GameplayConfigs {

        @Configurable
        @Configurable.Comment({ "Enable hazardous materials", "Default: true" })
        public boolean hazardsEnabled = true;
        @Configurable
        @Configurable.Comment({ "Whether hazards are applied to all valid items, or just GT's.",
                "true = all, false = GT only.", "Default: true" })
        public boolean universalHazards = true;
        @Configurable
        @Configurable.Comment({ "Whether environmental hazards like pollution or radiation are active",
                "Default: false" })
        public boolean environmentalHazards = false;
        @Configurable
        @Configurable.Comment({ "How much environmental hazards decay per chunk, per tick.",
                "Default: 0.001" })
        public float environmentalHazardDecayRate = 0.001f;
    }

    public static class ClientConfigs {

        @Configurable
        @Configurable.Comment({ "Whether or not to enable Emissive Textures for GregTech Machines.", "Default: true" })
        public boolean machinesEmissiveTextures = true;
        @Configurable
        @Configurable.Comment({
                "Whether most machines will have block entity renderers, mainly used for rendering certain covers. (Restart required)",
                "Disable if experiencing performance issues.", "Default: true" })
        public boolean machinesHaveBERsByDefault = true;
        @Configurable
        @Configurable.Comment({ "Whether or not sounds should be played when using tools outside of crafting.",
                "Default: true" })
        public boolean toolUseSounds = true;
        @Configurable
        @Configurable.Comment({ "Whether or not sounds should be played when crafting with tools.", "Default: true" })
        public boolean toolCraftingSounds = true;
        @Configurable
        @Configurable.Comment({ "The default color to overlay onto machines.",
                "#FFFFFF is no coloring (default).",
                "#D2DCFF is the classic blue from GT5." })
        @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,6}")
        @Configurable.Gui.ColorValue
        public String defaultPaintingColor = "#FFFFFF";
        @Configurable
        @Configurable.Comment({ "The default color to overlay onto Machine (and other) UIs.",
                "#FFFFFF is no coloring (like GTCE) (default).",
                "#D2DCFF is the classic blue from GT5." })
        @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,6}")
        @Configurable.Gui.ColorValue
        public String defaultUIColor = "#FFFFFF";
        @Configurable
        @Configurable.Comment({ "Use VBO cache for multiblock preview.",
                "Disable if you have issues with rendering multiblocks.", "Default: true" })
        public boolean useVBO = true;
        @Configurable
        @Configurable.Comment({ "Duration of the multiblock in-world preview (s)", "Default: 10" })
        @Configurable.Range(min = 1, max = 999)
        public int inWorldPreviewDuration = 10;
        @Configurable
        @Configurable.Comment({ "Duration of UI animations in ms", "Default: 300" })
        @Configurable.Range(min = 1)
        public int animationTime = 300;
        @Configurable
        public ArmorHud armorHud = new ArmorHud();
        @Configurable
        public RendererConfigs renderer = new RendererConfigs();
        @Configurable
        public TankItemFluidPreview tankItemFluidPreview = new TankItemFluidPreview();

        public int getDefaultPaintingColor() {
            // OR with full alpha to differentiate from a machine that's painted white (map color 0xffffff)
            return Long.decode(defaultPaintingColor).intValue() | 0xff000000;
        }

        public static class ArmorHud {

            @Configurable
            @Configurable.Comment({ "Sets HUD location", "1 - left-upper corner", "2 - right-upper corner",
                    "3 - left-bottom corner", "4 - right-bottom corner", "Default: 1" })
            @Configurable.Range(min = 1, max = 4)
            public int hudLocation = 1;
            @Configurable
            @Configurable.Comment({ "Horizontal offset of HUD.", "Default: 0" })
            @Configurable.Range(min = 0, max = 100)
            public int hudOffsetX = 0;
            @Configurable
            @Configurable.Comment({ "Vertical offset of HUD.", "Default: 0" })
            @Configurable.Range(min = 0, max = 100)
            public int hudOffsetY = 0;
        }

        public static class TankItemFluidPreview {

            @Configurable
            @Configurable.Comment({ "Set true to render the including fluid icons to GT Drums" })
            public boolean drum = false;
            @Configurable
            @Configurable.Comment({ "Set true to render the including fluid icons to Super (Quantum) Tanks" })
            public boolean quantumTank = false;
        }
    }

    public static class DeveloperConfigs {

        @Configurable
        @Configurable.Comment({ "Debug general events? (will print recipe conficts etc. to server's debug.log)",
                "Default: false" })
        public boolean debug = false;
        @Configurable
        @Configurable.Comment({ "Debug ore vein placement? (will print placed veins to server's debug.log)",
                "Default: false (no placement printout in debug.log)" })
        public boolean debugWorldgen = false;
        @Configurable
        @Configurable.Comment({ "Generate ores in superflat worlds?", "Default: false" })
        public boolean doSuperflatOres = false;
        @Configurable
        @Configurable.Comment({ "Dump all registered GT recipes?", "Default: false" })
        public boolean dumpRecipes = false;
        @Configurable
        @Configurable.Comment({ "Dump all registered GT models/blockstates/etc?", "Default: false" })
        public boolean dumpAssets = false;
    }

    public static class RendererConfigs {

        @Configurable
        @Configurable.Comment({ "Render fluids in multiblocks that support them?", "Default: true" })
        public boolean renderFluids = true;

        @Configurable
        @Configurable.Comment({ "Render growing plants in multiblocks that support them?", "Default: true" })
        public boolean renderGrowingPlants = true;

        @Configurable
        @Configurable.Comment({ "Whether or not to color tiered machine highlights in the tier color",
                "Default: true" })
        public boolean coloredTieredMachineOutline = true;

        @Configurable
        @Configurable.Comment({ "Whether or not to color wire/cable highlights based on voltage tier",
                "Default: true" })
        public boolean coloredWireOutline = true;
    }
}
