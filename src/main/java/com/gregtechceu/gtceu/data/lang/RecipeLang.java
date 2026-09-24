package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class RecipeLang {

    public static void init(GTLangProvider provider) {
        generateChanceLang(provider);
        generateRecipeConditions(provider);
        generateRecipeDataKeys(provider);
        generateCapabilityLang(provider);
        generateModifierKeys(provider);
    }

    private static void generateCapabilityLang(GTLangProvider provider) {
        provider.add("gui.gtceu.recipe.show_recipes", "Show Recipes");
        provider.add("gui.gtceu.recipe.condition_fails", "Condition Fails");
        provider.add("gui.gtceu.recipe.no_contents", "Recipe has no Contents");
        provider.add("gui.gtceu.recipe.no_capabilities", "Machine has no Capabilities");

        provider.add("gui.gtceu.recipe.setup_fail", "Fail to setup recipe: ");
        provider.add("gui.gtceu.recipe.recipe_waiting", "Recipe Waiting: ");

        provider.add("gui.gtceu.recipe.insufficient_fuel", "Insufficient Fuel");
        provider.add("gui.gtceu.recipe.insufficient_in", "Insufficient Inputs");
        provider.add("gui.gtceu.recipe.insufficient_out", "Insufficient Outputs");
        provider.add("gui.gtceu.recipe.condition_fails", "Condition Fails");
        provider.add("gui.gtceu.recipe.no_contents", "Recipe has no Contents");
        provider.add("gui.gtceu.recipe.no_capabilities", "Machine has no Capabilities");

        provider.addMultiLang("gui.gtceu.recipe.oc", "Min: %s", "Left click to increase the OC",
                "Right click to decrease the OC", "Middle click to reset the OC",
                "Hold Shift to change by Perfect OC");
    }

    public static void generateChanceLang(RegistrateLangProvider provider) {
        provider.add("gui.gtceu.recipe_content.chance_nc", "Not Consumed");
        provider.add("gui.gtceu.recipe_content.chance_nc_short", "NC");
        provider.add("gui.gtceu.recipe_content.chance_base", "Base Chance: %s%%");
        provider.add("gui.gtceu.recipe_content.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("gui.gtceu.recipe_content.chance_no_boost", "Chance: %s%%");
        provider.add("gui.gtceu.recipe_content.chance_no_boost_logic", "Chance: %s%% (%s)");
        provider.add("gui.gtceu.recipe_content.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("gui.gtceu.recipe_content.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("gui.gtceu.recipe_content.chance_boosted", "Chance at Tier: %s%%");
        provider.add("gui.gtceu.recipe_content.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("gui.gtceu.recipe_content.count_range", "%s-%sx");
        provider.add("gui.gtceu.recipe_content.fluid_range", "%s-%smB");
        provider.add("gui.gtceu.recipe_content.range", "%s-%s");
        provider.add("gui.gtceu.recipe_content.times_item", "x %s");

        provider.add("gui.gtceu.recipe_content.per_tick", "Consumed/Produced Per Tick");
        provider.add("gui.gtceu.recipe_content.tips.per_tick_short", "/tick");
        provider.add("gui.gtceu.recipe_content.tips.per_second_short", "/second");
    }

    private static void generateModifierKeys(RegistrateLangProvider provider) {
        provider.add("recipe_modifier.gtceu.default_fail", "Recipe Modifier Fail");
        provider.add("recipe_modifier.gtceu.insufficient_voltage", "Voltage Tier Too Low");
        provider.add("recipe_modifier.gtceu.insufficient_eu_to_start_fusion",
                "Insufficient Energy to Initiate Fusion Reaction");
        provider.add("recipe_modifier.gtceu.coil_temperature_too_low", "Coil Temperature Too Low");
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
        provider.add("gui.gtceu.recipe.total", "Total: %s EU");
        provider.add("gui.gtceu.recipe.max_eu", "Max. EU: %s EU");
        provider.add("gui.gtceu.recipe.eu", "Usage: %s A @ %s");
        provider.add("gui.gtceu.recipe.eu_inverted", "Generation: %s A @ %s");

        provider.add("gui.gtceu.recipe.scan_for_research", "Scan for Assembly Line");
        provider.add("gui.gtceu.recipe.computation_per_tick", "Min. Computation: %s CWU/t");
        provider.add("gui.gtceu.recipe.total_computation", "Computation: %s CWU");

        provider.add("gui.gtceu.recipe.duration", "Duration: %s secs");
        provider.add("gui.gtceu.recipe.voltage", "Usage: %s A @ %s");
        provider.add("gui.gtceu.recipe.total_eu", "Total Usage: %s EU/t");

        provider.add("gui.gtceu.recipe.byproduct_tier", "Byproducts from %s§r+");

        provider.add("gui.gtceu.recipe.temperature", "Temp: %s");
        provider.add("gui.gtceu.recipe.coil.tier", "Coil: %s");
    }
}
