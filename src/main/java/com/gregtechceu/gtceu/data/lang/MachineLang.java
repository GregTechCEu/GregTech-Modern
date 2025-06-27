package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import net.minecraft.ChatFormatting;

public class MachineLang {

    public static void init(GTLangProvider provider) {
        generateSteamMachines(provider);

        generateGeneralMachineTooltips(provider);
        generateGeneralMultiblockTooltips(provider);

        generateCustomPerTierMachineTooltips(provider);
        generateCustomMachineTooltips(provider);

        generateCustomMultiblockTooltips(provider);
        generateCustomMultiblockPartTooltips(provider);

        generateMultiblockKeys(provider);
    }

    public static void standardTooltips(GTLangProvider provider, String root, String machine,
                                        String lowTier, String midTier, String highTier) {
        makeElectricMachineTooltipULVtoMAX(provider, root, machine,
                lowTier, lowTier, lowTier, lowTier,
                lowTier, midTier, midTier, midTier,
                highTier, highTier, highTier, highTier,
                highTier, highTier, highTier);
    }

    private static void generateSteamMachines(GTLangProvider provider) {
        makeSteamMachineTooltip(provider, "gtceu", "steam_solid_boiler",
                "An early way to get Steam Power", "Faster than the Small Steam Solid Fuel Boiler");
        makeSteamMachineTooltip(provider, "gtceu", "steam_solar_boiler",
                "Steam Power by Sun", "Steam Power by Sun");
        makeSteamMachineTooltip(provider, "gtceu", "steam_liquid_boiler",
                "A Boiler running off Liquids", "Faster than Small Steam Liquid Boiler");
        makeSteamMachineTooltip(provider, "gtceu", "steam_extractor",
                "Extracting your first Rubber", "Extracting your first Rubber");
        makeSteamMachineTooltip(provider, "gtceu", "steam_macerator",
                "Macerating your Ores", "Macerating your Ores");
        makeSteamMachineTooltip(provider, "gtceu", "steam_compressor",
                "Compressing Items", "Compressing Items");
        makeSteamMachineTooltip(provider, "gtceu", "steam_forge_hammer",
                "Forge Hammer", "Forge Hammer");
        makeSteamMachineTooltip(provider, "gtceu", "steam_furnace",
                "Smelting things with compressed Steam", "Smelting things with compressed Steam");
        makeSteamMachineTooltip(provider, "gtceu", "steam_alloy_smelter",
                "Combination Smelter", "Combination Smelter");
        makeSteamMachineTooltip(provider, "gtceu", "steam_rock_crusher",
                "Place Water and Lava horizontally adjacent", "Place Water and Lava horizontally adjacent");
        makeSteamMachineTooltip(provider, "gtceu", "steam_miner",
                "Mines ores below the Miner!", "Mines ores below the Miner!");

        // unused
        provider.add("block.gtceu.steam_solid_boiler.bronze", "Small Steam Solid Boiler");
        provider.add("block.gtceu.steam_liquid_boiler.bronze", "Small Steam Liquid Boiler");
    }

    private static void generateGeneralMachineTooltips(GTLangProvider provider) {
        // General Machine Behavior
        provider.add("machine.gtceu.basic.input_from_output_side.allow", "Allow Input from Output Side: ");
        provider.add("machine.gtceu.basic.input_from_output_side.disallow",
                "Disallow Input from Output Side: ");
        provider.add("machine.gtceu.muffle.on", "Sound Muffling: Enabled");
        provider.add("machine.gtceu.muffle.off", "Sound Muffling: Disabled");
        provider.add("machine.gtceu.perfect_oc", "Does not lose energy efficiency when overclocked.");
    }

    /**
     * For Tooltips that are NOT in the form {@code mod.machine.tier_name.tooltip}
     */
    private static void generateCustomMachineTooltips(GTLangProvider provider) {
        // Steam Boilers
        provider.add("machine.gtceu.boiler.info.heating.up", "§cHeating up§r%s");
        provider.add("machine.gtceu.boiler.info.cooling.down", "§9Cooling down§r%s");
        provider.add("machine.gtceu.boiler.info.producing.steam", " §a(boiling water)");
        provider.add("machine.gtceu.boiler.heat_info", "Heat Capacity: %s %%");

        // Generators
        provider.add("recipe_type.gtceu.combustion_generator.tooltip", "§7Requires flammable Liquids");
        provider.add("recipe_type.gtceu.steam_turbine.tooltip", "§7Converts Steam into EU");
        provider.add("recipe_type.gtceu.gas_turbine.tooltip", "§7Requires flammable Gases");

        // Machine Hull
        provider.add("machine.gtceu.hull.tooltip",
                "§7You just need §5I§dm§4a§cg§ei§an§ba§3t§7i§1o§5n§7 to use this");

        // Battery Buffer
        provider.add("gtceu.battery_buffer.average_input", "Average input: %s EU/t");
        provider.add("gtceu.battery_buffer.average_output", "Average output: %s EU/t");

        // Transformer
        provider.add("machine.gtceu.transformer.description", "§7Transforms Energy between voltage tiers");
        provider.add("machine.gtceu.transformer.tooltip_tool_usage",
                "§7Starts as §fTransform Down§7, use Screwdriver to change");
        provider.add("machine.gtceu.transformer.tooltip_transform_down",
                "§aTransform Down: §f%dA %s EU (%s§f) -> %dA %s EU (%s§f)");
        provider.add("machine.gtceu.transformer.message_transform_down",
                "Transforming Down, In: %s EU %dA, Out: %s EU %dA");
        provider.add("machine.gtceu.transformer.tooltip_transform_up",
                "§cTransform Up: §f%dA %s EU (%s§f) -> %dA %s EU (%s§f)");
        provider.add("machine.gtceu.transformer.message_transform_up",
                "Transforming Up, In: %s EU %dA, Out: %s EU %dA");

        // Diode
        provider.add("machine.gtceu.diodemessage", "Max Amperage throughput: %s");
        provider.add("machine.gtceu.diodetooltip_tool_usage",
                "Hit with a Soft Mallet to change Amperage flow.");
        provider.add("machine.gtceu.diodetooltip_general",
                "Allows Energy Flow in one direction and limits Amperage");
        provider.add("machine.gtceu.diodetooltip_starts_at", "Starts as §f1A§7, use Soft Mallet to change");

        // Energy Converter
        provider.add("machine.gtceu.energy_converter.description", "Converts Energy between EU and FE");
        provider.add("machine.gtceu.energy_converter.tooltip_tool_usage",
                "Starts as §fFE Converter§7, use Soft Mallet to change");
        provider.add("machine.gtceu.energy_converter.tooltip_conversion_native",
                "§cNative Conversion: §f%d FE -> %dA %d EU (%s§f)");
        provider.add("machine.gtceu.energy_converter.message_conversion_native",
                "Converting Native Energy, In: %d FE, Out: %dA %d EU");
        provider.add("machine.gtceu.energy_converter.tooltip_conversion_eu",
                "§aEU Conversion: §f%dA %d EU (%s§f) -> %d Native");
        provider.add("machine.gtceu.energy_converter.message_conversion_eu",
                "Converting EU, In: %dA %d EU, Out: %d Native");

        // Pump
        provider.add("machine.gtceu.pump.tooltip", "§7The best way to empty Oceans!");
        provider.add("machine.gtceu.pump.tooltip_buckets", "§f%d §7ticks per Bucket");

        // Item Collector
        provider.add("machine.gtceu.item_collector.tooltip", "Collects Items around itself");
        provider.add("machine.gtceu.item_collector.gui.collect_range",
                "Collect within an area of %sx%s blocks");

        // Fisher
        provider.add("machine.gtceu.fisher.tooltip", "Costs string to fish. Consumes one string each time.");
        provider.add("machine.gtceu.fisher.speed", "Catches something every %d ticks");
        provider.add("machine.gtceu.fisher.requirement",
                "Requires a %dx%d centered square of water directly below.");

        // World Accelerator
        provider.add("machine.gtceu.world_accelerator.description",
                "Tick accelerates nearby blocks in one of 2 modes: §fTile Entity§7 or §fRandom Tick§7. Use Screwdriver to change mode.");
        provider.add("machine.gtceu.world_accelerator.working_area", "§bWorking Area:");
        provider.add("machine.gtceu.world_accelerator.working_area_tile",
                "  Block Entity Mode:§f Adjacent Blocks");
        provider.add("machine.gtceu.world_accelerator.working_area_random", "  Random Tick Mode:§f %dx%d");
        provider.add("machine.gtceu.world_accelerator.mode_tile", "Block Entity Mode");
        provider.add("machine.gtceu.world_accelerator.mode_entity", "Random Tick Mode");

        // Forming Press
        provider.add("gtceu.forming_press.naming.press", "§oNamed Press");
        provider.add("gtceu.forming_press.naming.to_name", "§oItem to Name");
        provider.add("gtceu.forming_press.naming.named", "§oNamed Item");

        // Scanner
        provider.add("gtceu.scanner.copy_stick_from", "§oStick to Copy");
        provider.add("gtceu.scanner.copy_stick_empty", "§oEmpty Stick");
        provider.add("gtceu.scanner.copy_stick_to", "§oCopy of Stick");

        // Miner
        provider.add("machine.gtceu.miner.tooltip", "§7Mines ores below the Miner! Starts as §f%sx%s §7area");
        provider.add("machine.gtceu.miner.per_block", "§7takes §f%ds §7per Block");

        // Buffer
        provider.add("machine.gtceu.buffer.tooltip", "A Small Buffer to store Items and Fluids");

        // Block Breaker
        provider.add("machine.gtceu.block_breaker.tooltip",
                "§7Mines block on front face and collects its drops");
        provider.add("machine.gtceu.block_breaker.speed_bonus", "§eSpeed Bonus: §f%d%%");

        // Creative Chest/Tank
        provider.add("machine.gtceu.quantum_chest.tooltip", "§7Better than Storage Drawers");
        provider.add("machine.gtceu.quantum_chest.items_stored", "Item Amount:");
        provider.add("machine.gtceu.quantum_tank.tooltip", "§7Compact place to store all your fluids");

        provider.add("gtceu.creative.chest.item", "Item");
        provider.add("gtceu.creative.chest.ipc", "Items per Cycle");
        provider.add("gtceu.creative.chest.tpc", "Ticks per Cycle");
        provider.add("gtceu.creative.tank.fluid", "Fluid");
        provider.add("gtceu.creative.tank.mbpc", "mB per Cycle");
        provider.add("gtceu.creative.tank.tpc", "Ticks per Cycle");
        provider.add("gtceu.creative.energy.amperage", "Amperage");
        provider.add("gtceu.creative.energy.voltage", "Voltage");
        provider.add("gtceu.creative.energy.sink", "Sink");
        provider.add("gtceu.creative.energy.source", "Source");
        provider.add("gtceu.creative.computation.average", "Average Requested CWUt");
        provider.add("gtceu.creative.activity.on", "Active");
        provider.add("gtceu.creative.activity.off", "Not active");

        // Workbench
        provider.addMultiline("gtceu.machine.workbench.tooltip",
                "Better than Forestry\nHas Item Storage, Tool Storage, pulls from adjacent Inventories, and saves Recipes.");
        provider.add("gtceu.machine.workbench.tab.workbench", "Crafting");
        provider.add("gtceu.machine.workbench.tab.item_list", "Storage");
        provider.addMultiline("gtceu.machine.workbench.storage_note",
                "(Available items from connected\ninventories usable for crafting)");
        provider.add("gtceu.item_list.item_stored", "§7Stored: %d");
        provider.add("gtceu.machine.workbench.tab.crafting", "Crafting");
        provider.add("gtceu.machine.workbench.tab.container", "Container");
        provider.addMultiline("gtceu.recipe_memory_widget.tooltip",
                "§7Left click to automatically input this recipe into the crafting grid\n§7Shift click to lock/unlock this recipe");

        // Safe
        provider.add("gtceu.machine.locked_safe.malfunctioning", "§cMalfunctioning!");
        provider.add("gtceu.machine.locked_safe.requirements", "§7Replacements required:");

        // Drums
        provider.add("machine.gtceu.drum.enable_output", "Will drain Fluid to downward adjacent Tanks");
        provider.add("machine.gtceu.drum.disable_output", "Will not drain Fluid");

        // Long Distance Pipeline
        provider.addMultiLang("gtceu.machine.endpoint.tooltip",
                "Connect with §fLong Distance Pipe§7 blocks to create a pipeline.",
                "Pipelines must have exactly §f1 Input§7 and §f1 Output§7 endpoint.",
                "Only pipeline endpoints need to be §fchunk-loaded§7.");
        provider.add("block.gtceu.long_distance_item_pipeline_no_network", "No network found");
        provider.add("block.gtceu.long_distance_item_pipeline_input_endpoint", "Input Endpoint");
        provider.add("block.gtceu.long_distance_item_pipeline_output_endpoint", "Output Endpoint");
        provider.add("block.gtceu.long_distance_item_pipeline_network_header", "Network:");
        provider.add("block.gtceu.long_distance_item_pipeline_pipe_count", " - Pipes: %s");
        provider.add("block.gtceu.long_distance_item_pipeline_input_pos", " - Input: %s");
        provider.add("block.gtceu.long_distance_item_pipeline_output_pos", " - Output: %s");
        provider.add("gtceu.machine.endpoint.tooltip.min_length", "§bMinimum Endpoint Distance: §f%d Blocks");
    }

