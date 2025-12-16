package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.*;

import net.minecraftforge.fml.ModLoader;

import com.mojang.serialization.Codec;

public final class GTRecipeConditions {

    static {
        GTRegistries.RECIPE_CONDITIONS.unfreeze();
    }

    private GTRecipeConditions() {}

    // spotless:off
    public static final RecipeConditionType<BiomeCondition> BIOME = register("biome", BiomeCondition::new, BiomeCondition.CODEC);
    public static final RecipeConditionType<DimensionCondition> DIMENSION = register("dimension", DimensionCondition::new, DimensionCondition.CODEC);
    public static final RecipeConditionType<PositionYCondition> POSITION_Y = register("pos_y", PositionYCondition::new, PositionYCondition.CODEC);
    public static final RecipeConditionType<RainingCondition> RAINING = register("rain", RainingCondition::new, RainingCondition.CODEC);
    public static final RecipeConditionType<AdjacentFluidCondition> ADJACENT_FLUID = register("adjacent_fluid", AdjacentFluidCondition::new, AdjacentFluidCondition.CODEC);
    public static final RecipeConditionType<AdjacentBlockCondition> ADJACENT_BLOCK = register("adjacent_block", AdjacentBlockCondition::new, AdjacentBlockCondition.CODEC);
    public static final RecipeConditionType<ThunderCondition> THUNDER = register("thunder", ThunderCondition::new, ThunderCondition.CODEC);
    public static final RecipeConditionType<VentCondition> VENT = register("steam_vent", VentCondition::new, VentCondition.CODEC);
    public static final RecipeConditionType<CleanroomCondition> CLEANROOM = register("cleanroom", CleanroomCondition::new, CleanroomCondition.CODEC);
    public static final RecipeConditionType<EUToStartCondition> EU_TO_START = register("eu_to_start", EUToStartCondition::new, EUToStartCondition.CODEC);
    public static final RecipeConditionType<ResearchCondition> RESEARCH = register("research", ResearchCondition::new, ResearchCondition.CODEC);
    public static final RecipeConditionType<EnvironmentalHazardCondition> ENVIRONMENTAL_HAZARD = register("environmental_hazard", EnvironmentalHazardCondition::new, EnvironmentalHazardCondition.CODEC);
    public static final RecipeConditionType<DaytimeCondition> DAYTIME = register("daytime", DaytimeCondition::new, DaytimeCondition.CODEC);
    // spotless:on
    public static RecipeConditionType<FTBQuestCondition> FTB_QUEST;
    public static RecipeConditionType<GameStageCondition> GAMESTAGE;
    public static RecipeConditionType<HeraclesQuestCondition> HERACLES_QUEST;

    public static void init() {
        if (GTCEu.Mods.isFTBQuestsLoaded()) {
            FTB_QUEST = register("ftb_quest", FTBQuestCondition::new, FTBQuestCondition.CODEC);
        }
        if (GTCEu.Mods.isGameStagesLoaded()) {
            GAMESTAGE = register("game_stage", GameStageCondition::new, GameStageCondition.CODEC);
        }
        if (GTCEu.Mods.isHeraclesLoaded()) {
            HERACLES_QUEST = register("heracles_quest", HeraclesQuestCondition::new, HeraclesQuestCondition.CODEC);
        }
        // fix the rock breaker condition's ID
        GTRegistries.RECIPE_CONDITIONS.remap("rock_breaker", "adjacent_fluid");

        // noinspection unchecked
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CONDITIONS,
                (Class<RecipeConditionType<?>>) (Class<?>) RecipeConditionType.class));
        GTRegistries.RECIPE_CONDITIONS.freeze();
    }

    private static <T extends RecipeCondition> RecipeConditionType<T> register(String name,
                                                                               RecipeConditionType.ConditionFactory<T> factory,
                                                                               Codec<T> codec) {
        return GTRegistries.RECIPE_CONDITIONS.register(name, new RecipeConditionType<>(factory, codec));
    }
}
