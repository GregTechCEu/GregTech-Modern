package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RecipeLogicTest {

    public static NotifiableItemStackHandler getInputSlot(IRecipeLogicMachine recipeLogicMachine) {
        RecipeHandlerList recipeHandlerList = recipeLogicMachine
                .getCapabilitiesProxy()
                .get(IO.IN)
                .stream()
                .filter(x -> x.hasCapability(ItemRecipeCapability.CAP))
                .toList()
                .get(0);
        NotifiableItemStackHandler itemStackHandler = (NotifiableItemStackHandler) recipeHandlerList
                .getCapability(ItemRecipeCapability.CAP).get(0);
        return itemStackHandler;
    }

    public static NotifiableItemStackHandler getOutputSlot(IRecipeLogicMachine recipeLogicMachine) {
        RecipeHandlerList recipeHandlerList = recipeLogicMachine
                .getCapabilitiesProxy()
                .get(IO.OUT)
                .stream()
                .filter(x -> x.hasCapability(ItemRecipeCapability.CAP))
                .toList()
                .get(0);
        NotifiableItemStackHandler itemStackHandler = (NotifiableItemStackHandler) recipeHandlerList
                .getCapability(ItemRecipeCapability.CAP).get(0);
        return itemStackHandler;
    }

    // This test doesn't work yet, I yield so I can focus on more productive things
    // If you see this, please feel free to give it another shot :)
    // Leaving it in here because it's still a good test to take inspiration from.
    @GameTest(template = "lcr", setupTicks = 20L, timeoutTicks = 200, required = false)
    public static void recipeLogicTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 2, 0));
        if (!(holder instanceof MetaMachineBlockEntity atte)) {
            helper.fail("wrong block at relative pos [1,2,0]!");
            return;
        }
        MetaMachine machine = atte.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        if (!(machine instanceof MultiblockControllerMachine controller)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        controller.onStructureFormed();
        controller.asyncCheckPattern(0);
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(controller.isFormed(), "Controller didn't form after 100 ticks");
            helper.assertTrue(controller.getParts().size() == 4,
                    "Controller didn't register all 4 parts after 100 ticks");

            GTRecipe recipe = GTRecipeBuilder.ofRaw()
                    .id(GTCEu.id("test"))
                    .inputItems(new ItemStack(Blocks.COBBLESTONE))
                    .outputItems(new ItemStack(Blocks.STONE))
                    .EUt(1).duration(1)
                    .buildRawRecipe();
            // force insert the recipe into the manager.
            recipeLogicMachine.getRecipeType().getCategory().addRecipe(recipe);

            RecipeLogic recipeLogic = recipeLogicMachine.getRecipeLogic();

            recipeLogic.findAndHandleRecipe();

            // no recipe found
            helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
            helper.assertTrue(recipeLogic.getLastRecipe() == null,
                    "Recipe logic has somehow found a recipe, when there should be none");

            // put an item in the inventory that will trigger recipe recheck
            NotifiableItemStackHandler inputSlot = getInputSlot(recipeLogicMachine);
            NotifiableItemStackHandler outputSlot = getOutputSlot(recipeLogicMachine);
            inputSlot.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
            inputSlot.onContentsChanged();

            // Inputs change. did we detect it ?
            // helper.assertTrue(recipeLogic.isRecipeDirty(), "Recipe is not dirty after inserting cobblestone in input
            // bus");
            recipeLogic.findAndHandleRecipe();
            helper.assertFalse(recipeLogic.getLastRecipe() == null,
                    "Last recipe is empty, even though recipe logic should've found a recipe.");
            helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
            int stackCount = inputSlot.getStackInSlot(0).getCount();
            helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s".formatted(stackCount));

            // Save a reference to the old recipe so we can make sure it's getting reused
            GTRecipe prev = recipeLogic.getLastRecipe();

            // Finish the recipe, the output should generate, and the next iteration should begin
            recipeLogic.serverTick();
            helper.assertTrue(recipeLogic.getLastRecipe() == prev, "lastRecipe is wrong");
            helper.assertTrue(ItemStack.isSameItem(
                    getOutputSlot(recipeLogicMachine).getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 1)), "wrong output stack.");
            helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");

            // Complete the second iteration, but the machine stops because its output is now full
            outputSlot.setStackInSlot(0,
                    new ItemStack(Blocks.STONE, 63));
            outputSlot.setStackInSlot(1,
                    new ItemStack(Blocks.STONE, 64));
            recipeLogic.serverTick();
            helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");

            // Try to process again and get failed out because of full buffer.
            recipeLogic.serverTick();
            helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");

            // Some room is freed in the output bus, so we can continue now.
            outputSlot.setStackInSlot(1, ItemStack.EMPTY);
            recipeLogic.serverTick();
            // helper.assertTrue(arl.isActive(), "Recipelogic is inactive.");
            helper.assertTrue(ItemStack.isSameItem(outputSlot.getStackInSlot(0), new ItemStack(Blocks.STONE, 1)),
                    "Wrong stack.");

            // Finish.
            helper.succeed();

        });
    }
}
