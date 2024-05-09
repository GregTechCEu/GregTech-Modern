package com.gregtechceu.gtceu.data.recipes.misc;

import com.gregtechceu.gtceu.data.machines.GTCreateMachines;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.data.recipes.RecipeOutput;

import static com.gregtechceu.gtceu.data.recipes.CraftingComponent.*;
import static com.gregtechceu.gtceu.data.recipes.misc.MetaTileEntityLoader.registerMachineRecipe;

/**
 * @author KilaBash
 * @date 2023/7/13
 * @implNote CreateRecipeLoader
 */
public class CreateRecipeLoader {
    public static void init(RecipeOutput provider) {
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_MIXER, "GRG", "GEG", "CMC", 'M', HULL, 'R', ROTOR, 'C', AllItems.PRECISION_MECHANISM, 'G', GLASS, 'E', AllBlocks.SHAFT);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_2A, "WMW", "RER", "CHC", 'H', HULL, 'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_8A, "WMW", "RER", "CHC", 'H', HULL, 'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_QUAD, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_16A, "WMW", "RER", "CHC", 'H', HULL, 'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_OCT, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.ELECTRIC_GEAR_BOX_32A, "WMW", "RER", "CHC", 'H', HULL, 'C', CIRCUIT, 'E', AllBlocks.SHAFT.asStack(), 'W', CABLE_HEX, 'M', MOTOR, 'R', ROTOR);
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_INPUT_BOX, " S ", " H ", "   ", 'S', AllBlocks.SHAFT, 'H', HULL);
        registerMachineRecipe(provider, false, GTCreateMachines.KINETIC_OUTPUT_BOX, "   ", " H ", " S ", 'S', AllBlocks.SHAFT, 'H', HULL);
    }
}
