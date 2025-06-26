package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class RecipeLang {

    public static void init(GTLangProvider provider) {
        generateRecipeTypes(provider);
        generateRecipeConditions(provider);
        generateRecipeDataKeys(provider);
        generateMiscKeys(provider);
    }

    private static void generateRecipeTypes(GTLangProvider provider) {
        // RecipeTypes
        for (var recipeType : GTRegistries.RECIPE_TYPES) {
            provider.add(recipeType.getLanguageKey(), toEnglishName(recipeType.registryName.getPath()));
        }

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
        provider.add("recipe.condition.thunder.tooltip", "Thunder Level: %d");
        provider.add("recipe.condition.rain.tooltip", "Rain Level: %d");
        provider.add("recipe.condition.dimension.tooltip", "Dimension: %s");
        provider.add("recipe.condition.dimension_marker.tooltip", "Dimension:");
        provider.add("recipe.condition.biome.tooltip", "Biome: %s");
        provider.add("recipe.condition.pos_y.tooltip", "Y Level: %d <= Y <= %d");

        provider.add("recipe.condition.steam_vent.tooltip", "Clean steam vent");
        provider.add("recipe.condition.rock_breaker.tooltip", "Fluid blocks around");
        provider.add("recipe.condition.adjacent_block.tooltip", "Blocks around");
        provider.add("recipe.condition.eu_to_start.tooltip", "EU to Start: %d%s");

        provider.add("recipe.condition.daytime.day.tooltip", "Requires day time to work");
        provider.add("recipe.condition.daytime.night.tooltip", "Requires night time to work");

        provider.add("recipe.condition.gamestage.unlocked_stage", "Unlocked at stage: %s");
        provider.add("recipe.condition.gamestage.locked_stage", "Locked at stage: %s");

        provider.add("recipe.condition.quest.completed.tooltip", "Requires %s completed");
        provider.add("recipe.condition.quest.not_completed.tooltip", "Requires %s not completed");
    }

    private static void generateRecipeDataKeys(GTLangProvider provider) {
        // Recipe Data
        provider.add("gtceu.recipe.total", "Total: %s EU");
        provider.add("gtceu.recipe.max_eu", "Max. EU: %s EU");
        provider.add("gtceu.recipe.eu", "Usage: %s EU/t");
        provider.add("gtceu.recipe.eu_inverted", "Generation: %s EU/t");

        provider.add("gtceu.recipe.scan_for_research", "Scan for Assembly Line");
        provider.add("gtceu.recipe.computation_per_tick", "Min. Computation: %s CWU/t");
        provider.add("gtceu.recipe.total_computation", "Computation: %s CWU");

        provider.add("gtceu.recipe.duration", "Duration: %s secs");
        provider.add("gtceu.recipe.amperage", "Amperage: %s");

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

        provider.add("gtceu.recipe.temperature", "Temp: %sK");
        provider.add("gtceu.recipe.coil.tier", "Coil: %s");
    }

    private static void generateMiscKeys(GTLangProvider provider) {
        // IO
        provider.add("gtceu.io.import", "Import");
        provider.add("gtceu.io.export", "Export");
        provider.add("gtceu.io.both", "Both");
        provider.add("gtceu.io.none", "None");
    }
}