    private static void generateCustomPerTierMachineTooltips(GTLangProvider provider) {
        // Macerator
        provider.add("machine.gtceu.lv_macerator.tooltip", "§7Shredding your Ores without Byproducts");
        provider.add("machine.gtceu.mv_macerator.tooltip", "§7Shredding your Ores without Byproducts");
        provider.add("machine.gtceu.hv_macerator.tooltip", "§7Shredding your Ores with Byproducts");
        provider.add("machine.gtceu.ev_macerator.tooltip", "§7Shredding your Ores with Byproducts");
        provider.add("machine.gtceu.iv_macerator.tooltip", "§7Blend-O-Matic 9001");
        provider.add("machine.gtceu.luv_macerator.tooltip", "§7Blend-O-Matic 9002");
        provider.add("machine.gtceu.zpm_macerator.tooltip", "§7Blend-O-Matic 9003");
        provider.add("machine.gtceu.uv_macerator.tooltip", "§7Shape Eliminator");
        provider.add("machine.gtceu.uhv_macerator.tooltip", "§7Shape Eliminator");
        provider.add("machine.gtceu.uev_macerator.tooltip", "§7Shape Eliminator");
        provider.add("machine.gtceu.uiv_macerator.tooltip", "§7Shape Eliminator");
        provider.add("machine.gtceu.uxv_macerator.tooltip", "§7Shape Eliminator");
        provider.add("machine.gtceu.opv_macerator.tooltip", "§7Shape Eliminator");

        // Centrifuge
        provider.add("machine.gtceu.lv_centrifuge.tooltip", "§7Separating Molecules");
        provider.add("machine.gtceu.mv_centrifuge.tooltip", "§7Separating Molecules");
        provider.add("machine.gtceu.hv_centrifuge.tooltip", "§7Separating Molecules");
        provider.add("machine.gtceu.ev_centrifuge.tooltip", "§7Molecular Separator");
        provider.add("machine.gtceu.iv_centrifuge.tooltip", "§7Molecular Cyclone");
        provider.add("machine.gtceu.luv_centrifuge.tooltip", "§7Molecular Cyclone");
        provider.add("machine.gtceu.zpm_centrifuge.tooltip", "§7Molecular Cyclone");
        provider.add("machine.gtceu.uv_centrifuge.tooltip", "§7Molecular Tornado");
        provider.add("machine.gtceu.uhv_centrifuge.tooltip", "§7Molecular Tornado");
        provider.add("machine.gtceu.uev_centrifuge.tooltip", "§7Molecular Tornado");
        provider.add("machine.gtceu.uiv_centrifuge.tooltip", "§7Molecular Tornado");
        provider.add("machine.gtceu.uxv_centrifuge.tooltip", "§7Molecular Tornado");
        provider.add("machine.gtceu.opv_centrifuge.tooltip", "§7Molecular Tornado");

        // Laser Engraver
        provider.add("machine.gtceu.lv_laser_engraver.tooltip", "§7Don't look directly at the Laser");
        provider.add("machine.gtceu.mv_laser_engraver.tooltip", "§7Don't look directly at the Laser");
        provider.add("machine.gtceu.hv_laser_engraver.tooltip", "§7Don't look directly at the Laser");
        provider.add("machine.gtceu.ev_laser_engraver.tooltip", "§7Don't look directly at the Laser");
        provider.add("machine.gtceu.iv_laser_engraver.tooltip", "§7With the Power of 2.04 MW");
        provider.add("machine.gtceu.luv_laser_engraver.tooltip", "§7With the Power of 8.16 MW");
        provider.add("machine.gtceu.zpm_laser_engraver.tooltip", "§7With the Power of 32.64 MW");
        provider.add("machine.gtceu.uv_laser_engraver.tooltip", "§7Exact Photon Cannon");
        provider.add("machine.gtceu.uhv_laser_engraver.tooltip", "§7Exact Photon Cannon");
        provider.add("machine.gtceu.uev_laser_engraver.tooltip", "§7Exact Photon Cannon");
        provider.add("machine.gtceu.uiv_laser_engraver.tooltip", "§7Exact Photon Cannon");
        provider.add("machine.gtceu.uxv_laser_engraver.tooltip", "§7Exact Photon Cannon");
        provider.add("machine.gtceu.opv_laser_engraver.tooltip", "§7Exact Photon Cannon");

        // Thermal Centrifuge
        provider.add("machine.gtceu.lv_thermal_centrifuge.tooltip", "§7Separating Ores more precisely");
        provider.add("machine.gtceu.mv_thermal_centrifuge.tooltip", "§7Separating Ores more precisely");
        provider.add("machine.gtceu.hv_thermal_centrifuge.tooltip", "§7Separating Ores more precisely");
        provider.add("machine.gtceu.ev_thermal_centrifuge.tooltip", "§7Separating Ores more precisely");
        provider.add("machine.gtceu.iv_thermal_centrifuge.tooltip", "§7Blaze Sweatshop T-6350");
        provider.add("machine.gtceu.luv_thermal_centrifuge.tooltip", "§7Blaze Sweatshop T-6351");
        provider.add("machine.gtceu.zpm_thermal_centrifuge.tooltip", "§7Blaze Sweatshop T-6352");
        provider.add("machine.gtceu.uv_thermal_centrifuge.tooltip", "§7Fire Cyclone");
        provider.add("machine.gtceu.uhv_thermal_centrifuge.tooltip", "§7Fire Cyclone");
        provider.add("machine.gtceu.uev_thermal_centrifuge.tooltip", "§7Fire Cyclone");
        provider.add("machine.gtceu.uiv_thermal_centrifuge.tooltip", "§7Fire Cyclone");
        provider.add("machine.gtceu.uxv_thermal_centrifuge.tooltip", "§7Fire Cyclone");
        provider.add("machine.gtceu.opv_thermal_centrifuge.tooltip", "§7Fire Cyclone");

        // Electrolyzer
        provider.add("machine.gtceu.lv_electrolyzer.tooltip", "§7Electrolyzing Molecules");
        provider.add("machine.gtceu.mv_electrolyzer.tooltip", "§7Electrolyzing Molecules");
        provider.add("machine.gtceu.hv_electrolyzer.tooltip", "§7Electrolyzing Molecules");
        provider.add("machine.gtceu.ev_electrolyzer.tooltip", "§7Electrolyzing Molecules");
        provider.add("machine.gtceu.iv_electrolyzer.tooltip", "§7Molecular Disintegrator E-4906");
        provider.add("machine.gtceu.luv_electrolyzer.tooltip", "§7Molecular Disintegrator E-4907");
        provider.add("machine.gtceu.zpm_electrolyzer.tooltip", "§7Molecular Disintegrator E-4908");
        provider.add("machine.gtceu.uv_electrolyzer.tooltip", "§7Atomic Ionizer");
        provider.add("machine.gtceu.uhv_electrolyzer.tooltip", "§7Atomic Ionizer");
        provider.add("machine.gtceu.uev_electrolyzer.tooltip", "§7Atomic Ionizer");
        provider.add("machine.gtceu.uiv_electrolyzer.tooltip", "§7Atomic Ionizer");
        provider.add("machine.gtceu.uxv_electrolyzer.tooltip", "§7Atomic Ionizer");
        provider.add("machine.gtceu.opv_electrolyzer.tooltip", "§7Atomic Ionizer");

        // Lathe
        provider.add("machine.gtceu.lv_lathe.tooltip", "§7Produces Rods more efficiently");
        provider.add("machine.gtceu.mv_lathe.tooltip", "§7Produces Rods more efficiently");
        provider.add("machine.gtceu.hv_lathe.tooltip", "§7Produces Rods more efficiently");
        provider.add("machine.gtceu.ev_lathe.tooltip", "§7Produces Rods more efficiently");
        provider.add("machine.gtceu.iv_lathe.tooltip", "§7Turn-O-Matic L-5906");
        provider.add("machine.gtceu.luv_lathe.tooltip", "§7Turn-O-Matic L-5907");
        provider.add("machine.gtceu.zpm_lathe.tooltip", "§7Turn-O-Matic L-5908");
        provider.add("machine.gtceu.uv_lathe.tooltip", "§7Rotation Grinder");
        provider.add("machine.gtceu.uhv_lathe.tooltip", "§7Rotation Grinder");
        provider.add("machine.gtceu.uev_lathe.tooltip", "§7Rotation Grinder");
        provider.add("machine.gtceu.uiv_lathe.tooltip", "§7Rotation Grinder");
        provider.add("machine.gtceu.uxv_lathe.tooltip", "§7Rotation Grinder");
        provider.add("machine.gtceu.opv_lathe.tooltip", "§7Rotation Grinder");

        // Ore Washer
        provider.add("machine.gtceu.lv_ore_washer.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("machine.gtceu.mv_ore_washer.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("machine.gtceu.hv_ore_washer.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("machine.gtceu.ev_ore_washer.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("machine.gtceu.iv_ore_washer.tooltip", "§7Repurposed Laundry-Washer I-360");
        provider.add("machine.gtceu.luv_ore_washer.tooltip", "§7Repurposed Laundry-Washer I-361");
        provider.add("machine.gtceu.zpm_ore_washer.tooltip", "§7Repurposed Laundry-Washer I-362");
        provider.add("machine.gtceu.uv_ore_washer.tooltip", "§7Miniature Car Wash");
        provider.add("machine.gtceu.uhv_ore_washer.tooltip", "§7Miniature Car Wash");
        provider.add("machine.gtceu.uev_ore_washer.tooltip", "§7Miniature Car Wash");
        provider.add("machine.gtceu.uiv_ore_washer.tooltip", "§7Miniature Car Wash");
        provider.add("machine.gtceu.uxv_ore_washer.tooltip", "§7Miniature Car Wash");
        provider.add("machine.gtceu.opv_ore_washer.tooltip", "§7Miniature Car Wash");

        // Electric Furnace
        standardTooltips(provider, "gtceu",
                "electric_furnace",
                "Not like using a Commodore 64",
                "Electron Excitement Processor",
                "Atom Stimulator");

        // Alloy Smelter
        standardTooltips(provider, "gtceu",
                "alloy_smelter",
                "HighTech combination Smelter",
                "Alloy Integrator",
                "Metal Amalgamator");

        // Arc Furnace
        standardTooltips(provider, "gtceu",
                "arc_furnace",
                "Who needs a Blast Furnace?",
                "Discharge Heater",
                "Short Circuit Heater");

        // Assembler
        standardTooltips(provider, "gtceu",
                "assembler",
                "Avengers, Assemble!",
                "NOT a Crafting Table",
                "Assembly Constructor");

        // Autoclave
        standardTooltips(provider, "gtceu",
                "autoclave",
                "Crystallizing your Dusts",
                "Pressure Cooker",
                "Encumbrance Unit");

        // Bender
        standardTooltips(provider, "gtceu",
                "bender",
                "Boo, he's bad! We want BENDER!!!",
                "Shape Distorter",
                "Matter Deformer");

        // Brewery
        standardTooltips(provider, "gtceu",
                "brewery",
                "Compact and efficient potion brewing",
                "Brewing your Drinks",
                "Brew Rusher");

        // Canner
        standardTooltips(provider, "gtceu",
                "canner",
                "Puts things into and out of Containers",
                "Can Operator",
                "Can Actuator");

        // Chemical Bath
        standardTooltips(provider, "gtceu",
                "chemical_bath",
                "Bathing Ores in Chemicals to separate them",
                "Chemical Soaker",
                "Chemical Dunktron");

        // Chemical Reactor
        standardTooltips(provider, "gtceu",
                "chemical_reactor",
                "Letting Chemicals react with each other",
                "Chemical Performer",
                "Reaction Catalyzer");

        // Compressor
        standardTooltips(provider, "gtceu",
                "compressor",
                "Compress-O-Matic C77",
                "Singularity Condenser",
                "Matter Constrictor");

        // Cutter
        standardTooltips(provider, "gtceu",
                "cutter",
                "Slice'N Dice",
                "Matter Cleaver",
                "Object Divider");

        // Distillery
        standardTooltips(provider, "gtceu",
                "distillery",
                "Extracting most relevant Parts of Fluids",
                "Condensation Separator",
                "Fraction Splitter");

        // Electromagnetic Separator
        standardTooltips(provider, "gtceu",
                "electromagnetic_separator",
                "Separating the magnetic Ores from the rest",
                "EM Categorizer",
                "EMF Dispeller");

        // Extractor
        standardTooltips(provider, "gtceu",
                "extractor",
                "Dejuicer-Device of Doom - D123",
                "Vacuum Extractinator",
                "Liquefying Sucker");

        // Extruder
        standardTooltips(provider, "gtceu",
                "extruder",
                "Universal Machine for Metal Working",
                "Material Displacer",
                "Shape Driver");

        // Fermenter
        standardTooltips(provider, "gtceu",
                "fermenter",
                "Fermenting Fluids",
                "Fermentation Hastener",
                "Respiration Controller");

        // Fluid Heater
        standardTooltips(provider, "gtceu",
                "fluid_heater",
                "Heating up your Fluids",
                "Heat Infuser",
                "Thermal Imbuer");

        // Fluid Solidifier
        standardTooltips(provider, "gtceu",
                "fluid_solidifier",
                "Cools Fluids down to form Solids",
                "Not an Ice Machine",
                "Fluid Petrificator");

        // Forge Hammer
        standardTooltips(provider, "gtceu",
                "forge_hammer",
                "Stop, Hammertime!",
                "Plate Forger",
                "Impact Modulator");

        // Forming Press
        standardTooltips(provider, "gtceu",
                "forming_press",
                "Imprinting Images into things",
                "Object Layerer",
                "Surface Shifter");

        // Mixer
        standardTooltips(provider, "gtceu",
                "mixer",
                "Will it Blend?",
                "Matter Organizer",
                "Material Homogenizer");

        // Packer
        standardTooltips(provider, "gtceu",
                "packer",
                "Puts things into and Grabs things out of Boxes",
                "Boxinator",
                "Amazon Warehouse");

        // Polarizer
        standardTooltips(provider, "gtceu",
                "polarizer",
                "Bipolarising your Magnets",
                "Magnetism Inducer",
                "Magnetic Field Rearranger");

        // Sifter
        standardTooltips(provider, "gtceu",
                "sifter",
                "Stay calm and keep sifting",
                "Sponsored by TFC",
                "Pulsation Filter");

        // Wiremill
        standardTooltips(provider, "gtceu",
                "wiremill",
                "Produces Wires more efficiently",
                "Ingot Elongator",
                "Wire Transfigurator");

        // Circuit Assembler
        standardTooltips(provider, "gtceu",
                "circuit_assembler",
                "Pick-n-Place all over the place",
                "Electronics Manufacturer",
                "Computation Factory");

        // Mass Fabricator
        standardTooltips(provider, "gtceu",
                "mass_fabricator",
                "UUM Matter * Fabrication Squared",
                "Genesis Factory",
                "Existence Initiator");

        // Replicator
        standardTooltips(provider, "gtceu",
                "replicator",
                "Producing the Purest of Elements",
                "Matter Paster",
                "Elemental Composer");

        // Scanner
        standardTooltips(provider, "gtceu",
                "scanner",
                "Scans Materials and other things",
                "Anomaly Detector",
                "Electron Microscope");

        // Gas Collector
        provider.add("machine.gtceu.lv_gas_collector.tooltip",
                "§7Collects Gas from the air depending on the dimension");
        provider.add("machine.gtceu.mv_gas_collector.tooltip",
                "§7Collects Gas from the air depending on the dimension");
        provider.add("machine.gtceu.hv_gas_collector.tooltip",
                "§7Collects Gas from the air depending on the dimension");
        provider.add("machine.gtceu.ev_gas_collector.tooltip",
                "§7Collects Gas from the air depending on the dimension");
        provider.add("machine.gtceu.iv_gas_collector.tooltip",
                "§7Collects Gas from the atmosphere depending on the dimension");
        provider.add("machine.gtceu.luv_gas_collector.tooltip",
                "§7Collects Gas from the atmosphere depending on the dimension");
        provider.add("machine.gtceu.zpm_gas_collector.tooltip",
                "§7Collects Gas from the atmosphere depending on the dimension");
        provider.add("machine.gtceu.uv_gas_collector.tooltip",
                "§7Collects Gas from the solar system depending on the dimension");
        provider.add("machine.gtceu.uhv_gas_collector.tooltip",
                "§7Collects Gas from the solar system depending on the dimension");
        provider.add("machine.gtceu.uev_gas_collector.tooltip",
                "§7Collects Gas from the solar system depending on the dimension");
        provider.add("machine.gtceu.uiv_gas_collector.tooltip",
                "§7Collects Gas from the solar system depending on the dimension");
        provider.add("machine.gtceu.uxv_gas_collector.tooltip",
                "§7Collects Gas from the solar system depending on the dimension");
        provider.add("machine.gtceu.opv_gas_collector.tooltip",
                "§7Collects Gas from the universe depending on the dimension");

        // Rock Breaker
        provider.add("machine.gtceu.lv_rock_crusher.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("machine.gtceu.mv_rock_crusher.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("machine.gtceu.hv_rock_crusher.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("machine.gtceu.ev_rock_crusher.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("machine.gtceu.iv_rock_crusher.tooltip", "§7Cryogenic Magma Solidifier R-8200");
        provider.add("machine.gtceu.luv_rock_crusher.tooltip", "§7Cryogenic Magma Solidifier R-9200");
        provider.add("machine.gtceu.zpm_rock_crusher.tooltip", "§7Cryogenic Magma Solidifier R-10200");
        provider.add("machine.gtceu.uv_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
        provider.add("machine.gtceu.uhv_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
        provider.add("machine.gtceu.uev_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
        provider.add("machine.gtceu.uiv_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
        provider.add("machine.gtceu.uxv_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
        provider.add("machine.gtceu.opv_rock_crusher.tooltip", "§7Volcanic Formation Chamber");
    }

    private static void generateGeneralMultiblockTooltips(GTLangProvider provider) {
        provider.add("gtceu.multiblock.dimension", "§eDimensions: §r%sx%sx%s");

        provider.add("gtceu.machine.parallel_limit", "Can run up to §b%d§r§7 Recipes at once.");
    }

    private static void generateCustomMultiblockTooltips(GTLangProvider provider) {
        // Primitive Water Pump
        provider.add("machine.gtceu.primitive_pump.tooltip", "Endervoir at Home");
        provider.add("gtceu.multiblock.primitive_water_pump.description",
                "The Primitive Water Pump is a pre-Steam Era multiblock that collects water once per second, depending on the Biome it is in. It can use a Pump, ULV, or LV Output Hatch, increasing the amount of water per tier. Follows the formula: Biome Coefficient * Hatch Multiplier.");
        provider.addMultiline("gtceu.multiblock.primitive_water_pump.extra1",
                "Biome Coefficient:\n  Ocean, River: 1000 mB/s\n  Swamp: 800 mB/s\n  Jungle: 350 mB/s\n  Snowy: 300 mB/s\n  Plains, Forest: 250 mB/s\n  Taiga: 175 mB/s\n  Beach: 170 mB/s\n  Other: 100 mB/s");
        provider.addMultiline("gtceu.multiblock.primitive_water_pump.extra2",
                "Hatch Multipliers:\n  Pump Hatch: 1x\n  ULV Output Hatch: 2x\n  LV Output Hatch: 4x\n\nWhile raining in the Pump's Biome, the total water production will be increased by 50%%.");

        // Primitive Blast Furnace
        provider.add("machine.gtceu.primitive_blast_furnace.tooltip", "Making your first Steel");
        provider.add("gtceu.multiblock.primitive_blast_furnace.bronze.description",
                "The Primitive Blast Furnace (PBF) is a multiblock structure used for cooking steel in the early game. Although not very fast, it will provide you with steel for your first setups.");

        // Electric Blast Furnace
        provider.add("machine.gtceu.electric_blast_furnace.tooltip", "Where's the electric smoker?");
        provider.add("gtceu.multiblock.blast_furnace.max_temperature", "Heat Capacity: %s");
        provider.addMultiLang("machine.gtceu.electric_blast_furnace.tooltip",
                "For every §f900K§7 above the recipe temperature, a multiplicative §f95%%§7 energy multiplier is applied pre-overclocking.",
                "For every §f1800K§7 above the recipe temperature, one overclock becomes §f100%% efficient§7 (perfect overclock).",
                "For every voltage tier above §bMV§7, temperature is increased by §f100K§7.");
        provider.add("gtceu.multiblock.electric_blast_furnace.description",
                "The Electric Blast Furnace (EBF) is a multiblock structure used for smelting alloys, cooking metals and refining ores. It is required for obtaining high-tier alloys and metals, such as aluminium, stainless steel, titanium, and naquadah alloy.");

        // Coke Oven
        provider.add("machine.gtceu.coke_oven.tooltip", "Making better fuels for Steel and Power");
        provider.add("gtceu.multiblock.coke_oven.description",
                "The Coke Oven is a multiblock structure used for getting coke and creosote in the early game. It doesn't require fuel and has an internal tank of 32 buckets for creosote. Its inventory can be accessed via its Coke Oven Hatch.");

        // Steam Grinder
        provider.add("machine.gtceu.steam_grinder.tooltip", "A multiblock Macerator without the Byproducts");
        provider.add("gtceu.multiblock.steam_grinder.description",
                "A Multiblock Macerator at the Steam Age. Requires at least 14 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch.");

        // Steam Oven
        provider.add("machine.gtceu.steam_oven.tooltip", "Not to be confused with Multi-Smelter");
        provider.add("gtceu.multiblock.steam_oven.description",
                "A Multi Smelter at the Steam Age. Requires at least 6 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch. Steam Hatch must be on the bottom layer, no more than one.");

        // General Steam Multiblocks
        provider.add("gtceu.multiblock.steam.low_steam", "Not enough Steam to run!");
        provider.add("gtceu.multiblock.steam.steam_stored", "Steam: %s / %s mb");
        provider.add("gtceu.multiblock.steam.duration_modifier",
                "Takes §f1.5x §7base duration to process, not affected by number of items.");
        provider.add("machine.gtceu.steam.steam_hatch.tooltip", "§eAccepted Fluid: §fSteam");
        provider.add("machine.gtceu.steam_bus.tooltip", "Does not work with non-steam multiblocks");
        provider.add("gtceu.multiblock.require_steam_parts", "Requires Steam Hatches and Buses!");

        // Large Boiler
        provider.add("machine.gtceu.bronze_large_boiler.tooltip", "We need more Steam!");
        provider.add("machine.gtceu.steel_large_boiler.tooltip", "Charcoal Incinerator");
        provider.add("machine.gtceu.titanium_large_boiler.tooltip", "Where's the Magic Super Fuel?");
        provider.add("machine.gtceu.tungstensteel_large_boiler.tooltip", "How do you even fuel this thing?");
        provider.add("gtceu.multiblock.large_boiler.description",
                "Large Boilers are multiblocks that generate steam from an energy source and water. Said energy source is either any Solid Fuel with a Burn Time, or a Diesel/Semi-Fluid Fuel. Can be throttled back in increments of 5%% to reduce Steam output and Fuel consumption.");
        provider.add("gtceu.multiblock.large_boiler.max_temperature",
                "Max Temperature: %dK, Steam Production: %dmB/t");
        provider.add("gtceu.multiblock.large_boiler.efficiency", "Efficiency: %s");
        provider.add("gtceu.multiblock.large_boiler.temperature", "Temperature: %sK / %sK");
        provider.add("gtceu.multiblock.large_boiler.steam_output", "Steam Output: %s mB/t");
        provider.add("gtceu.multiblock.large_boiler.throttle", "Throttle: %d");
        provider.add("gtceu.multiblock.large_boiler.throttle.tooltip",
                "Boiler can output less Steam and consume less fuel (efficiency is not lost, does not affect heat-up time)");
        provider.add("gtceu.multiblock.large_boiler.throttle_modify", "Modify Throttle:");
        provider.add("gtceu.multiblock.large_boiler.rate_tooltip",
                "§7Produces §f%d L §7of Steam with §f1 Coal");
        provider.add("gtceu.multiblock.large_boiler.heat_time_tooltip", "§7Takes §f%d seconds §7to boiling up");
        provider.add("gtceu.multiblock.large_boiler.explosion_tooltip",
                "Will explode if provided Fuel with no Water");

        // Multi Smelter
        provider.add("machine.gtceu.multi_smelter.tooltip", "Just like the Oven at Home");
        provider.add("gtceu.multiblock.multi_furnace.description",
                "The Multi Smelter is a multiblock structure used for smelting massive amounts of items at once. Different tiers of coils provide a speed boost and energy efficiency gain. 32 is the base value of items smelted per operation, and can be multiplied by using higher level coils.");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_level", "Heating Coil Level: %s");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_discount", "Heating Coil EU Boost: %sx");

        // Large Chemical Reactor
        provider.add("gtceu.multiblock.large_chemical_reactor.description",
                "The Large Chemical Reactor performs chemical reactions at 100%% energy efficiency. Overclocks multiply both speed and energy by 4. The multiblock requires exactly 1 Cupronickel Coil Block, which must be placed adjacent to the PTFE Pipe casing located in the center.");
        provider.add("machine.gtceu.large_chemical_reactor.tooltip", "Black Box Reactor");

        // Vacuum Freezer
        provider.add("gtceu.machine.vacuum_freezer.tooltip", "Aluminium Ice Box");
        provider.add("gtceu.multiblock.vacuum_freezer.description",
                "The Vacuum Freezer is a multiblock structure mainly used for freezing Hot Ingots into regular Ingots. However, it can also freeze other substances, such as Water.");

        // Pyrolyse Oven
        provider.add("gtceu.multiblock.pyrolyse_oven.description",
                "The Pyrolyse Oven is a multiblock structure used for turning Logs into Charcoal and Creosote Oil, or Ash and Heavy Oil.");
        provider.add("gtceu.machine.pyrolyse_oven.tooltip", "Electric Coke Oven");
        provider.add("gtceu.machine.pyrolyse_oven.tooltip.1",
                "§6Cupronickel §7coils are §f25%%§7 slower. Every coil after §bKanthal§7 increases speed by §f50%%§7.");
        provider.add("gtceu.multiblock.pyrolyse_oven.speed", "Processing Speed: %s%%");

        // Implosion Compressor
        provider.add("gtceu.machine.implosion_compressor.tooltip", "The only Machine you want to go Boom");
        provider.add("gtceu.multiblock.implosion_compressor.description",
                "The Implosion Compressor is a multiblock structure that uses explosives to turn gem dusts into their corresponding gems.");

        // Cracker
        provider.add("gtceu.machine.cracker.tooltip", "Makes Oil useful");
        provider.add("gtceu.machine.cracker.tooltip.1",
                "Every coil after §6Cupronickel§7 reduces energy usage by §f10%%§7.");
        provider.add("gtceu.multiblock.cracker.description",
                "The Oil Cracking Unit is a multiblock structure used for turning Light and Heavy Fuel into their Cracked variants.");
        provider.add("gtceu.multiblock.cracking_unit.energy", "Energy Usage: %s%%");

        // Distillation Tower
        provider.add("gtceu.machine.distillation_tower.tooltip", "Fluid Refinery");
        provider.add("gtceu.multiblock.distillation_tower.description",
                "The Distillation Tower is a multiblock structure used for distilling the various types of Oil and some of their byproducts. Each layer must have exactly one output hatch, starting from the second one. The bottom layer can output items and insert fluids in any position.");
        provider.add("gtceu.multiblock.distillation_tower.distilling_fluid", "Distilling %s");

        // Assembly Line
        provider.add("gtceu.multiblock.assembly_line.description",
                "The Assembly Line is a large multiblock structure consisting of 5 to 16 \"slices\". In theory, it's large Assembling Machine, used for creating advanced crafting components.");
        provider.add("gtceu.machine.assembly_line.tooltip", "Not a multiblock Assembling Machine!");

        // Combustion Engine
        provider.add("gtceu.machine.large_combustion_engine.tooltip", "Fuel Ignition Chamber");
        provider.add("gtceu.machine.extreme_combustion_engine.tooltip", "Extreme Chemical Energy Releaser");
        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_regular",
                "Supply §f20 mB/s§7 of Oxygen to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");
        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_extreme",
                "Supply §f80 mB/s§7 of Liquid Oxygen to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");
        provider.add("gtceu.multiblock.large_combustion_engine.description",
                "The Large Combustion Engine is a multiblock structure that acts as a Combustion Generator for EV power.");
        provider.add("gtceu.multiblock.extreme_combustion_engine.description",
                "The Extreme Combustion Engine is a multiblock structure that acts as a Combustion Generator for IV power.");
        provider.add("gtceu.multiblock.large_combustion_engine.lubricant_amount", "Lubricant Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_amount", "Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_amount",
                "Liquid Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_boosted", "§bOxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted",
                "§bLiquid Oxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.boost_disallowed",
                "§bUpgrade the Dynamo Hatch to enable Oxygen Boosting.");
        provider.add("gtceu.multiblock.large_combustion_engine.supply_oxygen_to_boost",
                "Supply Oxygen to boost.");
        provider.add("gtceu.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost",
                "Supply Liquid Oxygen to boost.");
        provider.add("gtceu.multiblock.large_combustion_engine.obstructed", "Engine Intakes Obstructed.");

        // Turbines
        provider.add("block.gtceu.steam_large_turbine", "Large Steam Turbine");
        provider.add("block.gtceu.gas_large_turbine", "Large Gas Turbine");
        provider.add("block.gtceu.plasma_large_turbine", "Large Plasma Turbine");
        provider.add("gtceu.machine.large_turbine.steam.tooltip", "Do not put your Head in it");
        provider.add("gtceu.machine.large_turbine.gas.tooltip", "Not a Jet Engine");
        provider.add("gtceu.machine.large_turbine.plasma.tooltip", "Plasma Energy Siphon");
        provider.add("gtceu.multiblock.turbine.fuel_amount", "Fuel Amount: %sL (%s)");
        provider.add("gtceu.multiblock.turbine.rotor_speed", "Rotor Speed: %s/%s RPM");
        provider.add("gtceu.multiblock.turbine.rotor_durability", "Rotor Durability: %s%%");
        provider.add("gtceu.multiblock.turbine.efficiency", "Turbine Efficiency: %s%%");
        provider.add("gtceu.multiblock.turbine.energy_per_tick", "Energy Output: %s/%s EU/t");
        provider.add("gtceu.multiblock.turbine.energy_per_tick_maxed", "Energy Output: %s EU/t");
        provider.add("gtceu.multiblock.turbine.obstructed", "Turbine Face Obstructed");
        provider.add("gtceu.multiblock.turbine.efficiency_tooltip",
                "Each Rotor Holder above %s§7 adds §f10%% efficiency and multiplies EU/t by 2§7.");
        provider.add("gtceu.multiblock.turbine.fuel_needed", "Consumes %s per %s ticks");
        provider.add("gtceu.multiblock.large_turbine.description",
                "Large Turbines are multiblocks that generate power from steam, gases, and plasma by having them spin the turbine's rotor. Energy output is based on rotor efficiency and current speed of turbine. Gearbox casings are used in the center of the structure.");

        // Fusion Reactor
        provider.add("gtceu.machine.fusion_reactor.capacity", "§7Maximum Energy Storage: §e%sM EU");
        provider.add("gtceu.machine.fusion_reactor.overclocking",
                "Overclocks double energy and halve duration.");
        provider.add("gtceu.multiblock.luv_fusion_reactor.description",
                "The Fusion Reactor MK 1 is a large multiblock structure used for fusing elements into heavier ones. It can only use LuV, ZPM, and UV Energy Hatches. For every Hatch it has, its buffer increases by 10M EU, and has a maximum of 160M.");
        provider.add("gtceu.multiblock.zpm_fusion_reactor.description",
                "The Fusion Reactor MK 2 is a large multiblock structure used for fusing elements into heavier ones. It can only use ZPM and UV Energy Hatches. For every Hatch it has, its buffer increases by 20M EU, and has a maximum of 320M.");
        provider.add("gtceu.multiblock.uv_fusion_reactor.description",
                "The Fusion Reactor MK 3 is a large multiblock structure used for fusing elements into heavier ones. It can only use UV Energy Hatches. For every Hatch it has, its buffer increases by 40M EU, and has a maximum of 640M.");
        provider.add("gtceu.multiblock.fusion_reactor.energy", "EU: %d / %d");
        provider.add("gtceu.multiblock.fusion_reactor.heat", "Heat: %d");
        provider.add("gtceu.machine.fusion_reactor.luv.tooltip", "Atomic Alloy Smelter");
        provider.add("gtceu.machine.fusion_reactor.zpm.tooltip", "A SUN DOWN ON EARTH");
        provider.add("gtceu.machine.fusion_reactor.uv.tooltip", "A WHITE DWARF DOWN ON YOUR BASE");

        // Large Miner
        provider.add("gtceu.machine.large_miner.ev.tooltip", "Digging Ore instead of You");
        provider.add("gtceu.machine.large_miner.iv.tooltip", "Biome Excavator");
        provider.add("gtceu.machine.large_miner.luv.tooltip", "Terrestrial Harvester");
        provider.add("gtceu.machine.miner.multi.modes", "Has Silk Touch and Chunk Aligned Modes.");
        provider.add("gtceu.machine.miner.multi.production",
                "Produces §f3x§7 more crushed ore than a §fMacerator§7.");
        provider.add("gtceu.machine.miner.fluid_usage", "Uses §f%d mB/t §7of §f%s§7, doubled per overclock.");
        provider.add("gtceu.machine.miner.multi.description",
                "A multiblock mining machine that covers a large area and produces huge quantity of ore.");
        provider.add("gtceu.machine.miner.startx", "sX: %d");
        provider.add("gtceu.machine.miner.starty", "sY: %d");
        provider.add("gtceu.machine.miner.startz", "sZ: %d");
        provider.add("gtceu.machine.miner.minex", "mX: %d");
        provider.add("gtceu.machine.miner.miney", "mY: %d");
        provider.add("gtceu.machine.miner.minez", "mZ: %d");
        provider.add("gtceu.machine.miner.radius", "Radius: %d");
        provider.add("gtceu.machine.miner.chunkradius", "Chunk Radius: %d");
        provider.add("gtceu.multiblock.large_miner.done", "Done!");
        provider.add("gtceu.multiblock.large_miner.working", "Working...");
        provider.add("gtceu.multiblock.large_miner.invfull", "Inventory Full!");
        provider.add("gtceu.multiblock.large_miner.needspower", "Needs Power!");
        provider.add("gtceu.multiblock.large_miner.vent", "Venting Blocked!");
        provider.add("gtceu.multiblock.large_miner.steam", "Needs Steam!");
        provider.add("gtceu.multiblock.large_miner.radius", "Radius: §a%d§r Blocks");
        provider.add("gtceu.multiblock.large_miner.errorradius", "§cCannot change radius while working!");
        provider.add("gtceu.multiblock.large_miner.needsfluid", "Needs Drilling Fluid");

        // Fluid Drilling Rig
        provider.add("gtceu.machine.fluid_drilling_rig.description",
                "§7Drills fluids from veins under bedrock.");
        provider.add("gtceu.machine.fluid_drilling_rig.production",
                "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.fluid_drilling_rig.depletion", "§bDepletion Rate: §f%s%%");
        provider.add("gtceu.machine.mv_fluid_drilling_rig.tooltip", "Oil Extraction Pump");
        provider.add("gtceu.machine.hv_fluid_drilling_rig.tooltip", "Does not perform Fracking");
        provider.add("gtceu.machine.ev_fluid_drilling_rig.tooltip", "Well Drainer");
        provider.add("gtceu.multiblock.fluid_rig.drilled_fluid", "Fluid: %s");
        provider.add("gtceu.multiblock.fluid_rig.no_fluid_in_area", "None in Area.");
        provider.add("gtceu.multiblock.fluid_rig.fluid_amount", "Pumping Rate: %s");
        provider.add("gtceu.multiblock.fluid_rig.vein_depletion", "Vein Size: %s");
        provider.add("gtceu.multiblock.fluid_rig.vein_depleted", "Vein Depleted.");

        // Bedrock Miner
        provider.add("gtceu.machine.bedrock_ore_miner.description", "§7Drills ores from veins under bedrock.");
        provider.add("gtceu.machine.bedrock_ore_miner.production",
                "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.bedrock_ore_miner.depletion", "§bDepletion Rate: §f%s%%");
        provider.add("gtceu.multiblock.ore_rig.drilled_ores_list", "Ores:");
        provider.add("gtceu.multiblock.ore_rig.drilled_ore_entry", " - %s");
        provider.add("gtceu.multiblock.ore_rig.ore_amount", "Drilling Rate: %s");

        // Cleanroom
        provider.add("gtceu.machine.cleanroom.tooltip", "Keeping those pesky particles out");
        provider.addMultiLang("gtceu.machine.cleanroom.tooltip",
                "Place machines inside to run cleanroom recipes.",
                "Uses §f30 EU/t§7 when dirty, §f4 EU/t§7 when clean.",
                "Overclocking increases cleaning per cycle.",
                "§bSize: §f5x5x5 to 15x15x15",
                "Requires §fFilter Casings §7in the ceiling, excluding the edges.",
                "Accepts up to §f4 Doors§7! Remains clean when the door is open.",
                "Generators, Mufflers, Drills, and Primitive Machines are too dirty for the cleanroom!",
                "Send power through §fHulls §7or §fDiodes §7in the walls.");
        provider.add("gtceu.machine.cleanroom.tooltip.hold_ctrl",
                "Hold CTRL to show additional Structure Information");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.channels",
                "Send up to §f8 AE2 Channels §7through §fHulls§7 in the walls.");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.no_channels",
                "Send §aAE2 Networks§7 through §fHulls§7 in the walls.");
        provider.add("gtceu.multiblock.cleanroom.clean_state", "Status: §aCLEAN");
        provider.add("gtceu.multiblock.cleanroom.dirty_state", "Status: §4CONTAMINATED");
        provider.add("gtceu.multiblock.cleanroom.clean_amount", "Cleanliness: §a%s%%");
        provider.addMultiline("gtceu.multiblock.dimensions", "Dimensions: \n" +
                                                             "  §c§lWidth§r: %s, §a§lHeight§r: %s, §9§lDepth§r: %s ");

        // Power Substation
        provider.addMultiLang("gtceu.machine.power_substation.tooltip",
                "The heart of a centralized power grid",
                "§fCapacitors§7 do not need to be all the same tier.",
                "Allows up to §f%d Capacitor Layers§7.",
                "Loses energy equal to §f1%%§7 of total capacity every §f24 hours§7.",
                "Capped at §f%d kEU/t§7 passive loss per Capacitor Block.",
                "Can use",
                " Laser Hatches§7.");
        provider.add("gtceu.multiblock.power_substation.stored", "§7Stored: %s §7EU");
        provider.add("gtceu.multiblock.power_substation.capacity", "§7Capacity: %s §7EU");
        provider.add("gtceu.multiblock.power_substation.passive_drain", "§7Passive Drain: %s §7EU/t");
        provider.add("gtceu.multiblock.power_substation.average_in", "§7Avg. Input: %s §7EU/t");
        provider.add("gtceu.multiblock.power_substation.average_in_hover",
                "The average inserted EU into the Power Substation's internal energy bank");
        provider.add("gtceu.multiblock.power_substation.average_out", "§7Avg. Output: %s §7EU/t");
        provider.add("gtceu.multiblock.power_substation.average_out_hover",
                "The average extracted EU out of the Power Substation's internal energy bank");
        provider.add("gtceu.multiblock.power_substation.time_to_fill", "§7Time to fill: %s");
        provider.add("gtceu.multiblock.power_substation.time_to_drain", "§7Time to drain: %s");
        provider.add("gtceu.multiblock.power_substation.time_forever", "Forever");
        provider.add("gtceu.multiblock.power_substation.under_one_hour_left",
                "Less than 1 hour until fully drained!");

        // Active Transformer
        provider.addMultiLang("gtceu.machine.active_transformer.tooltip",
                "§7Transformers: Lasers in Disguise",
                "§7Can combine any number of Energy §fInputs§7 into any number of Energy §fOutputs§7.",
                "§7Can transmit power at incredible distance with",
                " Lasers§7.");
        provider.add("gtceu.multiblock.active_transformer.average_in", "§bAvg. Input: §f%s EU/t");
        provider.add("gtceu.multiblock.active_transformer.average_out", "§bAvg. Output: §f%s EU/t");
        provider.add("gtceu.multiblock.active_transformer.max_input", "§aMax Input: §f%s EU/t");
        provider.add("gtceu.multiblock.active_transformer.max_output", "§cMax Output: §f%s EU/t");
        provider.add("gtceu.multiblock.active_transformer.danger_enabled", "§c§bDANGER: Explosive");

        // Charcoal Pile Ignitor
        provider.add("gtceu.machine.charcoal_pile.tooltip", "Underground fuel bakery");
        provider.addMultiLang("gtceu.machine.charcoal_pile.tooltip",
                "Turns Logs into §aCharcoal§7 when §cignited§7.",
                "Right Click with fire-starting items to start.",
                "Pyrolysis occurs in up to a §b9x4x9§7 space beneath.",
                "Logs must be not be exposed to §eAir§7!");
        provider.addMultiline("gtceu.multiblock.charcoal_pile.description",
                """
                        Converts logs into Brittle Charcoal in a 9x4x9 area beneath it.
                        
                        The floor of the pit must be made from bricks, and any ground-related block can be used for the walls and roof.
                        No air can be inside the pit.
                        
                        Larger pits take more time to process logs, but are more efficient.""");

        // Central Monitor
        provider.add("gtceu.multiblock.central_monitor.low_power", "Low Power");
        provider.add("gtceu.multiblock.central_monitor.height", "Screen Height:");
        provider.add("gtceu.multiblock.central_monitor.width", "Screen Width: %d");
        provider.add("gtceu.multiblock.central_monitor.height_modify", "Modify Height: %d");
        provider.addMultiLang("gtceu.multiblock.central_monitor.tooltip",
                "This is a machine that monitors machines proxied by the Digital Interface Cover. You can easily monitor the Fluids, Items, Energy, and States of machines proxied in Energy Network.",
                "You can build the central monitor screen from 3X2 to %dX%d (width X height).",
                "The default height is 3. You can adjust the screen height in the GUI before the structure is formed.",
                "Energy consumption: %d EU/s for each screen.");
        provider.addMultiLang("gtceu.multiblock.monitor_screen.tooltip",
                "The GUI can be opened with a right-click of a screwdriver.",
                "The proxy mode of Digital Interface Cover can delegate machines' capabilities and GUI. (Yes, you can connect pipes directly on the screen.)",
                "The screen also supports plugins.");
        provider.add("gtceu.machine.central_monitor.tooltip", "But can it run Doom?");

        // Processing Array
        provider.add("gtceu.machine.processing_array.tooltip", "When a few Machines just doesn't cut it");
        provider.add("gtceu.machine.advanced_processing_array.tooltip", "Parallelize the World");

        // Research Station
        provider.addMultiLang("gtceu.machine.research_station.tooltip",
                "More than just a Multiblock Scanner",
                "Used to scan onto §fData Orbs§7 and §fData Modules§7.",
                "Requires §fComputation§7 to work.",
                "Providing more Computation allows the recipe to run faster.");
        provider.add("gtceu.multiblock.research_station.description",
                "The Research Station is a multiblock structure used for researching much more complex Assembly Line Research Data. Any Research requiring a Data Orb or Data Module must be scanned in the Research Station. Requires Compute Work Units (CWU/t) to research recipes, which is supplied by High Performance Computing Arrays (HPCAs).");
        provider.add("gtceu.machine.research_station.researching", "Researching.");

        // Network Switch
        provider.addMultiLang("gtceu.machine.network_switch.tooltip",
                "Ethernet Hub",
                "Used to route and distribute §fComputation§7.",
                "Can combine any number of Computation §fReceivers§7 into any number of Computation §fTransmitters§7.",
                "Uses §f%s EU/t§7 per Computation Data Hatch.");
        provider.add("gtceu.multiblock.network_switch.description",
                "The Network Switch is a multiblock structure used for distributing Computation from many sources to many destinations. It can accept any number of Computation Data Reception or Transmission Hatches. It is necessary for Research Data which requires much higher Computation, as the Research Station can only accept one Computation Data Reception Hatch. HPCAs must have a Bridge Component for the Network Switch to be able to access their Computation.");

        // Data Bank
        provider.addMultiLang("gtceu.machine.data_bank.tooltip",
                "Your Personal NAS",
                "Bulk Data Storage. Transfer with Optical Cables.",
                "Data Banks can be chained together.",
                "Uses §f%s EU/t§7 per Data/Optical Hatch normally.",
                "Uses §f%s EU/t§7 per Data/Optical Hatch when chained.");
        provider.add("gtceu.multiblock.data_bank.description",
                "The Data Bank is a multiblock structure used for sharing Assembly Line Research Data between multiple Assembly Lines. Additionally, it enables Assembly Lines to read more complex research data on Data Modules.");

        provider.add("gtceu.multiblock.data_bank.providing", "Providing data.");

        // HPCA
        provider.addMultiLang("gtceu.machine.high_performance_computation_array.tooltip",
                "Just your average Supercomputer",
                "Used to generate §fComputation§7 (and heat).",
                "Requires HPCA components to generate §fCWU/t§7 (Compute Work Units).");
        provider.add("gtceu.multiblock.hpca.description",
                "The High Performance Computing Array (HPCA) is a multiblock structure used for creating Compute Work Units (CWU/t) for more complex Assembly Line Research Data. The structure has a flexible 3x3 area which can be filled in any way with HPCA components. Different components can provide different amounts of Computation, Cooling, as well as Energy Cost, Coolant Cost, and Heat Production. When used with a Bridge Component, the HPCA can connect to Network Switches for combining and routing Computation from multiple sources to one or more destinations.");
        provider.add("gtceu.multiblock.hpca.computation", "Providing: %s");
        provider.add("gtceu.multiblock.hpca.energy", "Using: %s / %s EU/t (%s)");
        provider.add("gtceu.multiblock.hpca.temperature", "Temperature: %s");
        provider.add("gtceu.multiblock.hpca.hover_for_info", "Hover for details");
        provider.add("gtceu.multiblock.hpca.error_damaged", "Damaged component in structure!");
        provider.add("gtceu.multiblock.hpca.error_temperature",
                "Temperature above 100C, components may be damaged!");
        provider.add("gtceu.multiblock.hpca.warning_temperature",
                "Temperature above 50C, components may be damaged at 100C!");
        provider.add("gtceu.multiblock.hpca.warning_temperature_active_cool", "Fully utilizing active coolers");
        provider.add("gtceu.multiblock.hpca.warning_structure_header", "Structure Warnings:");
        provider.add("gtceu.multiblock.hpca.warning_multiple_bridges",
                "- Multiple bridges in structure (provides no additional benefit)");
        provider.add("gtceu.multiblock.hpca.warning_no_computation", "- No computation providers");
        provider.add("gtceu.multiblock.hpca.warning_low_cooling", "- Not enough cooling");
        provider.add("gtceu.multiblock.hpca.info_max_computation", "Max CWU/t: %s");
        provider.add("gtceu.multiblock.hpca.info_max_cooling_demand", "Cooling Demand: %s");
        provider.add("gtceu.multiblock.hpca.info_max_cooling_available", "Cooling Available: %s");
        provider.add("gtceu.multiblock.hpca.info_max_coolant_required", "Coolant Needed: %s");
        provider.add("gtceu.multiblock.hpca.info_coolant_name", "PCB Coolant");
        provider.add("gtceu.multiblock.hpca.info_bridging_enabled", "Bridging Enabled");
        provider.add("gtceu.multiblock.hpca.info_bridging_disabled", "Bridging Disabled");

        // Multiblock Tanks
        provider.add("gtceu.machine.multiblock.tank.tooltip",
                "Fill and drain through the controller or tank valves.");
        provider.add("gtceu.machine.tank_valve.tooltip",
                "Use to fill and drain multiblock tanks. Auto outputs when facing down.");
        provider.add("gtceu.machine.fluid_tank.max_multiblock", "Max Multiblock Size: %dx%dx%d");
        provider.add("gtceu.machine.fluid_tank.fluid", "Contains %s L of %s");
    }

    private static void generateCustomMultiblockPartTooltips(GTLangProvider provider) {
        // Primitive Pump Hatch
        provider.add("gtceu.machine.pump_hatch.tooltip", "Primitive Fluid Output for Water Pump");

        // Coke Oven Hatch
        provider.add("gtceu.machine.coke_oven_hatch.tooltip", "§7Allows automation access for the Coke Oven.");

        // Maintenance Hatch
        provider.add("gtceu.machine.maintenance_hatch.tooltip", "For maintaining Multiblocks");
        provider.addMultiline("gtceu.machine.maintenance_hatch_configurable.tooltip",
                "For finer control over Multiblocks\nStarts with no Maintenance problems!");
        provider.add("gtceu.machine.maintenance_hatch_full_auto.tooltip",
                "For automatically maintaining Multiblocks");
        provider.addMultiLang("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip",
                "For automatically maintaining Multiblocks with Cleaning!", "Cleans as:");
        provider.add("gtceu.machine.maintenance_hatch_tool_slot.tooltip",
                "Click slot with empty hand when required tools are in inventory to solve problems");
        provider.add("gtceu.machine.maintenance_hatch_tape_slot.tooltip", "Insert Tape to prevent problems");
        provider.add("gtceu.maintenance.configurable_duration", "Duration: %fx");
        provider.add("gtceu.maintenance.configurable_duration.unchanged_description",
                "Recipes will run at normal speed. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_duration.changed_description",
                "Recipes will run with %fx duration, applied before overclocking.");
        provider.add("gtceu.maintenance.configurable_duration.modify", "Modify Duration:");
        provider.add("gtceu.maintenance.configurable_time", "Time: %fx");
        provider.add("gtceu.maintenance.configurable_time.unchanged_description",
                "Maintenance problems will occur at normal rate. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_time.changed_description",
                "Maintenance problems will occur at %fx the normal rate.");

        // Muffler Hatch
        provider.addMultiLang("gtceu.machine.muffler_hatch.tooltip", "Recovers waste from machines",
                "DO NOT OBSTRUCT THE OUTPUT!");
        provider.add("gtceu.muffler.recovery_tooltip", "§bRecovery Chance: §f%d%%");

        // Parallel Hatch
        provider.add("gtceu.machine.parallel_hatch.display", "Adjust the maximum parallel of the multiblock");
        provider.add("gtceu.multiblock.parallelizable.tooltip", "Can parallelize with Parallel Control Hatches.");
        provider.add("gtceu.machine.parallel_hatch_mk5.tooltip", "Allows to run up to 4 recipes in parallel.");
        provider.add("gtceu.machine.parallel_hatch_mk6.tooltip", "Allows to run up to 16 recipes in parallel.");
        provider.add("gtceu.machine.parallel_hatch_mk7.tooltip", "Allows to run up to 64 recipes in parallel.");
        provider.add("gtceu.machine.parallel_hatch_mk8.tooltip",
                "Allows to run up to 256 recipes in parallel.");

        // Item/Fluid Hatches
        provider.add("gtceu.machine.item_bus.import.tooltip", "Item Input for Multiblocks");
        provider.add("gtceu.machine.item_bus.export.tooltip", "Item Output for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.import.tooltip", "Fluid Input for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.export.tooltip", "Fluid Output for Multiblocks");

        // Pattern Buffer/Proxy
        provider.add("block.gtceu.pattern_buffer.desc.0",
                "§fAllows direct §6AE2 pattern storage §ffor GregTech Multiblocks.");
        provider.add("block.gtceu.pattern_buffer.desc.1",
                "§fAE2 Patterns can utilize anything stored in the §6shared inventory §fwidget.");
        provider.add("block.gtceu.pattern_buffer.desc.2",
                "§fLink §6Pattern Buffer Proxies §fwith a §bdatastick §fto link machines together!");
        provider.add("block.gtceu.pattern_buffer_proxy.desc.0",
                "§fAllows linking many machines to a singular §6ME Pattern Buffer§f.");
        provider.add("block.gtceu.pattern_buffer_proxy.desc.1",
                "§fAll connected proxies will share the patterns held within the §6original buffer§f.");
        provider.add("block.gtceu.pattern_buffer_proxy.desc.2",
                "§fLet the factory grow!");
        provider.add("gtceu.tooltip.proxy_bind",
                "§fBinding to a Pattern Buffer at %s %s %s");

        provider.add("gui.gtceu.share_inventory.title", "Shared Item Inventory");
        provider.add("gui.gtceu.share_inventory.desc.0", "Shares inserted items with all patterns within buffer!");
        provider.add("gui.gtceu.share_inventory.desc.1", "Allows powerful automation by storing catalysts");
        provider.add("gui.gtceu.share_tank.title", "Shared Tank Inventory");
        provider.add("gui.gtceu.share_tank.desc.0",
                "Shares inserted fluids/gasses/etc. with all patterns within buffer!");
        provider.add("gui.gtceu.rename.desc", "Rename Pattern Buffer");
        provider.add("gui.gtceu.refund_all.desc", "Return Stored Contents to AE2");

        // ME Hatches
        provider.add("gtceu.machine.me.item_export.tooltip", "Stores items directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_export.tooltip", "Stores fluids directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_import.tooltip",
                "Fetches fluids from an ME network automatically.");
        provider.add("gtceu.machine.me.item_import.tooltip", "Fetches items from an ME network automatically.");
        provider.add("gtceu.machine.me.export.tooltip",
                "Has infinite capacity before connecting to ME network.");

        // ME Stocking
        provider.addMultiLang("gtceu.machine.me.stocking_item.tooltip", "Retrieves items directly from the ME network",
                "Auto-Pull from ME mode will automatically stock the first 16 items in the ME system, updated every 5 seconds.");
        provider.addMultiLang("gtceu.machine.me.stocking_fluid.tooltip", "Retrieves fluids directly from the ME network",
                "Auto-Pull from ME mode will automatically stock the first 16 fluids in the ME system, updated every 5 seconds.");
        provider.add("gtceu.machine.me_import_item_hatch.configs.tooltip",
                "Keeps 16 item types in stock");
        provider.add("gtceu.machine.me_import_fluid_hatch.configs.tooltip",
                "Keeps 16 fluid types in stock");
        provider.add("gtceu.machine.me.stocking_auto_pull_enabled",
                "Auto-Pull Enabled");
        provider.add("gtceu.machine.me.stocking_auto_pull_disabled",
                "Auto-Pull Disabled");

        // ME Data Stick Behavior
        provider.add("gtceu.machine.me.copy_paste.tooltip",
                "Sneak right-click with Data Stick to copy settings, right-click to apply");
        provider.add("gtceu.machine.me.import_copy_settings",
                "Saved settings to Data Stick");
        provider.add("gtceu.machine.me.import_paste_settings",
                "Applied settings from Data Stick");
        provider.add("gtceu.machine.me.item_import.data_stick.name",
                "§oME Input Bus Configuration Data");
        provider.add("gtceu.machine.me.fluid_import.data_stick.name",
                "§oME Input Hatch Configuration Data");

        // Dual Hatch
        provider.add("gtceu.machine.dual_hatch.import.tooltip", "Item and Fluid Input for Multiblocks");
        provider.add("gtceu.machine.dual_hatch.export.tooltip", "Item and Fluid Output for Multiblocks");

        // Energy Hatch
        provider.add("gtceu.machine.energy_hatch.input.tooltip", "Energy Input for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.input_hi_amp.tooltip",
                "Multiple Ampere Energy Input for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.input.tooltip", "Energy Input for the Power Substation");
        provider.add("gtceu.machine.energy_hatch.output.tooltip", "Energy Output for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.output_hi_amp.tooltip",
                "Multiple Ampere Energy Output for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.output.tooltip", "Energy Output for the Power Substation");

        // Rotor Holder
        provider.addMultiLang("gtceu.machine.rotor_holder.tooltip", "Rotor Holder for Multiblocks",
                "Holds Rotor in place so it will not fly away");

        // Data Hatches
        provider.addMultiLang("gtceu.machine.data_access_hatch.tooltip", "Data Access for Multiblocks",
                "Adds §a%s§7 slots for Data Items");
        provider.add("gtceu.machine.data_receiver_hatch.tooltip", "Research Data Input for Multiblocks");
        provider.add("gtceu.machine.data_transmitter_hatch.tooltip", "Research Data Output for Multiblocks");

        // Computation Hatches
        provider.add("gtceu.machine.computation_transmitter_hatch.tooltip",
                "Computation Data Output for Multiblocks");
        provider.add("gtceu.machine.computation_receiver_hatch.tooltip",
                "Computation Data Input for Multiblocks");

        // HPCA Parts
        provider.add("gtceu.machine.hpca.component_general.upkeep_eut", "§eUpkeep Energy: §f%d EU/t");
        provider.add("gtceu.machine.hpca.component_general.max_eut", "§6Max Energy: §f%d EU/t");
        provider.add("gtceu.machine.hpca.component_type.cooler_passive", "§bCooler Type: §fPassive");
        provider.add("gtceu.machine.hpca.component_type.cooler_active", "§bCooler Type: §fActive");
        provider.add("gtceu.machine.hpca.component_type.cooler_cooling", "§aProvides: §f%d Cooling");
        provider.add("gtceu.machine.hpca.component_type.cooler_active_coolant",
                "§cRequires up to: §f%d mB/t %s");
        provider.add("gtceu.machine.hpca.component_type.computation_cwut", "§9Computation: §f%d CWU/t");
        provider.add("gtceu.machine.hpca.component_type.computation_cooling", "§cRequires up to: §f%d Cooling");
        provider.add("gtceu.machine.hpca.component_type.bridge",
                "Allows §fHPCA§7 to connect to §fNetwork Switches§7");
        provider.add("gtceu.machine.hpca.component_type.damaged", "Can be damaged by HPCA overheating!");

        provider.add("gtceu.machine.hpca.empty_component.tooltip", "Just for filling space");
        provider.add("gtceu.machine.hpca.heat_sink_component.tooltip", "Free cooling! Is anything free?");
        provider.add("gtceu.machine.hpca.active_cooler_component.tooltip", "Less free, more effective cooling");
        provider.add("gtceu.machine.hpca.computation_component.tooltip", "Baby's first computation");
        provider.add("gtceu.machine.hpca.advanced_computation_component.tooltip", "Computation big leagues");
        provider.add("gtceu.machine.hpca.bridge_component.tooltip",
                "So that's where the \"Array\" in HPCA comes from");

        provider.add("gtceu.machine.hpca.computation_component.damaged.name",
                "Damaged HPCA Computation Component");
        provider.add("gtceu.machine.hpca.computation_component.damaged.tooltip", "Free recyclable materials");
        provider.add("gtceu.machine.hpca.advanced_computation_component.damaged.name",
                "Damaged HPCA Advanced Computation Component");
        provider.add("gtceu.machine.hpca.advanced_computation_component.damaged.tooltip",
                "It only cost an arm and a leg");

        // Object Holder
        provider.add("gtceu.machine.object_holder.tooltip", "Advanced Holding Mechanism for Research Station");

        // Passthrough Hatch
        provider.add("gtceu.machine.passthrough_hatch_item.tooltip", "Sends Items from one Side to the other");
        provider.add("gtceu.machine.passthrough_hatch_fluid.tooltip",
                "Sends Fluids from one Side to the other");

        // Laser Hatches
        provider.add("gtceu.machine.laser_hatch.source.tooltip", "§7Transmitting power at distance");
        provider.add("gtceu.machine.laser_hatch.target.tooltip", "§7Receiving power from distance");
        provider.add("gtceu.machine.laser_hatch.both.tooltip", "§cLaser Cables must be in a straight line!§7");
        provider.addMultiLang("gtceu.machine.laser_source_hatch.tooltip",
                "Transmitting power at distance",
                "§cLaser cables must be in a straight line!§7");

        provider.addMultiLang("gtceu.machine.laser_target_hatch.tooltip",
                "Receiving power from distance",
                "§cLaser cables must be in a straight line!§7");

        // Processing Array Machine Hatch
        provider.add("gtceu.machine.machine_hatch.locked", "Machine Interface Locked");
        provider.add("gtceu.machine.machine_hatch.tooltip",
                "Specialized Access Bus that only holds valid items");
        provider.add("gtceu.machine.machine_hatch.processing_array",
                "When in the §eProcessing Array§7, only holds machines that work in the §eProcessing Array");
    }

    /**
     * Generates tooltips for both variants of steam machine
     */
    private static void makeSteamMachineTooltip(GTLangProvider provider, String modId, String machineName,
                                                String lpTooltip, String hpTooltip) {
        provider.add("machine.%s.lp_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + lpTooltip);
        provider.add("machine.%s.hp_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + hpTooltip);
    }

    /**
     * Generates tooltips for LV-HV machines
     */
    private static void makeElectricMachineTooltipLVtoHV(GTLangProvider provider, String modId, String machineName,
                                                         String lvTooltip, String mvTooltip, String hvTooltip) {
        makeElectricMachineTooltipULVtoMAX(provider, modId, machineName,
                null, lvTooltip, mvTooltip, hvTooltip,
                null, null, null, null,
                null, null, null, null,
                null, null, null);
    }

    /**
     * Generates tooltips for LV-UV machines
     */
    private static void makeElectricMachineTooltipLVtoUV(GTLangProvider provider, String modId, String machineName,
                                                         String lvTooltip, String mvTooltip, String hvTooltip,
                                                         String evTooltip, String ivTooltip, String luvTooltip,
                                                         String zpmTooltip, String uvTooltip) {
        makeElectricMachineTooltipULVtoMAX(provider, modId, machineName,
                null, lvTooltip, mvTooltip, hvTooltip,
                evTooltip, ivTooltip, luvTooltip, zpmTooltip,
                uvTooltip, null, null, null,
                null, null, null);
    }

    /**
     * Generates tooltips for ULV-MAX Machines <br>
     * If a parameter is {@code null}, it won't be added.
     */
    private static void makeElectricMachineTooltipULVtoMAX(GTLangProvider provider, String modId, String machineName,
                                                           String ulvTooltip, String lvTooltip, String mvTooltip,
                                                           String hvTooltip, String evTooltip, String ivTooltip,
                                                           String luvTooltip, String zpmTooltip, String uvTooltip,
                                                           String uhvTooltip, String uevTooltip, String uivTooltip,
                                                           String uxvTooltip, String opvTooltip, String maxTooltip) {
        if (ulvTooltip != null) {
            provider.add("machine.%s.ulv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + ulvTooltip);
        }
        if (lvTooltip != null) {
            provider.add("machine.%s.lv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + lvTooltip);
        }
        if (mvTooltip != null) {
            provider.add("machine.%s.mv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + mvTooltip);
        }
        if (hvTooltip != null) {
            provider.add("machine.%s.hv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + hvTooltip);
        }
        if (evTooltip != null) {
            provider.add("machine.%s.ev_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + evTooltip);
        }
        if (ivTooltip != null) {
            provider.add("machine.%s.iv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + ivTooltip);
        }
        if (luvTooltip != null) {
            provider.add("machine.%s.luv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + luvTooltip);
        }
        if (zpmTooltip != null) {
            provider.add("machine.%s.zpm_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + zpmTooltip);
        }
        if (uvTooltip != null) {
            provider.add("machine.%s.uv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + uvTooltip);
        }
        if (uhvTooltip != null) {
            provider.add("machine.%s.uhv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + uhvTooltip);
        }
        if (uevTooltip != null) {
            provider.add("machine.%s.uev_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + uevTooltip);
        }
        if (uivTooltip != null) {
            provider.add("machine.%s.uiv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + uivTooltip);
        }
        if (uxvTooltip != null) {
            provider.add("machine.%s.uxv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + uxvTooltip);
        }
        if (opvTooltip != null) {
            provider.add("machine.%s.opv_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + opvTooltip);
        }
        if (maxTooltip != null) {
            provider.add("machine.%s.max_%s.tooltip".formatted(modId, machineName), ChatFormatting.GRAY + maxTooltip);
        }
    }

    public static void generateMultiblockKeys(GTLangProvider provider) {
        // Recipe Logic
        provider.add("gtceu.multiblock.work_paused", "Work Paused.");
        provider.add("gtceu.multiblock.running", "Running perfectly.");
        provider.add("gtceu.multiblock.idling", "§6Idling.");

        // Creative Mode
        provider.add("gtceu.creative_tooltip.1", "§7You just need");
        provider.add("gtceu.creative_tooltip.2", " Creative Mode");
        provider.add("gtceu.creative_tooltip.3", "§7 to use this");

        // Researching
        provider.add("gtceu.multiblock.research_station.researching", "§6Researching.");

        // Energy Requirement
        provider.add("gtceu.multiblock.not_enough_energy", "WARNING: Machine needs more energy.");
        provider.add("gtceu.multiblock.not_enough_energy_output", "WARNING: Energy Dynamo Tier Too Low!");

        // Recipe Progress
        provider.add("gtceu.multiblock.waiting", "WARNING: Machine is waiting.");
        provider.add("gtceu.multiblock.progress_percent", "Progress: %s%%");
        provider.add("gtceu.multiblock.progress", "Progress: %ss / %ss (%s%%)");

        // Recipe Outputs
        provider.add("gtceu.multiblock.output_line.0", "%s x §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.1", "%s x §e%s§r (%s/s)");
        provider.add("gtceu.multiblock.output_line.2", "%s ≈ §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.3", "%s ≈ §e%s§r (%s/s)");

        // Structure Validation
        provider.add("gtceu.multiblock.invalid_structure", "Invalid structure.");
        provider.add("gtceu.multiblock.invalid_structure.tooltip",
                "This block is a controller of the multiblock structure. For building help, see structure template in JEI.");
        provider.add("gtceu.multiblock.validation_failed", "Invalid amount of inputs/outputs.");

        // Pattern Preview
        provider.add("gtceu.multiblock.title", "Multiblock Pattern");

        // Hatch Limitation - Mostly for GCYM Multiblocks that are Hardcoded into GTCEU
        provider.add("gtceu.multiblock.exact_hatch_1.tooltip", "§fAccepts Exactly §6One §fEnergy Hatch.");

        // Recipe Tier
        provider.add("gtceu.multiblock.max_recipe_tier", "Max Recipe Tier: %s");
        provider.add("gtceu.multiblock.max_recipe_tier_hover", "The maximum tier of recipes that can be run");

        // Energy Consumption/Generation
        provider.add("gtceu.multiblock.max_energy_per_tick", "Max EU/t: §a%s (%s§r)");
        provider.add("gtceu.multiblock.max_energy_per_tick_hover",
                "The maximum EU/t available for running recipes or overclocking");
        provider.add("gtceu.multiblock.max_energy_per_tick_amps", "Max EU/t: %s (%sA %s)");
        provider.add("gtceu.multiblock.energy_consumption", "Energy Usage: %s EU/t (%s)");
        provider.add("gtceu.multiblock.generation_eu", "Outputting: §a%s EU/t");

        // Maintenence
        provider.add("gtceu.multiblock.universal.no_problems", "No Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems", "Has Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems_header",
                "Fix the following issues in a Maintenance Hatch:");
        provider.add("gtceu.multiblock.universal.problem.wrench", "§7Pipe is loose. (§aWrench§7)");
        provider.add("gtceu.multiblock.universal.problem.screwdriver", "§7Screws are loose. (§aScrewdriver§7)");
        provider.add("gtceu.multiblock.universal.problem.soft_mallet", "§7Something is stuck. (§aSoft Mallet§7)");
        provider.add("gtceu.multiblock.universal.problem.hard_hammer", "§7Plating is dented. (§aHard Hammer§7)");
        provider.add("gtceu.multiblock.universal.problem.wire_cutter", "§7Wires burned out. (§aWire Cutter§7)");
        provider.add("gtceu.multiblock.universal.problem.crowbar", "§7That doesn't belong there. (§aCrowbar§7)");

        // Muffler
        provider.add("gtceu.multiblock.universal.muffler_obstructed", "Muffler Hatch is Obstructed!");
        provider.add("gtceu.multiblock.universal.muffler_obstructed.tooltip",
                "Muffler Hatch must have a block of airspace in front of it.");

        // Rotor
        provider.add("gtceu.multiblock.universal.rotor_obstructed", "Rotor is Obstructed!");

        // Distinctness
        provider.add("gtceu.multiblock.universal.distinct", "Distinct Buses:");
        provider.add("gtceu.multiblock.universal.distinct.no", "No");
        provider.add("gtceu.multiblock.universal.distinct.yes", "Yes");
        provider.add("gtceu.multiblock.universal.distinct.info",
                "If enabled, each Item Input Bus will be treated as fully distinct from each other for recipe lookup. Useful for things like Programmed Circuits, Extruder Shapes, etc.");

        // Parallel
        provider.add("gtceu.multiblock.parallel", "Performing up to %d Recipes in Parallel");
        provider.add("gtceu.multiblock.parallel.exact", "Performing %d Recipes in Parallel");

        // Active RecipeMap
        provider.add("gtceu.multiblock.multiple_recipemaps.header", "Machine Mode:");
        provider.add("gtceu.multiblock.multiple_recipemaps.tooltip",
                "Screwdriver the controller to change which machine mode to use.");
        provider.add("gtceu.multiblock.multiple_recipemaps_recipes.tooltip", "Machine Modes: §e%s§r");
        provider.add("gtceu.multiblock.multiple_recipemaps.switch_message",
                "The machine must be off to switch modes!");
        provider.add("gtceu.gui.machinemode.title", "Active Machine Mode");
        provider.add("gtceu.gui.machinemode", "Active Machine Mode: %s");
        provider.add("machine.gtceu.available_recipe_map_1.tooltip", "Available Recipe Types: %s");
        provider.add("machine.gtceu.available_recipe_map_2.tooltip", "Available Recipe Types: %s, %s");
        provider.add("machine.gtceu.available_recipe_map_3.tooltip", "Available Recipe Types: %s, %s, %s");
        provider.add("machine.gtceu.available_recipe_map_4.tooltip", "Available Recipe Types: %s, %s, %s, %s");

        // Multiblock Preview
        provider.add("gtceu.multiblock.preview.zoom", "Use mousewheel or right-click + drag to zoom");
        provider.add("gtceu.multiblock.preview.rotate", "Click and drag to rotate");
        provider.add("gtceu.multiblock.preview.select", "Right-click to check candidates");
        provider.add("gtceu.multiblock.pattern.error", "Expected components (%s) at (%s).");
        provider.add("gtceu.multiblock.pattern.error.limited_exact", "§cExactly: %d§r");
        provider.add("gtceu.multiblock.pattern.error.limited_within", "§cBetween %d and %d§r");
        provider.addMultiLang("gtceu.multiblock.pattern.error.limited", "§cMaximum: %d§r", "§cMinimum: %d§r",
                "§cMaximum: %d per layer§r", "§cMinimum: %d per layer§r");
        provider.add("gtceu.multiblock.pattern.error.coils", "§cAll heating coils must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.filters", "§cAll filters must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.batteries", "§cAll batteries must be the same§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_1", "§6Must have a clear 1x1x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_3", "§6Must have a clear 3x3x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.single", "§6Only this block can be used§r");
        provider.add("gtceu.multiblock.pattern.location_end", "§cVery End§r");
        provider.add("gtceu.multiblock.pattern.replaceable_air", "Replaceable by Air");

        // Computation
        provider.add("gtceu.multiblock.computation.max", "Max CWU/t: %s");
        provider.add("gtceu.multiblock.computation.usage", "Using: %s");
        provider.add("gtceu.multiblock.computation.non_bridging", "Non-bridging connection found");
        provider.add("gtceu.multiblock.computation.non_bridging.detailed",
                "A Reception Hatch is linked to a machine which cannot bridge");
        provider.add("gtceu.multiblock.computation.not_enough_computation", "Machine needs more computation!");

        // Hatch Collapsing
        provider.add("gtceu.bus.collapse_true", "Bus will collapse Items");
        provider.add("gtceu.bus.collapse_false", "Bus will not collapse Items");
        provider.add("gtceu.bus.collapse.error", "Bus must be attached to multiblock first");

        // XEI Categories
        provider.add("gtceu.auto_decomp.rotor", "Turbine Rotor");
        provider.add("gtceu.auto_decomp.tool", "Non-electric tool");

        provider.add("gtceu.machine.canner.jei_description",
                "You can fill and empty any fluid containers with the Fluid Canner (e.g. Buckets or Fluid Cells)");
    }
}
