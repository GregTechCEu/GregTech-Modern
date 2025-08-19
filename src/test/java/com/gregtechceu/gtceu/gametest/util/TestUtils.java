package com.gregtechceu.gtceu.gametest.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTRIC;

public class TestUtils {

    // Compares two itemstacks' items and amounts
    // DOES NOT CHECK TAGS OR NBT ETC!
    public static boolean isItemStackEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }

    // Compares two itemstacks and an IntProvider.
    // Checks if items are equal, and if stack2's amount is within the IntProvider's range.
    public static boolean isItemStackWithinRange(ItemStack stack1, ItemStack stack2, IntProvider provider) {
        return ItemStack.isSameItem(stack1, stack2) && stack2.getCount() < provider.getMaxValue() &&
                stack2.getCount() > provider.getMinValue();
    }

    // Compares an itemstack with a number of batched parallels run
    // Returns true if the itemstack is NOT an exact multiple of the batch/parallel count
    // Intended to test if an IntProvider is being rolled correctly for a batch, or if it is returning a single value for all rolls
    // This test can cause false failures from bad luck and should be run twice to reduce the odds of bad luck, and only fail if both fail.
    public static boolean isItemStackExactlyEvenMultiple(ItemStack stack, int batches, int parallels) {
        return  stack.getCount() % (batches * parallels)!= 0;
    }

    // Compares two fluidstacks' items and amounts
    // DOES NOT CHECK TAGS OR NBT ETC!
    public static boolean isFluidStackEqual(FluidStack stack1, FluidStack stack2) {
        return stack1.isFluidEqual(stack2) && stack1.getAmount() == stack2.getAmount();
    }

    // Compares two fluidstacks and an IntProvider.
    // Checks if items are equal, and if stack2's amount is within the IntProvider's range.
    public static boolean isFluidStackWithinRange(FluidStack stack1, FluidStack stack2, IntProvider provider) {
        return stack1.isFluidEqual(stack2) && stack2.getAmount() < provider.getMaxValue() &&
                stack2.getAmount() > provider.getMinValue();
    }

    // Compares an fluidstacks with a number of batched parallels run
    // Returns true if the fluidstack is NOT an exact multiple of the batch/parallel count
    // Intended to test if an IntProvider is being rolled correctly for a batch, or if it is returning a single value for all rolls
    // This test can cause false failures from bad luck and should be run twice to reduce the odds of bad luck, and only fail if both fail.
    public static boolean isFluidStackExactlyEvenMultiple(FluidStack stack, int batches, int parallels) {
        return  stack.getAmount() % (batches * parallels)!= 0;
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
