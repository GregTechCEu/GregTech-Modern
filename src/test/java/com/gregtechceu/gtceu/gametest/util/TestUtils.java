package com.gregtechceu.gtceu.gametest.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTRIC;

public class TestUtils {

    // Compares two itemstacks' items and amounts
    // DOES NOT CHECK TAGS OR NBT ETC!
    public static boolean isItemStackEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }

    // Forces a structure check on multiblocks after being placed, to avoid having to wait ticks.
    // Ideally this doesn't need to happen, but it seems not doing this makes the multiblock tests flakey
    public static void formMultiblock(MultiblockControllerMachine controller) {
        controller.getPattern().checkPatternAt(controller.getMultiblockState(), false);
        controller.onStructureFormed();
    }

    // Creates a dummy recipe type that also includes a basic, HV, 1 tick, cobblestone -> stone recipe
    public static GTRecipeType createRecipeTypeAndInsertRecipe(String name) {
        GTRecipeType type = createRecipeType(name);
        type.getLookup().addRecipe(type
                .recipeBuilder(GTCEu.id("test_recipe"))
                .inputItems(new ItemStack(Items.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.V[GTValues.HV])
                .duration(1).buildRawRecipe());
        return type;
    }

    public static GTRecipeType createRecipeType(String name) {
        return createRecipeType(name, 1, 1, 1, 1);
    }

    public static GTRecipeType createRecipeType(String name, int maxInputs, int maxOutputs, int maxFluidInputs,
                                                int maxFluidOutputs) {
        GTRegistries.RECIPE_TYPES.unfreeze();
        GTRegistries.RECIPE_CATEGORIES.unfreeze();
        GTRecipeType type = new GTRecipeType(GTCEu.id(name), ELECTRIC, RecipeType.SMELTING)
                .setEUIO(IO.IN)
                .setMaxIOSize(maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs);

        GTRegistries.RECIPE_CATEGORIES.freeze();
        GTRegistries.RECIPE_TYPES.freeze();
        return type;
    }
}
