package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.*;

public final class GTRecipeConditions {

    private GTRecipeConditions() {}

    // spotless:off
    public static final RecipeConditionType<BiomeCondition> BIOME = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("biome"), new RecipeConditionType<>(BiomeCondition::new, BiomeCondition.CODEC));
    public static final RecipeConditionType<DimensionCondition> DIMENSION = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("dimension"), new RecipeConditionType<>(DimensionCondition::new, DimensionCondition.CODEC));
    public static final RecipeConditionType<PositionYCondition> POSITION_Y = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("pos_y"), new RecipeConditionType<>(PositionYCondition::new, PositionYCondition.CODEC));
    public static final RecipeConditionType<RainingCondition> RAINING = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("rain"), new RecipeConditionType<>(RainingCondition::new, RainingCondition.CODEC));
    public static final RecipeConditionType<RockBreakerCondition> ROCK_BREAKER = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("rock_breaker"), new RecipeConditionType<>(RockBreakerCondition::new, RockBreakerCondition.CODEC));
    public static final RecipeConditionType<AdjacentBlockCondition> ADJACENT_BLOCK = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("adjacent_block"), new RecipeConditionType<>(AdjacentBlockCondition::new, AdjacentBlockCondition.CODEC));
    public static final RecipeConditionType<ThunderCondition> THUNDER = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("thunder"), new RecipeConditionType<>(ThunderCondition::new, ThunderCondition.CODEC));
    public static final RecipeConditionType<VentCondition> VENT = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("steam_vent"), new RecipeConditionType<>(VentCondition::new, VentCondition.CODEC));
    public static final RecipeConditionType<CleanroomCondition> CLEANROOM = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("cleanroom"), new RecipeConditionType<>(CleanroomCondition::new, CleanroomCondition.CODEC));
    public static final RecipeConditionType<EUToStartCondition> EU_TO_START = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("eu_to_start"), new RecipeConditionType<>(EUToStartCondition::new, EUToStartCondition.CODEC));
    public static final RecipeConditionType<ResearchCondition> RESEARCH = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("research"), new RecipeConditionType<>(ResearchCondition::new, ResearchCondition.CODEC));
    public static final RecipeConditionType<EnvironmentalHazardCondition> ENVIRONMENTAL_HAZARD = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("environmental_hazard"), new RecipeConditionType<>(EnvironmentalHazardCondition::new, EnvironmentalHazardCondition.CODEC));
    public static final RecipeConditionType<DaytimeCondition> DAYTIME = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
            GTCEu.id("daytime"), new RecipeConditionType<>(DaytimeCondition::new, DaytimeCondition.CODEC));
    public static RecipeConditionType<FTBQuestCondition> FTB_QUEST;
    public static RecipeConditionType<GameStageCondition> GAMESTAGE;
    public static RecipeConditionType<HeraclesQuestCondition> HERACLES_QUEST;

    public static void init() {
        if (GTCEu.Mods.isFTBQuestsLoaded()) {
            FTB_QUEST = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
                    GTCEu.id("ftb_quest"), new RecipeConditionType<>(FTBQuestCondition::new, FTBQuestCondition.CODEC));
        }
        if (GTCEu.Mods.isGameStagesLoaded()) {
            GAMESTAGE = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
                    GTCEu.id("game_stage"), new RecipeConditionType<>(GameStageCondition::new, GameStageCondition.CODEC));
        }
        if (GTCEu.Mods.isHeraclesLoaded()) {
            HERACLES_QUEST = GTRegistries.register(GTRegistries.RECIPE_CONDITIONS,
                    GTCEu.id("heracles_quest"), new RecipeConditionType<>(HeraclesQuestCondition::new, HeraclesQuestCondition.CODEC));
        }
    }
    // spotless:on
}
