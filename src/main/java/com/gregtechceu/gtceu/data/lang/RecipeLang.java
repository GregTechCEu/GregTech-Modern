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

        provider.add("gtceu.recipe_type.show_recipes", "Show Recipes");

        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.recipe_logic.no_contents", "Recipe has no Contents");
        provider.add("gtceu.recipe_logic.no_capabilities", "Machine has no Capabilities");

        provider.add("gtceu.recipe_logic.setup_fail", "Fail to setup recipe: ");
        provider.add("gtceu.recipe_logic.recipe_waiting", "Recipe Waiting: ");

        provider.add("gtceu.recipe_logic.insufficient_fuel", "Insufficient Fuel");
        provider.add("gtceu.recipe_logic.insufficient_in", "Insufficient Inputs");
        provider.add("gtceu.recipe_logic.insufficient_out", "Insufficient Outputs");
        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.recipe_logic.no_contents", "Recipe has no Contents");
        provider.add("gtceu.recipe_logic.no_capabilities", "Machine has no Capabilities");

        provider.addMultiLang("gtceu.oc.tooltip", "Min: %s", "Left click to increase the OC",
                "Right click to decrease the OC", "Middle click to reset the OC",
                "Hold Shift to change by Perfect OC");
    }

    public static void generateChanceLang(RegistrateLangProvider provider) {
        provider.add("recipe.gtceu.content.chance_nc", "Not Consumed");
        provider.add("recipe.gtceu.content.chance_nc_short", "NC");
        provider.add("recipe.gtceu.content.chance_base", "Base Chance: %s%%");
        provider.add("recipe.gtceu.content.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("recipe.gtceu.content.chance_no_boost", "Chance: %s%%");
        provider.add("recipe.gtceu.content.chance_no_boost_logic", "Chance: %s%% (%s)");
        provider.add("recipe.gtceu.content.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("recipe.gtceu.content.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("recipe.gtceu.content.chance_boosted", "Chance at Tier: %s%%");
        provider.add("recipe.gtceu.content.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("recipe.gtceu.content.count_range", "%s-%sx");
        provider.add("recipe.gtceu.content.fluid_range", "%s-%smB");
        provider.add("recipe.gtceu.content.range", "%s-%s");
        provider.add("recipe.gtceu.content.times_item", "x %s");

        provider.add("recipe.gtceu.content.per_tick", "Consumed/Produced Per Tick");
        provider.add("recipe.gtceu.content.tips.per_tick_short", "/tick");
        provider.add("recipe.gtceu.content.tips.per_second_short", "/second");

        provider.add("chance_logic.gtceu.or", "OR");
        provider.add("chance_logic.gtceu.and", "AND");
        provider.add("chance_logic.gtceu.xor", "XOR");
        provider.add("chance_logic.gtceu.first", "FIRST");
        provider.add("chance_logic.gtceu.none", "NONE");
    }

    private static void generateModifierKeys(RegistrateLangProvider provider) {
        provider.add("recipe.gtceu.modifier.default_fail", "Recipe Modifier Fail");
        provider.add("recipe.gtceu.modifier.insufficient_voltage", "Voltage Tier Too Low");
        provider.add("recipe.gtceu.modifier.insufficient_eu_to_start_fusion",
                "Insufficient Energy to Initiate Fusion Reaction");
        provider.add("recipe.gtceu.modifier.coil_temperature_too_low", "Coil Temperature Too Low");
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
        provider.add("recipe.gtceu.condition.thunder.tooltip", "Thunder Level: %d");
        provider.add("recipe.gtceu.condition.rain.tooltip", "Rain Level: %d");
        provider.add("recipe.gtceu.condition.dimension.tooltip", "Dimension: %s");
        provider.add("recipe.gtceu.condition.dimension_marker.tooltip", "Dimension:");
        provider.add("recipe.gtceu.condition.biome.tooltip", "Biome: %s");
        provider.add("recipe.gtceu.condition.pos_y.tooltip", "Y Level: %d <= Y <= %d");

        provider.add("recipe.gtceu.condition.steam_vent.tooltip", "Clean steam vent");
        provider.add("recipe.gtceu.condition.adjacent_fluid.tooltip", "Fluid blocks around");
        provider.add("recipe.gtceu.condition.adjacent_block.tooltip", "Blocks around");
        provider.add("recipe.gtceu.condition.eu_to_start.tooltip", "EU to Start: %d%s");

        provider.add("recipe.gtceu.condition.daytime.day.tooltip", "Requires day time to work");
        provider.add("recipe.gtceu.condition.daytime.night.tooltip", "Requires night time to work");

        provider.add("recipe.gtceu.condition.gamestage.unlocked_stage", "Unlocked at stage: %s");
        provider.add("recipe.gtceu.condition.gamestage.locked_stage", "Locked at stage: %s");

        provider.add("recipe.gtceu.condition.quest.completed.tooltip", "Requires %s completed");
        provider.add("recipe.gtceu.condition.quest.not_completed.tooltip", "Requires %s not completed");
    }

    private static void generateRecipeDataKeys(GTLangProvider provider) {
        // Recipe Data
        provider.add("gtceu.recipe.total", "Total: %s EU");
        provider.add("gtceu.recipe.max_eu", "Max. EU: %s EU");
        provider.add("gtceu.recipe.eu", "Usage: %s A @ %s");
        provider.add("gtceu.recipe.eu_inverted", "Generation: %s A @ %s");
        provider.add("gtceu.recipe.eu.total", "%s EU/t");

        provider.add("gtceu.recipe.scan_for_research", "Scan for Assembly Line");
        provider.add("gtceu.recipe.computation_per_tick", "Min. Computation: %s CWU/t");
        provider.add("gtceu.recipe.total_computation", "Computation: %s CWU");

        provider.add("gtceu.recipe.byproduct_tier", "Byproducts from %s§r+");

        provider.add("gtceu.recipe.duration", "Duration: %s secs");
        provider.add("gtceu.recipe.voltage", "Usage: %s A @ %s");
        provider.add("gtceu.recipe.total_eu", "Total Usage: %s EU/t");

        provider.add("gtceu.recipe.not_consumed", "Does not get consumed in the process");
        provider.add("gtceu.recipe.chance", "Chance: %s +%s/tier");

        provider.add("gtceu.recipe.explosive", "Explosive: %s");
        // TODO make the ones below this comment recipe condition keys
        provider.add("gtceu.recipe.eu_to_start", "EU To Start: %sEU%s");
        provider.add("gtceu.recipe.dimensions", "Dimensions: %s");

        provider.add("gtceu.recipe.environmental_hazard.reverse", "§cArea must be free of %s");
        provider.add("gtceu.recipe.environmental_hazard", "§cArea must have %s");

        provider.add("gtceu.recipe.cleanroom", "Requires %s");
        provider.add("gtceu.recipe.cleanroom.display_name", "Cleanroom");
        provider.add("gtceu.recipe.cleanroom_sterile.display_name", "Sterile Cleanroom");

        provider.add("gtceu.recipe.research", "Requires Research");

        provider.add("gtceu.recipe.temperature", "Temp: %s");
        provider.add("gtceu.recipe.coil.tier", "Coil: %s");
    }
}
