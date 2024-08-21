package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.machines.GTCreateMachines;

import net.minecraft.data.recipes.FinishedRecipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.*;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaBlockEntityLoader.registerMachineRecipe;

/**
 * @author KilaBash
 * @date 2023/7/13
 * @implNote CreateRecipeLoader
 */
public class CreateRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_MIXER, "GRG", "GEG", "CMC", 'M', HULL, 'R',
                ROTOR, 'C', AllItems.PRECISION_MECHANISM, 'G', GLASS, 'E', AllBlocks.SHAFT);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_2A, "WMW", "RER", "CHC", 'H', HULL,
                'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_8A, "WMW", "RER", "CHC", 'H', HULL,
                'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_QUAD, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_16A, "WMW", "RER", "CHC", 'H', HULL,
                'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_OCT, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_32A, "WMW", "RER", "CHC", 'H', HULL,
                'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_HEX, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_INPUT_BOX, " S ", " H ", "   ", 'S',
                AllBlocks.SHAFT, 'H', HULL);
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_OUTPUT_BOX, "   ", " H ", " S ", 'S',
                AllBlocks.SHAFT, 'H', HULL);
    }
}
