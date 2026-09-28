package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class RecipeLang {

    public static void init(GTLangProvider provider) {
        generateRecipeTypes(provider);
        generateChanceLang(provider);
        generateRecipeConditions(provider);
        generateRecipeDataKeys(provider);
        generateCapabilityLang(provider);
        generateModifierKeys(provider);
    }

    private static void generateCapabilityLang(GTLangProvider provider) {
        provider.add("recipe_capability.gtceu.eu", "GTCEu Energy");
        provider.add("recipe_capability.gtceu.fluid", "Fluid");
        provider.add("recipe_capability.gtceu.item", "Item");

        provider.add("recipe_type.gtceu.show_recipes", "Show Recipes");

        provider.add("recipe_logic.gtceu.condition_fails", "Condition Fails");
        provider.add("recipe_logic.gtceu.no_contents", "Recipe has no Contents");
        provider.add("recipe_logic.gtceu.no_capabilities", "Machine has no Capabilities");

        provider.add("recipe_logic.gtceu.setup_fail", "Fail to setup recipe: ");
        provider.add("recipe_logic.gtceu.recipe_waiting", "Recipe Waiting: ");

        provider.add("recipe_logic.gtceu.insufficient_fuel", "Insufficient Fuel");
        provider.add("recipe_logic.gtceu.insufficient_in", "Insufficient Inputs");
        provider.add("recipe_logic.gtceu.insufficient_out", "Insufficient Outputs");
        provider.add("recipe_logic.gtceu.condition_fails", "Condition Fails");
        provider.add("recipe_logic.gtceu.no_contents", "Recipe has no Contents");
        provider.add("recipe_logic.gtceu.no_capabilities", "Machine has no Capabilities");

        provider.addMultiLang("recipe_modifier.gtceu.oc", "Min: %s", "Left click to increase the OC",
                "Right click to decrease the OC", "Middle click to reset the OC",
                "Hold Shift to change by Perfect OC");
    }

    public static void generateChanceLang(RegistrateLangProvider provider) {
        provider.add("recipe_content.gtceu.chance_nc", "Not Consumed");
        provider.add("recipe_content.gtceu.chance_nc_short", "NC");
        provider.add("recipe_content.gtceu.chance_base", "Base Chance: %s%%");
        provider.add("recipe_content.gtceu.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("recipe_content.gtceu.chance_no_boost", "Chance: %s%%");
        provider.add("recipe_content.gtceu.chance_no_boost_logic", "Chance: %s%% (%s)");
        provider.add("recipe_content.gtceu.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("recipe_content.gtceu.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("recipe_content.gtceu.chance_boosted", "Chance at Tier: %s%%");
        provider.add("recipe_content.gtceu.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("recipe_content.gtceu.count_range", "%s-%sx");
        provider.add("recipe_content.gtceu.fluid_range", "%s-%smB");
        provider.add("recipe_content.gtceu.range", "%s-%s");
        provider.add("recipe_content.gtceu.times_item", "x %s");

        provider.add("recipe_content.gtceu.per_tick", "Consumed/Produced Per Tick");
        provider.add("recipe_content.gtceu.tips.per_tick_short", "/tick");
        provider.add("recipe_content.gtceu.tips.per_second_short", "/second");

        provider.add("chance_logic.gtceu.or", "OR");
        provider.add("chance_logic.gtceu.and", "AND");
        provider.add("chance_logic.gtceu.xor", "XOR");
        provider.add("chance_logic.gtceu.first", "FIRST");
        provider.add("chance_logic.gtceu.none", "NONE");
    }

    private static void generateModifierKeys(RegistrateLangProvider provider) {
        provider.add("recipe_modifier.gtceu.default_fail", "Recipe Modifier Fail");
        provider.add("recipe_modifier.gtceu.insufficient_voltage", "Voltage Tier Too Low");
        provider.add("recipe_modifier.gtceu.insufficient_eu_to_start_fusion",
                "Insufficient Energy to Initiate Fusion Reaction");
        provider.add("recipe_modifier.gtceu.coil_temperature_too_low", "Coil Temperature Too Low");
    }

    private static void generateRecipeTypes(GTLangProvider provider) {
        // Recipe Categories
        provider.add("recipe_type.gtceu.category.arc_furnace_recycling", "Plasma Scrapping");
        provider.add("recipe_type.gtceu.category.macerator_recycling", "Part Grinding");
        provider.add("recipe_type.gtceu.category.extractor_recycling", "Scrap Remelting");
        provider.add("recipe_type.gtceu.category.ore_crushing", "Ore Grinding");
        provider.add("recipe_type.gtceu.category.ore_forging", "Ore Crushing");
        provider.add("recipe_type.gtceu.category.ore_bathing", "Ore Treating");
        provider.add("recipe_type.gtceu.category.chem_dyes", "Chemical Dyeing");
        provider.add("recipe_type.gtceu.category.ingot_molding", "Metal Molding");
    }

    private static void generateRecipeConditions(GTLangProvider provider) {
        // Recipe Conditions
        provider.add("recipe_condition.gtceu.thunder", "Thunder Level: %d");
        provider.add("recipe_condition.gtceu.rain", "Rain Level: %d");
        provider.add("recipe_condition.gtceu.dimension", "Dimension: %s");
        provider.add("recipe_condition.gtceu.dimension_marker", "Dimension:");
        provider.add("recipe_condition.gtceu.biome", "Biome: %s");
        provider.add("recipe_condition.gtceu.pos_y", "Y Level: %d <= Y <= %d");

        provider.add("recipe_condition.gtceu.steam_vent", "Clean steam vent");
        provider.add("recipe_condition.gtceu.adjacent_fluid", "Fluid blocks around");
        provider.add("recipe_condition.gtceu.adjacent_block", "Blocks around");
        provider.add("recipe_condition.gtceu.eu_to_start", "EU to Start: %d%s");

        provider.add("recipe_condition.gtceu.daytime.day", "Requires day time to work");
        provider.add("recipe_condition.gtceu.daytime.night", "Requires night time to work");

        provider.add("recipe_condition.gtceu.gamestage.unlocked_stage", "Unlocked at stage: %s");
        provider.add("recipe_condition.gtceu.gamestage.locked_stage", "Locked at stage: %s");

        provider.add("recipe_condition.gtceu.quest.completed", "Requires %s completed");
        provider.add("recipe_condition.gtceu.quest.not_completed", "Requires %s not completed");

        provider.add("recipe_condition.gtceu.environmental_hazard.reverse", "§cArea must be free of %s");
        provider.add("recipe_condition.gtceu.environmental_hazard", "§cArea must have %s");

        provider.add("recipe_condition.gtceu.cleanroom", "Requires %s");
        provider.add("recipe_condition.gtceu.cleanroom.normal", "Cleanroom");
        provider.add("recipe_condition.gtceu.cleanroom.sterile", "Sterile Cleanroom");

        provider.add("recipe_condition.gtceu.research", "Requires Research");
    }

    private static void generateRecipeDataKeys(GTLangProvider provider) {
        // Recipe Data
        provider.add("recipe.gtceu.total", "Total: %s EU");
        provider.add("recipe.gtceu.max_eu", "Max. EU: %s EU");
        provider.add("recipe.gtceu.eu", "Usage: %s A @ %s");
        provider.add("recipe.gtceu.eu_inverted", "Generation: %s A @ %s");
        provider.add("recipe.gtceu.eu.total", "%s EU/t");

        provider.add("recipe.gtceu.scan_for_research", "Scan for Assembly Line");
        provider.add("recipe.gtceu.computation_per_tick", "Min. Computation: %s CWU/t");
        provider.add("recipe.gtceu.total_computation", "Computation: %s CWU");

        provider.add("recipe.gtceu.duration", "Duration: %s secs");
        provider.add("recipe.gtceu.voltage", "Usage: %s A @ %s");
        provider.add("recipe.gtceu.total_eu", "Total Usage: %s EU/t");

        provider.add("recipe.gtceu.byproduct_tier", "Byproducts from %s§r+");

        provider.add("recipe.gtceu.temperature", "Temp: %s");
        provider.add("recipe.gtceu.coil.tier", "Coil: %s");
    }
}
