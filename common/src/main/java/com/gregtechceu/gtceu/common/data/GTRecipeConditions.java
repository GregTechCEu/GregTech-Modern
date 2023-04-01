package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote GTRecipeConditions
 */
public class GTRecipeConditions {

    public static void init() {
        GTRegistries.RECIPE_CONDITIONS.register(BiomeCondition.INSTANCE.getType(), BiomeCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(DimensionCondition.INSTANCE.getType(), DimensionCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(PositionYCondition.INSTANCE.getType(), PositionYCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(RainingCondition.INSTANCE.getType(), RainingCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(SteamVentCondition.INSTANCE.getType(), SteamVentCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(ThunderCondition.INSTANCE.getType(), ThunderCondition.class);
        GTRegistries.RECIPE_CONDITIONS.register(RockBreakerCondition.INSTANCE.getType(), RockBreakerCondition.class);
        if (GTCEu.isCreateLoaded()) {
            GTRegistries.RECIPE_CONDITIONS.register(RPMCondition.INSTANCE.getType(), RPMCondition.class);
        }
    }

}
