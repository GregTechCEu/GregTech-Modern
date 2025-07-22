package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;
import com.gregtechceu.gtceu.common.data.GTOres;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class IntegrationLang {

    public static void init(RegistrateLangProvider provider) {
        initRecipeViewerLang(provider);
        initWailaLikeLang(provider);
        initMinimapLang(provider);
        initOwnershipLang(provider);
    }

    /** JEI, REI, EMI */
    private static void initRecipeViewerLang(RegistrateLangProvider provider) {
        provider.add("gtceu.jei.multiblock_info", "Multiblock Info");
        provider.add("gtceu.jei.ore_processing_diagram", "Ore Processing Diagram");
        provider.add("gtceu.jei.ore_vein_diagram", "Ore Vein Diagram");
        provider.add("gtceu.jei.programmed_circuit", "Programmed Circuit Page");
        provider.add("gtceu.jei.bedrock_fluid_diagram", "Bedrock Fluid Diagram");
        provider.add("gtceu.jei.bedrock_ore_diagram", "Bedrock Ore Diagram");
        provider.add("gtceu.jei.ore_vein_diagram.chance", "§eChance: %s§r");
        provider.add("gtceu.jei.ore_vein_diagram.spawn_range", "Spawn Range:");
        provider.add("gtceu.jei.ore_vein_diagram.weight", "Weight: %s");
        provider.add("gtceu.jei.ore_vein_diagram.dimensions", "Dimensions:");
        GTRegistries.ORE_VEINS.unfreeze();
        GTOres.init();
        for (GTOreDefinition oreDefinition : GTRegistries.ORE_VEINS) {
            String name = GTRegistries.ORE_VEINS.getKey(oreDefinition).getPath();
            provider.add("gtceu.jei.ore_vein." + name, RegistrateLangProvider.toEnglishName(name));
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        GTBedrockFluids.init();
        for (BedrockFluidDefinition fluid : GTRegistries.BEDROCK_FLUID_DEFINITIONS) {
            String name = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid).getPath();
            provider.add("gtceu.jei.bedrock_fluid." + name, RegistrateLangProvider.toEnglishName(name));
        }

        provider.add("gtceu.rei.group.potion_fluids", "Potion Fluids");
    }

    /** Jade, TheOneProbe, WTHIT */
    private static void initWailaLikeLang(RegistrateLangProvider provider) {
        provider.add("gtceu.top.working_disabled", "Working Disabled");
        provider.add("gtceu.top.energy_consumption", "Using");
        provider.add("gtceu.top.energy_production", "Producing");
        provider.add("gtceu.top.transform_up", "§cStep Up§r %s");
        provider.add("gtceu.top.transform_down", "§aStep Down§r %s");
        provider.add("gtceu.top.transform_input", "§6Input:§r %s");
        provider.add("gtceu.top.transform_output", "§9Output:§r %s");
        provider.add("gtceu.top.convert_eu", "Converting §eEU§r -> §cFE§r");
        provider.add("gtceu.top.convert_fe", "Converting §cFE§r -> §eEU§r");
        provider.add("gtceu.top.fuel_min_consume", "Needs");
        provider.add("gtceu.top.fuel_none", "No fuel");
        provider.add("gtceu.top.invalid_structure", "Structure Incomplete");
        provider.add("gtceu.top.valid_structure", "Structure Formed");
        provider.add("gtceu.top.obstructed_structure", "Structure Obstructed");
        provider.add("gtceu.top.maintenance_fixed", "Maintenance Fine");
        provider.add("gtceu.top.maintenance_broken", "Needs Maintenance");
        provider.add("gtceu.top.maintenance.wrench", "Pipe is loose");
        provider.add("gtceu.top.maintenance.screwdriver", "Screws are loose");
        provider.add("gtceu.top.maintenance.soft_mallet", "Something is stuck");
        provider.add("gtceu.top.maintenance.hard_hammer", "Plating is dented");
        provider.add("gtceu.top.maintenance.wire_cutter", "Wires burned out");
        provider.add("gtceu.top.maintenance.crowbar", "That doesn't belong there");
        provider.add("gtceu.top.primitive_pump_production", "Production: %s mB/s");
        provider.add("gtceu.top.filter.label", "Filter:");
        provider.add("gtceu.top.link_cover.color", "Color:");
        provider.add("gtceu.top.mode.export", "Exporting");
        provider.add("gtceu.top.mode.import", "Importing");
        provider.add("gtceu.top.unit.items", "Items");
        provider.add("gtceu.top.unit.fluid_milibuckets", "L");
        provider.add("gtceu.top.unit.fluid_buckets", "kL");
        provider.add("gtceu.top.recipe_output", "Recipe Outputs:");
        provider.add("gtceu.top.item_auto_output", "Item Output: %s");
        provider.add("gtceu.top.fluid_auto_output", "Fluid Output: %s");
        provider.add("gtceu.top.auto_output", "Auto Output");
        provider.add("gtceu.top.allow_output_input", "Allow Input");
        provider.add("gtceu.top.cable_voltage", "Voltage: ");
        provider.add("gtceu.top.cable_amperage", "Amperage: ");
        provider.add("gtceu.top.exhaust_vent_direction", "Exhaust Vent: %s");
        provider.add("gtceu.top.exhaust_vent_blocked", "Blocked");
        provider.add("gtceu.top.machine_mode", "Machine Mode: ");
        provider.add("gtceu.top.stained", "Colored: %s");
        provider.add("gtceu.top.buffer_not_bound", "Buffer Not Currently Bound");
        provider.add("gtceu.top.buffer_bound_pos", "Bound To - X: %s, Y: %s, Z: %s");
        provider.add("gtceu.top.proxies_bound", "Buffer Proxies Bound: %s");

        provider.add("gtceu.jade.energy_stored", "%d / %d EU");
        provider.add("gtceu.jade.progress_computation", "%s / %s CWU");
        provider.add("gtceu.jade.progress_sec", "%s / %s s");
        provider.add("gtceu.jade.progress_tick", "%s / %s t");
        provider.add("gtceu.jade.cleaned_this_second", "Cleaned hazard: %s/s");
        provider.add("gtceu.jade.fluid_use", "%s mB/t");
        provider.add("gtceu.jade.amperage_use", "%s A");
        provider.add("gtceu.jade.at", " @ ");

        provider.add("gtceu.top.energy_stored", " / %d EU");
        provider.add("gtceu.top.progress_computation", " / %s CWU");
        provider.add("gtceu.top.progress_sec", " / %s s");
        provider.add("gtceu.top.progress_tick", " / %s t");
    }

    private static void initMinimapLang(RegistrateLangProvider provider) {
        provider.add("gtceu.minimap.ore_vein.depleted", "Depleted");

        provider.add("message.gtceu.new_veins.amount", "Prospected %d new veins!");
        provider.add("message.gtceu.new_veins.name", "Prospected %s!");
        provider.add("button.gtceu.mark_as_depleted.name", "Mark as Depleted");
        provider.add("button.gtceu.toggle_waypoint.name", "Toggle Waypoint");

        provider.add("gtceu.journeymap.options.layers", "Prospection layers");
        provider.add("gtceu.journeymap.options.layers.ore_veins", "Show Ore Veins");
        provider.add("gtceu.journeymap.options.layers.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("gtceu.journeymap.options.layers.hide_depleted", "Hide Depleted Veins");
    }

    private static void initOwnershipLang(RegistrateLangProvider provider) {
        provider.add("gtceu.ownership.name.player", "Player");
        provider.add("gtceu.ownership.name.ftb", "FTB Teams");
        provider.add("gtceu.ownership.name.argonauts", "Argonauts Guild");
    }
}
