package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;
import com.gregtechceu.gtceu.common.data.GTOres;
import com.gregtechceu.gtceu.utils.FormattingUtil;

public class IntegrationLang {

    public static void init(GTLangProvider provider) {
        initRecipeViewerLang(provider);
        initWailaLikeLang(provider);
        initMinimapLang(provider);
        initOwnershipLang(provider);

        // Curios
        provider.add("curios.identifier.gtceu_magnet", "GTCEu Magnet");
    }

    /** JEI, REI, EMI */
    private static void initRecipeViewerLang(GTLangProvider provider) {
        provider.add("gtceu.jei.multiblock_info", "Multiblock Info");

        provider.add("gtceu.jei.ore_processing_diagram", "Ore Processing Diagram");
        provider.add("gtceu.jei.ore_vein_diagram", "Ore Vein Diagram");

        provider.add("gtceu.jei.programmed_circuit", "Programmed Circuit Page");

        provider.add("gtceu.jei.bedrock_fluid_diagram", "Bedrock Fluid Diagram");
        provider.add("gtceu.jei.bedrock_ore_diagram", "Bedrock Ore Diagram");

        // Ore Veins
        provider.add("ore.spawnlocation.name", "Ore Spawn Information");

        provider.add("gtceu.jei.ore_vein_diagram.chance", "§eChance: %s§r");
        provider.add("gtceu.jei.ore_vein_diagram.spawn_range", "Spawn Range:");
        provider.add("gtceu.jei.ore_vein_diagram.weight", "Weight: %s");
        provider.add("gtceu.jei.ore_vein_diagram.dimensions", "Dimensions:");

        provider.addMultiLang("gtceu.jei.ore.primary", "Top Ore",
                "Spawns in the top %d layers of the vein");
        provider.addMultiLang("gtceu.jei.ore.secondary", "Bottom Ore",
                "Spawns in the bottom %d layers of the vein");
        provider.addMultiLang("gtceu.jei.ore.between", "Between Ore",
                "Spawns in the middle %d layers of the vein, with other ores");
        provider.addMultiLang("gtceu.jei.ore.sporadic", "Sporadic Ore",
                "Spawns anywhere in the vein");

        provider.add("gtceu.jei.ore.biome_weighting_title", "§dModified Biome Total Weights:");
        provider.add("gtceu.jei.ore.biome_weighting", "§d%s Weight: §3%d");
        provider.add("gtceu.jei.ore.biome_weighting_no_spawn", "§d%s Weight: §cCannot Spawn");
        provider.add("gtceu.jei.ore.ore_weight", "Weight in vein: %d%%");

        // Surface Rock
        provider.addMultiLang("gtceu.jei.ore.surface_rock",
                "Surface Rocks with this material denote vein spawn locations.",
                "They can be broken for 3 Tiny Piles of the dust, with Fortune giving a bonus.");

        // Fluid vein
        provider.add("fluid.spawnlocation.name", "Fluid Vein Information");

        provider.add("gtceu.jei.fluid.vein_weight", "Vein Weight: %d");
        provider.add("gtceu.jei.fluid.min_yield", "Minimum Yield: %d");
        provider.add("gtceu.jei.fluid.max_yield", "Maximum Yield: %d");
        provider.add("gtceu.jei.fluid.depletion_chance", "Depletion Chance: %d%%");
        provider.add("gtceu.jei.fluid.depletion_amount", "Depletion Amount: %d");
        provider.add("gtceu.jei.fluid.depleted_rate", "Depleted Yield: %d");

        provider.add("gtceu.jei.fluid.dimension", "Dimensions:");

        provider.add("gtceu.jei.fluid.weight_hover",
                "The Weight of the vein. Hover over the fluid to see any possible biome modifications");
        provider.add("gtceu.jei.fluid.min_hover",
                "The minimum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.max_hover",
                "The maximum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.dep_chance_hover",
                "The percentage chance for the vein to be depleted upon harvest");
        provider.add("gtceu.jei.fluid.dep_amount_hover", "The amount the vein will be depleted by");
        provider.add("gtceu.jei.fluid.dep_yield_hover",
                "The maximum yield of the vein when it is fully depleted");

        // Vein Names
        GTRegistries.ORE_VEINS.unfreeze();
        GTOres.init();
        for (GTOreDefinition oreDefinition : GTRegistries.ORE_VEINS) {
            String name = GTRegistries.ORE_VEINS.getKey(oreDefinition).getPath();
            provider.add("gtceu.jei.ore_vein." + name, FormattingUtil.toEnglishName(name));
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        GTBedrockFluids.init();
        for (BedrockFluidDefinition fluid : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
            String name = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid).getPath();
            provider.add("gtceu.jei.bedrock_fluid." + name, FormattingUtil.toEnglishName(name));
        }

        // Potion
        provider.add("gtceu.rei.group.potion_fluids", "Potion Fluids");
    }

