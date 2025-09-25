package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RecipeLogicTest {

    private static GTRecipeType LCR_RECIPE_TYPE;
    private static GTRecipeType CR_RECIPE_TYPE;

    @BeforeBatch(batch = "RecipeLogic")
    public static void prepare(ServerLevel level) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("recipe_logic_test_lcr", GTRecipeTypes.LARGE_CHEMICAL_RECIPES);
        CR_RECIPE_TYPE = TestUtils.createRecipeType("recipe_logic_test_cr", GTRecipeTypes.CHEMICAL_RECIPES);

        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_recipelogic"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getLookup().addRecipe(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_multiblock_recipelogic_16_items"))
                .inputItems(new ItemStack(Blocks.STONE, 16))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());

        CR_RECIPE_TYPE.getLookup().addRecipe(CR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_singleblock_recipelogic"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .EUt(GTValues.VA[GTValues.HV]).duration(1)
                .buildRawRecipe());
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     *
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static RecipeLogicTest.BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        return new RecipeLogicTest.BusHolder(inputBus1, inputBus2, outputBus1, controller);
    }

    @GameTest(template = "lcr_input_separation", batch = "RecipeLogic")
    public static void recipeLogicMultiBlockTest(GameTestHelper helper) {
        BlockEntity holder = helper.getBlockEntity(new BlockPos(1, 2, 0));

        RecipeLogicTest.BusHolder busHolder = getBussesAndForm(helper);

        helper.assertTrue(busHolder.controller.isFormed(), "Controller didn't form after structure check");
        helper.assertTrue(busHolder.controller.getParts().size() == 6,
                "Controller didn't register all 6 parts after structure check, only registered " +
                        busHolder.controller.getParts().size());

        RecipeLogic recipeLogic = busHolder.controller.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // No recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        // Put an item in the inventory that will trigger recipe recheck
        NotifiableItemStackHandler inputSlots = busHolder.inputBus1.getInventory();
        NotifiableItemStackHandler outputSlots = busHolder.outputBus1.getInventory();

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

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
                TestUtils.isItemStackEqual(outputSlots.getStackInSlot(0),
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
                TestUtils.isItemStackEqual(outputSlots.getStackInSlot(0),
                        new ItemStack(Blocks.STONE, 1)),
                "Wrong stack.");

        // Finish.
        helper.succeed();
    }

    // spotless:off
    // Blocked by LDLib sync issues
    /*
    @GameTest(template = "singleblock_charged_cr", batch = "RecipeLogic")
    public static void recipeLogicSingleBlockTest(GameTestHelper helper) {
        WorkableTieredMachine machine = (WorkableTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));

        machine.setRecipeType(CR_RECIPE_TYPE);
        NotifiableItemStackHandler inputSlots = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler outputSlots = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);


        RecipeLogic recipeLogic = machine.getRecipeLogic();

        recipeLogic.findAndHandleRecipe();

        // no recipe found
        helper.assertFalse(recipeLogic.isActive(), "Recipe logic is active, even when it shouldn't be");
        helper.assertTrue(recipeLogic.getLastRecipe() == null,
                "Recipe logic has somehow found a recipe, when there should be none");

        inputSlots.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        inputSlots.onContentsChanged();

        recipeLogic.findAndHandleRecipe();
        helper.assertFalse(recipeLogic.getLastRecipe() == null,
                "Last recipe is empty, even though recipe logic should've found a recipe.");
        helper.assertTrue(recipeLogic.isActive(), "Recipelogic is inactive, when it should be active.");
        int stackCount = inputSlots.getStackInSlot(0).getCount();
        helper.assertTrue(stackCount == 15, "Count is wrong (should be 15, when it's %s)".formatted(stackCount));

        // Save a reference to the old recipe so we can make sure it's getting reused
        ResourceLocation prev = recipeLogic.getLastRecipe().getId();

        // Finish the recipe, the output should generate, and the next iteration should begin
        recipeLogic.serverTick();
        helper.assertTrue(recipeLogic.getLastRecipe().getId().equals(prev), "lastRecipe is wrong");
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
     */
    // spotless:on

    // Test for putting both ingredients in the same bus in 2 stacks.
    @GameTest(template = "lcr_input_separation", batch = "RecipeLogicTest")
    public static void recipeLogicInTwoStacksTest(GameTestHelper helper) {
        RecipeLogicTest.BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 10));
        busHolder.inputBus1.getInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 6));
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Crafting items in same bus failed, expected STONE but was " +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }
}
