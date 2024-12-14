package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.*;

import net.minecraftforge.fml.ModLoader;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote GTRecipeConditions
 */
public final class GTRecipeConditions {

    static {
        GTRegistries.RECIPE_CONDITIONS.unfreeze();
    }

    private GTRecipeConditions() {}

    public static final RecipeConditionType<BiomeCondition> BIOME = GTRegistries.RECIPE_CONDITIONS.register("biome",
            new RecipeConditionType<>(BiomeCondition::new, BiomeCondition.CODEC));
    public static final RecipeConditionType<DimensionCondition> DIMENSION = GTRegistries.RECIPE_CONDITIONS
            .register("dimension", new RecipeConditionType<>(DimensionCondition::new, DimensionCondition.CODEC));
    public static final RecipeConditionType<PositionYCondition> POSITION_Y = GTRegistries.RECIPE_CONDITIONS
            .register("pos_y", new RecipeConditionType<>(PositionYCondition::new, PositionYCondition.CODEC));
    public static final RecipeConditionType<RainingCondition> RAINING = GTRegistries.RECIPE_CONDITIONS.register("rain",
            new RecipeConditionType<>(RainingCondition::new, RainingCondition.CODEC));
    public static final RecipeConditionType<RockBreakerCondition> ROCK_BREAKER = GTRegistries.RECIPE_CONDITIONS
            .register("rock_breaker", new RecipeConditionType<>(RockBreakerCondition::new, RockBreakerCondition.CODEC));
    public static final RecipeConditionType<AdjacentBlockCondition> ADJACENT_BLOCK = GTRegistries.RECIPE_CONDITIONS
            .register("adjacent_block",
                    new RecipeConditionType<>(AdjacentBlockCondition::new, AdjacentBlockCondition.CODEC));
    public static final RecipeConditionType<ThunderCondition> THUNDER = GTRegistries.RECIPE_CONDITIONS
            .register("thunder", new RecipeConditionType<>(ThunderCondition::new, ThunderCondition.CODEC));
    public static final RecipeConditionType<VentCondition> VENT = GTRegistries.RECIPE_CONDITIONS.register("steam_vent",
            new RecipeConditionType<>(VentCondition::new, VentCondition.CODEC));
    public static final RecipeConditionType<CleanroomCondition> CLEANROOM = GTRegistries.RECIPE_CONDITIONS
            .register("cleanroom", new RecipeConditionType<>(CleanroomCondition::new, CleanroomCondition.CODEC));
    public static final RecipeConditionType<EUToStartCondition> EU_TO_START = GTRegistries.RECIPE_CONDITIONS
            .register("eu_to_start", new RecipeConditionType<>(EUToStartCondition::new, EUToStartCondition.CODEC));
    public static final RecipeConditionType<ResearchCondition> RESEARCH = GTRegistries.RECIPE_CONDITIONS
            .register("research", new RecipeConditionType<>(ResearchCondition::new, ResearchCondition.CODEC));
    public static final RecipeConditionType<EnvironmentalHazardCondition> ENVIRONMENTAL_HAZARD = GTRegistries.RECIPE_CONDITIONS
            .register("environmental_hazard",
                    new RecipeConditionType<>(EnvironmentalHazardCondition::new, EnvironmentalHazardCondition.CODEC));

    public static void init() {
        // noinspection unchecked
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.RECIPE_CONDITIONS,
                (Class<RecipeConditionType<?>>) (Class<?>) RecipeConditionType.class));
        GTRegistries.RECIPE_CONDITIONS.freeze();
    }
}
