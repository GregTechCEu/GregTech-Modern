package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;

/**
 * Collection of Recipe Modifiers for different machines
 * Makes using them for KJS machines easier
 */
@SuppressWarnings("unused")
public final class MachineModifiers {

    public static RecipeModifier SIMPLE_STEAM = SimpleSteamMachine::recipeModifier;
    public static RecipeModifier SIMPLE_GENERATOR = SimpleGeneratorMachine::recipeModifier;
    public static RecipeModifier STEAM_BOILER = SteamBoilerMachine::recipeModifier;
    public static RecipeModifier LARGE_BOILER = LargeBoilerMachine::recipeModifier;
    public static RecipeModifier LARGE_TURBINE = LargeTurbineMachine::recipeModifier;
    public static RecipeModifier LARGE_COMBUSTION_ENGINE = LargeCombustionEngineMachine::recipeModifier;
    public static RecipeModifier STEAM_PARALLEL_MULTIBLOCK = SteamParallelMultiblockMachine::recipeModifier;
    public static RecipeModifier FUSION_REACTOR = FusionReactorMachine::recipeModifier;
}
