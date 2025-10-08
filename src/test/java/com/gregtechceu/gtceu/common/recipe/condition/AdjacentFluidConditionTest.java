package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.gametest.util.TestUtils.getMetaMachine;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class AdjacentFluidConditionTest {

    private static GTRecipeType ROCK_BREAKER_RECIPE_TYPE;

    @BeforeBatch(batch = "AdjacentFluidCondition")
    public static void prepare(ServerLevel level) {
        ROCK_BREAKER_RECIPE_TYPE = TestUtils.createRecipeType("adjacent_fluid_conditions_tests",
                GTRecipeTypes.ROCK_BREAKER_RECIPES);
        ROCK_BREAKER_RECIPE_TYPE.getLookup().addRecipe(ROCK_BREAKER_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_adjacent_fluid_conditions"))
                .inputItems(new ItemStack(Blocks.COBBLESTONE))
                .outputItems(new ItemStack(Blocks.STONE))
                .adjacentFluidTag(FluidTags.WATER)
                .EUt(GTValues.VA[GTValues.HV])
                .duration(8)
                .buildRawRecipe());

        ROCK_BREAKER_RECIPE_TYPE.getLookup().addRecipe(ROCK_BREAKER_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_adjacent_fluid_conditions_multiple_fluids"))
                .inputItems(new ItemStack(Blocks.OAK_WOOD))
                .outputItems(new ItemStack(Items.CHARCOAL))
                .adjacentFluidTag(FluidTags.WATER, FluidTags.LAVA)
                .EUt(GTValues.VA[GTValues.HV])
                .duration(8)
                .buildRawRecipe());
    }

    // Test for checking if the rock breaker works when the condition is fulfilled
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionCorrectFluidPresentTest(GameTestHelper helper) {
        // Machine is at 1,1,1 so 0,1,1 is next to it
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.WATER);

        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        // 1t to turn on, 8t to run the recipe
        helper.succeedOnTickWhen(9, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    itemOut.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 1)),
                    "Singleblock Rock Breaker didn't run recipe in correct time");
        });
    }

    // Test for checking if the rock breaker works when there are no fluids
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionNoFluidPresentTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        helper.onEachTick(() -> {
            helper.assertTrue(
                    itemOut.isEmpty(),
                    "Singleblock Rock Breaker ran recipe without fluid present");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for checking if the rock breaker works when there is the wrong fluids
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionWrongFluidPresentTest(GameTestHelper helper) {
        // Machine is at 1,1,1 so 0,1,1 is next to it
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.LAVA);
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        // 1t to turn on, 8t to run the recipe
        helper.onEachTick(() -> {
            helper.assertTrue(
                    itemOut.isEmpty(),
                    "Singleblock Rock Breaker ran recipe with wrong fluid present");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for checking if the rock breaker works when two fluids are present
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionTwoFluidCorrectFluidsPresentTest(GameTestHelper helper) {
        // Machine is at 1,1,1 so 0,1,1 and 1,1,0 are next to it
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.LAVA);
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.WATER);
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.OAK_WOOD));
        // 1t to turn on, 8t to run the recipe
        helper.succeedOnTickWhen(9, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    itemOut.getStackInSlot(0),
                    new ItemStack(Items.CHARCOAL, 1)),
                    "Singleblock Rock Breaker didn't run recipe in correct time");
        });
    }

    // Test for checking if the rock breaker works when one of the two fluids are present
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionTwoFluidNr1FluidPresentTest(GameTestHelper helper) {
        // Machine is at 1,1,1 so 0,1,1 and 1,1,0 are next to it
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.WATER);
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.OAK_WOOD));
        helper.onEachTick(() -> {
            helper.assertTrue(
                    itemOut.isEmpty(),
                    "Singleblock Rock Breaker ran with only 1 of 2 fluids present");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for checking if the rock breaker works when one of the two fluids are present
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionTwoFluidNr2FluidPresentTest(GameTestHelper helper) {
        // Machine is at 1,1,1 so 0,1,1 and 1,1,0 are next to it
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.LAVA);
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.OAK_WOOD));
        helper.onEachTick(() -> {
            helper.assertTrue(
                    itemOut.isEmpty(),
                    "Singleblock Rock Breaker ran with only 1 of 2 fluids present");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for checking if the rock breaker works when one of the two fluids are present
    @GameTest(template = "charged_hv_rock_breaker", batch = "AdjacentFluidCondition")
    public static void adjacentFluidConditionTwoFluidNoFluidPresentTest(GameTestHelper helper) {
        SimpleTieredMachine machine = (SimpleTieredMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 1, 1)));

        machine.setRecipeType(ROCK_BREAKER_RECIPE_TYPE);
        NotifiableItemStackHandler itemIn = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler itemOut = (NotifiableItemStackHandler) machine
                .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).get(0);

        itemIn.setStackInSlot(0, new ItemStack(Items.OAK_WOOD));
        helper.onEachTick(() -> {
            helper.assertTrue(
                    itemOut.isEmpty(),
                    "Singleblock Rock Breaker ran with no fluids present");
        });
        TestUtils.succeedAfterTest(helper);
    }
}
