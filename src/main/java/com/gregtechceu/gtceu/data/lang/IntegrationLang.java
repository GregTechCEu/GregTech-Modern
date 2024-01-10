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
    }

    /** JEI, REI, EMI */
    private static void initRecipeViewerLang(RegistrateLangProvider provider) {
        provider.add("gtceu.jei.multiblock_info", "Multiblock Info");
        provider.add("gtceu.jei.ore_processing_diagram", "Ore Processing Diagram");
        provider.add("gtceu.jei.ore_vein_diagram", "Ore Vein Diagram");
        provider.add("gtceu.jei.bedrock_fluid_diagram", "Bedrock Fluid Diagram");
        provider.add("gtceu.jei.ore_vein_diagram.chance", "§eChance: %s§r");
        provider.add("gtceu.jei.ore_vein_diagram.spawn_range", "Spawn Range:");
        provider.add("gtceu.jei.ore_vein_diagram.weight", "Weight: %s");
        provider.add("gtceu.jei.ore_vein_diagram.dimensions", "Dimensions:");
        GTOres.init();
        for (GTOreDefinition oreDefinition:GTRegistries.ORE_VEINS){
            String name = GTRegistries.ORE_VEINS.getKey(oreDefinition).getPath();
            provider.add("gtceu.jei.ore_vein." + name, RegistrateLangProvider.toEnglishName(name));
        }
        GTBedrockFluids.init();
        for (BedrockFluidDefinition fluid:GTRegistries.BEDROCK_FLUID_DEFINITIONS){
            String name = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid).getPath();
            provider.add("gtceu.jei.bedrock_fluid." + name, RegistrateLangProvider.toEnglishName(name));
        }
    }

    /** Jade, TheOneProbe, WTHIT */
    private static void initWailaLikeLang(RegistrateLangProvider provider) {
        provider.add("gtceu.top.working_disabled", "Working Disabled");
        provider.add("gtceu.top.energy_consumption", "Using");
        provider.add("gtceu.top.energy_production", "Producing");
        provider.add("gtceu.top.transform_up", "§cStep Up§r");
        provider.add("gtceu.top.transform_down", "§aStep Down§r");
        provider.add("gtceu.top.transform_input", "§6Input:§r");
        provider.add("gtceu.top.transform_output", "§9Output:§r");
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
        provider.add("gtceu.top.primitive_pump_production", "Production:");
        provider.add("gtceu.top.filter.label", "Filter:");
        provider.add("gtceu.top.link_cover.color", "Color:");
        provider.add("gtceu.top.mode.export", "Exporting");
        provider.add("gtceu.top.mode.import", "Importing");
        provider.add("gtceu.top.unit.items", "Items");
        provider.add("gtceu.top.unit.fluid_milibuckets", "L");
        provider.add("gtceu.top.unit.fluid_buckets", "kL");
    }
}