    /** Jade, TheOneProbe, WTHIT */
    private static void initWailaLikeLang(GTLangProvider provider) {
        provider.add("gtceu.top.working_disabled", "Working Disabled");
        provider.add("gtceu.top.energy_consumption", "Using");
        provider.add("gtceu.top.energy_production", "Producing");

        // Transformers
        provider.add("gtceu.top.transform_up", "§cStep Up§r %s");
        provider.add("gtceu.top.transform_down", "§aStep Down§r %s");
        provider.add("gtceu.top.transform_input", "§6Input:§r %s");
        provider.add("gtceu.top.transform_output", "§9Output:§r %s");
        provider.add("gtceu.top.convert_eu", "Converting §eEU§r -> §cFE§r");
        provider.add("gtceu.top.convert_fe", "Converting §cFE§r -> §eEU§r");

        // Generators
        provider.add("gtceu.top.fuel_min_consume", "Needs");
        provider.add("gtceu.top.fuel_none", "No fuel");

        // Multiblock Structures
        provider.add("gtceu.top.invalid_structure", "Structure Incomplete");
        provider.add("gtceu.top.valid_structure", "Structure Formed");
        provider.add("gtceu.top.obstructed_structure", "Structure Obstructed");

        // Maintenance
        provider.add("gtceu.top.maintenance_fixed", "Maintenance Fine");
        provider.add("gtceu.top.maintenance_broken", "Needs Maintenance");
        provider.add("gtceu.top.maintenance.wrench", "Pipe is loose");
        provider.add("gtceu.top.maintenance.screwdriver", "Screws are loose");
        provider.add("gtceu.top.maintenance.soft_mallet", "Something is stuck");
        provider.add("gtceu.top.maintenance.hard_hammer", "Plating is dented");
        provider.add("gtceu.top.maintenance.wire_cutter", "Wires burned out");
        provider.add("gtceu.top.maintenance.crowbar", "That doesn't belong there");

        // Steam Venting
        provider.add("gtceu.top.exhaust_vent_direction", "Exhaust Vent: %s");
        provider.add("gtceu.top.exhaust_vent_blocked", "Blocked");

        // Primitive Pump
        provider.add("gtceu.top.primitive_pump_production", "Production: %s mB/s");

        // Ender Link Cover??
        provider.add("gtceu.top.filter.label", "Filter:");
        provider.add("gtceu.top.link_cover.color", "Color:");
        provider.add("gtceu.top.mode.export", "Exporting");
        provider.add("gtceu.top.mode.import", "Importing");

        // Extra Cover Provider
        provider.add("gtceu.top.unit.items", "Items");
        provider.add("gtceu.top.unit.fluid_milibuckets", "L");
        provider.add("gtceu.top.unit.fluid_buckets", "kL");

        // Recipe Provider
        provider.add("gtceu.top.recipe_output", "Recipe Outputs:");
        provider.add("gtceu.top.item_auto_output", "Item Output: %s");
        provider.add("gtceu.top.fluid_auto_output", "Fluid Output: %s");

        // IO Provider
        provider.add("gtceu.top.auto_output", "Auto Output");
        provider.add("gtceu.top.allow_output_input", "Allow Input");

        // Cable Provider
        provider.add("gtceu.top.cable_voltage", "Voltage: ");
        provider.add("gtceu.top.cable_amperage", "Amperage: ");

        // Machine Mode Provider
        provider.add("gtceu.top.machine_mode", "Machine Mode: ");

        // Color Provider
        provider.add("gtceu.top.stained", "Colored: %s");

        // Proxy/Buffers
        provider.add("gtceu.top.buffer_not_bound", "Buffer Not Currently Bound");
        provider.add("gtceu.top.buffer_bound_pos", "Bound To - X: %s, Y: %s, Z: %s");
        provider.add("gtceu.top.proxies_bound", "Buffer Proxies Bound: %s");

        provider.add("gtceu.top.energy_stored", " / %d EU");
        provider.add("gtceu.top.progress_computation", " / %s CWU");
        provider.add("gtceu.top.progress_sec", " / %s s");
        provider.add("gtceu.top.progress_tick", " / %s t");

        provider.add("gtceu.jade.energy_stored", "%d / %d EU");
        provider.add("gtceu.jade.progress_computation", "%s / %s CWU");
        provider.add("gtceu.jade.progress_sec", "%s / %s s");
        provider.add("gtceu.jade.progress_tick", "%s / %s t");

        // Hazard Provider
        provider.add("gtceu.jade.cleaned_this_second", "Cleaned hazard: %s/s");

        // Plugin Names
        provider.add("config.jade.plugin_gtceu.controllable_provider", "[GTCEu] Controllable");
        provider.add("config.jade.plugin_gtceu.workable_provider", "[GTCEu] Workable");
        provider.add("config.jade.plugin_gtceu.electric_container_provider", "[GTCEu] Electric Container");
        provider.add("config.jade.plugin_gtceu.recipe_logic_provider", "[GTCEu] Recipe Logic");
        provider.add("config.jade.plugin_gtceu.hazard_cleaner_provider", "[GTCEu] Hazard Cleaner");
        provider.add("config.jade.plugin_gtceu.recipe_output_info", "[GTCEu] Recipe Output Info");
        provider.add("config.jade.plugin_gtceu.auto_output_info", "[GTCEu] Auto Output Info");
        provider.add("config.jade.plugin_gtceu.cable_info", "[GTCEu] Cable Info");
        provider.add("config.jade.plugin_gtceu.exhaust_vent_info", "[GTCEu] Exhaust Vent Info");
        provider.add("config.jade.plugin_gtceu.steam_boiler_info", "[GTCEu] Steam Boiler Info");
        provider.add("config.jade.plugin_gtceu.machine_mode", "[GTCEu] Machine Mode");
        provider.add("config.jade.plugin_gtceu.maintenance_info", "[GTCEu] Maintenance Info");
        provider.add("config.jade.plugin_gtceu.multiblock_structure", "[GTCEu] MultiBlock Structure");
        provider.add("config.jade.plugin_gtceu.parallel_info", "[GTCEu] Parallel Info");
        provider.add("config.jade.plugin_gtceu.primitive_pump", "[GTCEu] Primitive Pump Info");
        provider.add("config.jade.plugin_gtceu.transformer", "[GTCEu] Transformer Info");
        provider.add("config.jade.plugin_gtceu.stained_color", "[GTCEu] Stained Block Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer", "[GTCEu] Pattern Buffer Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer_proxy", "[GTCEu] Pattern Buffer Proxy Info");
        provider.add("config.jade.plugin_gtceu.energy_converter_provider", "[GTCEu] Energy Converter Mode");
    }

