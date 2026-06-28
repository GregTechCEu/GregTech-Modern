package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class RecipeLogicLang {

    public static void init(RegistrateLangProvider provider) {
        initGenericLang(provider);
        initModifierLang(provider);
    }

    private static void initGenericLang(RegistrateLangProvider provider) {
        provider.add("gtceu.recipe_logic.setup_fail", "Fail to setup recipe: ");
        provider.add("gtceu.recipe_logic.recipe_waiting", "Recipe Waiting: ");

        provider.add("gtceu.recipe_logic.insufficient_fuel", "Insufficient Fuel");
        provider.add("gtceu.recipe_logic.insufficient_in", "Insufficient Inputs");
        provider.add("gtceu.recipe_logic.insufficient_out", "Insufficient Outputs");
        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.recipe_logic.no_contents", "Recipe has no Contents");
        provider.add("gtceu.recipe_logic.no_capabilities", "Machine has no Capabilities");
        provider.add("gtceu.recipe_logic.assembly_line_item_inputs_out_of_order", "Item Inputs Out of Order");
        provider.add("gtceu.recipe_logic.assembly_line_fluid_inputs_out_of_order", "Fluid Inputs Out of Order");
    }

    private static void initModifierLang(RegistrateLangProvider provider) {
        provider.add("gtceu.recipe_modifier.default_fail", "Recipe Modifier Fail");
        provider.add("gtceu.recipe_modifier.insufficient_voltage", "Voltage Tier Too Low");
        provider.add("gtceu.recipe_modifier.insufficient_eu_to_start_fusion",
                "Insufficient Energy to Initiate Fusion Reaction");
        provider.add("gtceu.recipe_modifier.coil_temperature_too_low", "Coil Temperature Too Low");
        provider.add("gtceu.recipe_modifier.missing_research_data", "Missing Research Data");
        provider.add("gtceu.recipe_modifier.missing_valid_turbine_rotor", "No Valid Turbine Rotor");
        provider.add("gtceu.recipe_modifier.insufficient_distillation_tower_height", "Requires %s Outputs, Tower has only %s Layers");
        provider.add("gtceu.recipe_modifier.steam_machine_voltage_too_high", "Steam Machines Cannot Process Recipes Above LV");
        provider.add("gtceu.recipe_modifier.missing_lubricant", "Missing Lubricant");

    }
}
