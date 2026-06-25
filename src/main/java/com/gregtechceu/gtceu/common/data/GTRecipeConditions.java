package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.*;

import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class GTRecipeConditions {
    
    private GTRecipeConditions() {}

    private static final DeferredRegister<RecipeConditionType<?>> RECIPE_CONDITION = DeferredRegister.create(GTRegistries.Keys.RECIPE_CONDITION, GTCEu.MOD_ID);
    
    // spotless:off
    public static final RegistryObject<RecipeConditionType<BiomeCondition>> BIOME = RECIPE_CONDITION.register("biome", () ->  new RecipeConditionType<>(BiomeCondition::new, BiomeCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<BiomeTagCondition>> BIOME_TAG = RECIPE_CONDITION.register("biome_tag", () -> new RecipeConditionType<>( BiomeTagCondition::new, BiomeTagCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<DimensionCondition>> DIMENSION = RECIPE_CONDITION.register("dimension", () -> new RecipeConditionType<>( DimensionCondition::new, DimensionCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<PositionYCondition>> POSITION_Y = RECIPE_CONDITION.register("pos_y", () -> new RecipeConditionType<>( PositionYCondition::new, PositionYCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<RainingCondition>> RAINING = RECIPE_CONDITION.register("rain", () -> new RecipeConditionType<>( RainingCondition::new, RainingCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<AdjacentFluidCondition>> ADJACENT_FLUID = RECIPE_CONDITION.register("adjacent_fluid", () -> new RecipeConditionType<>( AdjacentFluidCondition::new, AdjacentFluidCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<AdjacentBlockCondition>> ADJACENT_BLOCK = RECIPE_CONDITION.register("adjacent_block", () -> new RecipeConditionType<>( AdjacentBlockCondition::new, AdjacentBlockCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<ThunderCondition>> THUNDER = RECIPE_CONDITION.register("thunder", () -> new RecipeConditionType<>( ThunderCondition::new, ThunderCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<VentCondition>> VENT = RECIPE_CONDITION.register("steam_vent", () -> new RecipeConditionType<>( VentCondition::new, VentCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<CleanroomCondition>> CLEANROOM = RECIPE_CONDITION.register("cleanroom", () -> new RecipeConditionType<>( CleanroomCondition::new, CleanroomCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<EUToStartCondition>> EU_TO_START = RECIPE_CONDITION.register("eu_to_start", () -> new RecipeConditionType<>( EUToStartCondition::new, EUToStartCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<ResearchCondition>> RESEARCH = RECIPE_CONDITION.register("research", () -> new RecipeConditionType<>( ResearchCondition::new, ResearchCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<EnvironmentalHazardCondition>> ENVIRONMENTAL_HAZARD = RECIPE_CONDITION.register("environmental_hazard", 
            () -> new RecipeConditionType<>( EnvironmentalHazardCondition::new, EnvironmentalHazardCondition.CODEC));
    public static final RegistryObject<RecipeConditionType<DaytimeCondition>> DAYTIME = RECIPE_CONDITION.register("daytime", () -> new RecipeConditionType<>( DaytimeCondition::new, DaytimeCondition.CODEC));
    // spotless:on
    public static RegistryObject<RecipeConditionType<FTBQuestCondition>> FTB_QUEST;
    public static RegistryObject<RecipeConditionType<GameStageCondition>> GAMESTAGE;
    public static RegistryObject<RecipeConditionType<HeraclesQuestCondition>> HERACLES_QUEST;

    public static void init(IEventBus modBus) {
        RECIPE_CONDITION.register(modBus);
        if (GTCEu.Mods.isFTBQuestsLoaded()) {
            FTB_QUEST = RECIPE_CONDITION.register("ftb_quest", () -> new RecipeConditionType<>( FTBQuestCondition::new, FTBQuestCondition.CODEC));
        }
        if (GTCEu.Mods.isGameStagesLoaded()) {
            GAMESTAGE = RECIPE_CONDITION.register("game_stage", () -> new RecipeConditionType<>(GameStageCondition::new, GameStageCondition.CODEC));
        }
        if (GTCEu.Mods.isHeraclesLoaded()) {
            HERACLES_QUEST = RECIPE_CONDITION.register("heracles_quest", () -> new RecipeConditionType<>( HeraclesQuestCondition::new, HeraclesQuestCondition.CODEC));
        }
        // fix the rock breaker condition's ID
        //GTRegistries.RECIPE_CONDITIONS.remap(GTCEu.id("rock_breaker"), GTCEu.id("adjacent_fluid"));
    }
}