    private static void initMinimapLang(GTLangProvider provider) {
        // Tooltip/Name
        provider.add("gtceu.minimap.ore_vein.depleted", "Depleted");

        // Chat Messages
        provider.add("message.gtceu.new_veins.amount", "Prospected %d new veins!");
        provider.add("message.gtceu.new_veins.name", "Prospected %s!");

        // JourneyMap button names
        provider.add("gtceu.journeymap.options.layers", "Prospection layers");
        provider.add("gtceu.journeymap.options.layers.ore_veins", "Show Ore Veins");
        provider.add("gtceu.journeymap.options.layers.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("gtceu.journeymap.options.layers.hide_depleted", "Hide Depleted Veins");

        // Buttons
        provider.add("button.gtceu.mark_as_depleted.name", "Mark as Depleted");
        provider.add("button.gtceu.toggle_waypoint.name", "Toggle Waypoint");
        provider.add("gtceu.button.ore_veins", "Show GT Ore Veins");
        provider.add("gtceu.button.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("gtceu.button.hide_depleted", "Hide Depleted Veins");
        provider.add("gtceu.button.show_depleted", "Show Depleted Veins");
    }

    private static void initOwnershipLang(GTLangProvider provider) {
        // Team Names
        provider.add("gtceu.ownership.name.player", "Player");
        provider.add("gtceu.ownership.name.ftb", "FTB Teams");
        provider.add("gtceu.ownership.name.argonauts", "Argonauts Guild");
    }
}
