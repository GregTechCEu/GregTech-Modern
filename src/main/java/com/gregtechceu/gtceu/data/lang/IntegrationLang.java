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
        initJadeLang(provider);
        initMinimapLang(provider);
        initOwnershipLang(provider);

        // Curios
        provider.add("curios.identifier.gtceu_magnet", "GTCEu Magnet");
    }

    /** JEI, REI, EMI */
    private static void initRecipeViewerLang(GTLangProvider provider) {
        provider.add("recipeviewer.gtceu.category.multiblock_info", "Multiblock Info");
        provider.add("recipeviewer.gtceu.category.programmed_circuit", "Programmed Circuit Page");

        provider.add("recipeviewer.gtceu.category.ore_processing", "Ore Processing Diagram");
        provider.add("recipeviewer.gtceu.category.ore_veins", "Ore Veins");
        provider.add("recipeviewer.gtceu.category.bedrock_fluids", "Bedrock Fluids");
        provider.add("recipeviewer.gtceu.category.bedrock_ores", "Bedrock Ores");

        // Ore Veins

        provider.add("recipeviewer.gtceu.ore_vein_diagram.chance", "§eChance: %s§r");
        provider.add("recipeviewer.gtceu.ore_vein_diagram.spawn_range", "Spawn Range:");
        provider.add("recipeviewer.gtceu.ore_vein_diagram.weight", "Weight: %s");
        provider.add("recipeviewer.gtceu.ore_vein_diagram.dimensions", "Dimensions:");

        // Fluid vein

        provider.add("recipeviewer.gtceu.fluid.vein_weight", "Vein Weight: %d");
        provider.add("recipeviewer.gtceu.fluid.min_yield", "Minimum Yield: %d");
        provider.add("recipeviewer.gtceu.fluid.max_yield", "Maximum Yield: %d");
        provider.add("recipeviewer.gtceu.fluid.depletion_chance", "Depletion Chance: %d%%");
        provider.add("recipeviewer.gtceu.fluid.depletion_amount", "Depletion Amount: %d");
        provider.add("recipeviewer.gtceu.fluid.depleted_rate", "Depleted Yield: %d");

        provider.add("recipeviewer.gtceu.fluid.dimension", "Dimensions:");

        provider.add("recipeviewer.gtceu.fluid.min_hover",
                "The minimum yield that any fluid vein of this fluid can have");
        provider.add("recipeviewer.gtceu.fluid.max_hover",
                "The maximum yield that any fluid vein of this fluid can have");
        provider.add("recipeviewer.gtceu.fluid.dep_chance_hover",
                "The percentage chance for the vein to be depleted upon harvest");
        provider.add("recipeviewer.gtceu.fluid.dep_amount_hover", "The amount the vein will be depleted by");
        provider.add("recipeviewer.gtceu.fluid.dep_yield_hover",
                "The maximum yield of the vein when it is fully depleted");

        // Vein Names

        // TODO This should be replaced with something else
        GTRegistries.ORE_VEINS.unfreeze();
        GTOres.init();
        for (GTOreDefinition oreDefinition : GTRegistries.ORE_VEINS) {
            String name = GTRegistries.ORE_VEINS.getKey(oreDefinition).getPath();
            provider.add("recipeviewer.gtceu.ore_vein." + name, FormattingUtil.toEnglishName(name));
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        GTBedrockFluids.init();
        for (BedrockFluidDefinition fluid : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
            String name = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid).getPath();
            provider.add("recipeviewer.gtceu.bedrock_fluid." + name, FormattingUtil.toEnglishName(name));
        }
    }

    /** Jade */
    private static void initJadeLang(GTLangProvider provider) {
        provider.add("integration.gtceu.jade.energy_consumption", "Using");
        provider.add("integration.gtceu.jade.energy_production", "Producing");

        // Transformers
        provider.add("integration.gtceu.jade.transform_up", "§cStep Up§r %s");
        provider.add("integration.gtceu.jade.transform_down", "§aStep Down§r %s");
        provider.add("integration.gtceu.jade.transform_input", "§6Input:§r %s");
        provider.add("integration.gtceu.jade.transform_output", "§9Output:§r %s");
        provider.add("integration.gtceu.jade.convert_eu", "Converting §eEU§r -> §cFE§r");
        provider.add("integration.gtceu.jade.convert_fe", "Converting §cFE§r -> §eEU§r");

        // Generators
        provider.add("integration.gtceu.jade.fuel_min_consume", "Needs");
        provider.add("integration.gtceu.jade.fuel_none", "No fuel");

        // Multiblock Structures
        provider.add("integration.gtceu.jade.invalid_structure", "Structure Incomplete");
        provider.add("integration.gtceu.jade.valid_structure", "Structure Formed");
        provider.add("integration.gtceu.jade.obstructed_structure", "Structure Obstructed");

        // Maintenance
        provider.add("integration.gtceu.jade.maintenance_fixed", "Maintenance Fine");
        provider.add("integration.gtceu.jade.maintenance_broken", "Needs Maintenance");
        provider.add("integration.gtceu.jade.maintenance.wrench", "Pipe is loose");
        provider.add("integration.gtceu.jade.maintenance.screwdriver", "Screws are loose");
        provider.add("integration.gtceu.jade.maintenance.soft_mallet", "Something is stuck");
        provider.add("integration.gtceu.jade.maintenance.hard_hammer", "Plating is dented");
        provider.add("integration.gtceu.jade.maintenance.wire_cutter", "Wires burned out");
        provider.add("integration.gtceu.jade.maintenance.crowbar", "That doesn't belong there");

        // Steam Venting
        provider.add("integration.gtceu.jade.exhaust_vent_direction", "Exhaust Vent: %s");
        provider.add("integration.gtceu.jade.exhaust_vent_blocked", "Blocked");

        // Primitive Pump
        provider.add("integration.gtceu.jade.primitive_pump_production", "Production: %s mB/s");

        // Recipe Provider
        provider.add("integration.gtceu.jade.recipe_output", "Recipe Outputs:");
        provider.add("integration.gtceu.jade.item_auto_output", "Item Output: %s");
        provider.add("integration.gtceu.jade.fluid_auto_output", "Fluid Output: %s");

        // IO Provider
        provider.add("integration.gtceu.jade.auto_output", "Auto Output");
        provider.add("integration.gtceu.jade.allow_output_input", "Allow Input");

        // Cable Provider
        provider.add("integration.gtceu.jade.cable.voltage", "Voltage: ");
        provider.add("integration.gtceu.jade.cable.amperage", "Amperage: ");
        provider.add("integration.gtceu.jade.cable.overloaded", "Cable Overloaded!");
        // Machine Mode Provider
        provider.add("integration.gtceu.jade.machine_mode", "Machine Mode: ");

        // Color Provider
        provider.add("integration.gtceu.jade.stained", "Colored: %s");

        // Proxy/Buffers
        provider.add("integration.gtceu.jade.pattern_buffer.not_bound", "Buffer Not Currently Bound");
        provider.add("integration.gtceu.jade.pattern_buffer.bound_to_pos", "Bound To - X: %s, Y: %s, Z: %s");
        provider.add("integration.gtceu.jade.pattern_buffer.proxies_bound", "Buffer Proxies Bound: %s");

        provider.add("integration.gtceu.jade.energy_stored", "%d / %d EU");
        provider.add("integration.gtceu.jade.progress_computation", "%s / %s CWU");
        provider.add("integration.gtceu.jade.progress_sec", "%s / %s s");
        provider.add("integration.gtceu.jade.progress_tick", "%s / %s t");

        provider.add("integration.gtceu.jade.cleaned_this_second", "Cleaned hazard: %s/s");
        provider.add("integration.gtceu.jade.fluid_use", "%s mB/t");
        provider.add("integration.gtceu.jade.amperage_use", "%s A");
        provider.add("integration.gtceu.jade.remaining_charge_time", "Until charged: %s");
        provider.add("integration.gtceu.jade.remaining_discharge_time", "Until empty: %s");
        provider.add("integration.gtceu.jade.changes_eu_sec", "%s EU/s");
        provider.add("integration.gtceu.jade.seconds", "%s seconds");
        provider.add("integration.gtceu.jade.minutes", "%s minutes");
        provider.add("integration.gtceu.jade.hours", "%s hours");
        provider.add("integration.gtceu.jade.days", "%s days");
        provider.add("integration.gtceu.jade.years", "%s years");

        provider.add("integration.gtceu.jade.ldp_endpoint.is_formed", "Pipeline Formed");
        provider.add("integration.gtceu.jade.ldp_endpoint.not_formed", "Pipeline Incomplete");
        provider.add("integration.gtceu.jade.ldp_endpoint.io_type", "IO Type: %s");
        provider.add("integration.gtceu.jade.ldp_endpoint.output_direction", "Output Direction: %s");

        provider.add("integration.gtceu.jade.generator.output_too_small", "Energy Output too small!");

        // Plugin Names
        provider.add("config.jade.plugin_gtceu.controllable_provider", "[GTCEu] Controllable");
        provider.add("config.jade.plugin_gtceu.workable_provider", "[GTCEu] Workable");
        provider.add("config.jade.plugin_gtceu.battery_info", "[GTCEu] Battery info");
        provider.add("config.jade.plugin_gtceu.electric_container_provider", "[GTCEu] Electric Container");
        provider.add("config.jade.plugin_recipe_logic.gtceu_provider", "[GTCEu] Recipe Logic");
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
        provider.add("config.jade.plugin_gtceu.data_bank", "[GTCEu] Data Bank Info");
        provider.add("config.jade.plugin_gtceu.transformer", "[GTCEu] Transformer Info");
        provider.add("config.jade.plugin_gtceu.stained_color", "[GTCEu] Stained Block Info");
        provider.add("config.jade.plugin_gtceu.me_grid_connected", "[GTCEu] ME Grid Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer", "[GTCEu] Pattern Buffer Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer_proxy", "[GTCEu] Pattern Buffer Proxy Info");
        provider.add("config.jade.plugin_gtceu.energy_converter_provider", "[GTCEu] Energy Converter Mode");
        provider.add("config.jade.plugin_gtceu.ldp_endpoint", "[GTCEu] Long Distance Pipeline Endpoint Info");
    }

    private static void initMinimapLang(GTLangProvider provider) {
        // Tooltip/Name
        provider.add("minimap.gtceu.ore_vein.depleted", "Depleted Vein");

        // Chat Messages
        provider.add("map.gtceu.new_veins.amount", "Prospected %d new veins!");
        provider.add("map.gtceu.new_veins.name", "Prospected %s!");

        // JourneyMap button names
        provider.add("integration.gtceu.journeymap.options.layers", "Prospection layers");
        provider.add("integration.gtceu.journeymap.options.layers.ore_veins", "Show Ore Veins");
        provider.add("integration.gtceu.journeymap.options.layers.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("integration.gtceu.journeymap.options.layers.hide_depleted", "Hide Depleted Veins");

        provider.add("map.gtceu.button.mark_as_depleted.name", "Mark as Depleted");
        provider.add("map.gtceu.button.toggle_waypoint.name", "Toggle Waypoint");
        provider.add("map.gtceu.button.ore_veins", "Show GT Ore Veins");
        provider.add("map.gtceu.button.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("map.gtceu.button.hide_depleted", "Hide Depleted Veins");
        provider.add("map.gtceu.button.show_depleted", "Show Depleted Veins");
    }

    private static void initOwnershipLang(GTLangProvider provider) {
        // Team Names
        provider.add("integration.gtceu.ownership.name.player", "Player");
        provider.add("integration.gtceu.ownership.name.ftb", "FTB Teams");
        provider.add("integration.gtceu.ownership.name.argonauts", "Argonauts Guild");
    }
}
