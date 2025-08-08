package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

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

    @GameTest(template = "lcr")
    public static void recipeLogicMultiBlockTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 2, 0));
        if (!(holder instanceof MetaMachineBlockEntity metaMachineBlockEntity)) {
            helper.fail("wrong block at relative pos [1,2,0]!");
            return;
        }
        MetaMachine machine = metaMachineBlockEntity.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        if (!(machine instanceof MultiblockControllerMachine controller)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }
        TestUtils.formMultiblock(controller);

        helper.assertTrue(controller.isFormed(), "Controller didn't form after structure check");
        helper.assertTrue(controller.getParts().size() == 4,
                "Controller didn't register all 4 parts after structure check");

        // Force insert the recipe into the manager.
        GTRecipeType type = recipeLogicMachine.getRecipeType();
        type.getLookup().removeAllRecipes();
        type.getLookup().addRecipe(type
                .recipeBuilder(GTCEu.id("test-multiblock-recipelogic"))
                .id(GTCEu.id("test-multiblock-recipelogic"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.UV]).duration(1)
                // NBT has a schematic in it with an UV energy input hatch
                .buildRawRecipe());

        RecipeLogic recipeLogic = recipeLogicMachine.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // No recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        // Put an item in the inventory that will trigger recipe recheck
        NotifiableItemStackHandler inputSlots = getInputSlot(recipeLogicMachine);
        NotifiableItemStackHandler outputSlots = getOutputSlot(recipeLogicMachine);

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        inputSlots.onContentsChanged();

        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = recipeLogic.getLastRecipe();

        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().equals(prev), "lastRecipe is wrong");
        helper.assertTrue(
                TestUtils.isItemStackEqual(getOutputSlot(recipeLogicMachine).getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "wrong output stack.");
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");

        // Complete the second iteration, but the machine stops because its output is now full
        // Fill up the recipe with enough stone to complete 1 more recipe and then nothing more.
        outputSlots.setStackInSlot(0,
                new ItemStack(Blocks.STONE, 63));
        for (int i = 1; i < outputSlots.getSlots(); i++) {
            outputSlots.setStackInSlot(i,
                    new ItemStack(Blocks.STONE, 64));
        }
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");

        // Try to process again and get failed out because of full buffer.
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");

        // Some room is freed in the output bus, so we can continue now.
        outputSlots.setStackInSlot(0, ItemStack.EMPTY);
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic didn't start running again");
        recipeLogic.serverTick();
        helper.assertTrue(
                TestUtils.isItemStackEqual(getOutputSlot(recipeLogicMachine).getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");

        // Finish.
        helper.succeed();
    }

    @GameTest(template = "singleblock_chem_reactor")
    public static void recipeLogicSingleBlockTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(0, 1, 0));
        if (!(holder instanceof MetaMachineBlockEntity metaMachineBlockEntity)) {
            helper.fail("wrong block at relative pos [0,1,0]!");
            return;
        }
        MetaMachine machine = metaMachineBlockEntity.getMetaMachine();
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) {
            helper.fail("wrong machine in MetaMachineBlockEntity!");
            return;
        }

        // force insert the recipe into the manager.
        GTRecipeType type = recipeLogicMachine.getRecipeType();
        type.getLookup().removeAllRecipes();
        type.getLookup().addRecipe(type
                .recipeBuilder(GTCEu.id("test-singleblock"))
                .id(GTCEu.id("test-singleblock"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(512).duration(1)
                .buildRawRecipe());

        RecipeLogic recipeLogic = recipeLogicMachine.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // no recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        // put an item in the inventory that will trigger recipe recheck
        NotifiableItemStackHandler inputSlots = getInputSlot(recipeLogicMachine);
        NotifiableItemStackHandler outputSlots = getOutputSlot(recipeLogicMachine);

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        inputSlots.onContentsChanged();

        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        GTRecipe prev = recipeLogic.getLastRecipe();

        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().equals(prev), "lastRecipe is wrong");
        helper.assertTrue(TestUtils.isItemStackEqual(
                outputSlots.getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)),
                "wrong output stack.");
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic is not active, when it should be.");

        // Complete the second iteration, but the machine stops because its output is now full
        outputSlots.setStackInSlot(0,
                new ItemStack(Blocks.STONE, 63));
        outputSlots.setStackInSlot(1,
                new ItemStack(Blocks.STONE, 64));
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "RecipeLogic is active, when it shouldn't be.");

        // Try to process again and get failed out because of full buffer.
        recipeLogic.serverTick();
        helper.assertFalse(recipeLogic.isActive(), "Recipelogic is active, when it shouldn't be.");

        // Some room is freed in the output bus, so we can continue now.
        outputSlots.setStackInSlot(0, ItemStack.EMPTY);
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.isActive(), "RecipeLogic didn't start running again");
        recipeLogic.serverTick();
        helper.assertTrue(
                TestUtils.isItemStackEqual(
                        outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");

        // Finish.
        helper.succeed();
    }
}
