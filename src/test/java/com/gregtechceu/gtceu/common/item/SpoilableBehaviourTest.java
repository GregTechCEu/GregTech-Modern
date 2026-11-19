package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.item.spoilage.ISpoilableItem;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilContext;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilUtils;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Objects;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class SpoilableBehaviourTest {

    private static GTRecipeType LCR_RECIPE_TYPE;

    @BeforeBatch(batch = "spoilageTests")
    public static void prepare(Level ignoredLevel) {
        LCR_RECIPE_TYPE = TestUtils.createRecipeType("spoilage_lcr_tests", GTRecipeTypes.LARGE_CHEMICAL_RECIPES);
        LCR_RECIPE_TYPE.getAdditionHandler().beginStaging();
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_spoilage_transfers"))
                .inputItems(new ItemStack(GTItems.SPOILABLE_4))
                .outputItems(new ItemStack(GTItems.SPOILABLE_2))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_spoilage_doesnt_transfer"))
                .inputItems(new ItemStack(GTItems.SPOILABLE_1.get()))
                .outputItems(new ItemStack(GTItems.SPOILABLE_2))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                .keepSpoilingProgress(false)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getAdditionHandler().completeStaging();
    }

    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) helper.getBlockEntity(new BlockPos(1, 2, 0));
        assert controller != null;
        TestUtils.formMultiblock(helper, controller);
        controller.setRecipeType(LCR_RECIPE_TYPE);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(2, 1, 0));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(2, 2, 0));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) helper.getBlockEntity(new BlockPos(0, 1, 0));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) helper.getBlockEntity(new BlockPos(0, 2, 0));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, controller);
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, WorkableMultiblockMachine controller) {}

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsInChest(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, Blocks.CHEST);
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) TestUtils.getItemHandler(helper,
                new BlockPos(1, 1, 1));

        helper.startSequence()
                .thenExecute(() -> itemHandler.insertItem(0,
                        GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(23), false))
                .thenIdle(9)
                .thenExecute(() -> {
                    ItemStack stack = itemHandler.getStackInSlot(0);
                    helper.assertTrue(TestUtils.isItemStackEqual(
                            GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(23),
                            stack), "item spoiled 1 tick earlier");
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have");
                    helper.assertTrue(spoilable.shouldSpoil(), "shouldSpoil returned false on spoilable item");
                    TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 1,
                            "spoilable didn't return correct ticks until spoiled amount");
                    helper.assertTrue(spoilable.getSpoilTicks() == 10,
                            "spoilable didn't return correct total tick amount");
                })
                .thenIdle(2)
                .thenExecute(() -> helper.assertTrue(TestUtils.isItemStackEqual(
                        GTItems.SPOILABLE_5.get().getDefaultInstance().copyWithCount(23),
                        TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1)).getStackInSlot(0)),
                        "item didn't spoil when should have"))
                .thenSucceed();
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsRecursively(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, Blocks.CHEST);
        IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) TestUtils.getItemHandler(helper,
                new BlockPos(1, 1, 1));

        helper.startSequence()
                .thenExecute(() -> {
                    ItemStack in = GTItems.SPOILABLE_1.get().getDefaultInstance().copyWithCount(41);
                    SpoilUtils.update(in, SpoilContext.EMPTY);
                    itemHandler.insertItem(0, in, false);

                    SpoilUtils.update(in, new SpoilContext(
                            helper.getLevel(),
                            helper.absolutePos(new BlockPos(1, 1, 1)),
                            itemHandler, 0));
                })
                .thenIdle(71)
                .thenExecute(() -> helper.assertTrue(TestUtils.isItemStackEqual(
                        GTItems.SPOILABLE_5.get().getDefaultInstance().copyWithCount(41),
                        itemHandler.getStackInSlot(0)),
                        "item didn't spoil recursively (expected spoilable 1 -> 2 -> ... -> 5), got " +
                                itemHandler.getStackInSlot(0) + " instead"))
                .thenSucceed();
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsInCrate(GameTestHelper helper) {
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);

        helper.startSequence()
                .thenExecute(() -> {
                    var newStack = GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(23);
                    SpoilUtils.update(newStack, SpoilContext.EMPTY);
                    crate.inventory.insertItem(0, newStack, false);
                })
                .thenIdle(9)
                .thenExecute(() -> {
                    ItemStack stack = crate.inventory.getStackInSlot(0);
                    helper.assertTrue(TestUtils.isItemStackEqual(
                            GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(23),
                            stack), "item spoiled 1 tick earlier");
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have");
                    helper.assertTrue(spoilable.shouldSpoil(), "shouldSpoil returned false on spoilable item");
                    helper.assertTrue(spoilable.getTicksUntilSpoiled() == 1,
                            "spoilable didn't return correct ticks until spoiled amount");
                    helper.assertTrue(spoilable.getSpoilTicks() == 10,
                            "spoilable didn't return correct total tick amount");
                })
                .thenIdle(2)
                .thenExecute(() -> helper.assertTrue(TestUtils.isItemStackEqual(
                        GTItems.SPOILABLE_5.get().getDefaultInstance().copyWithCount(23),
                        crate.inventory.getStackInSlot(0)), "item didn't spoil when should have"))
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "spoilageTests")
    public static void spoilageFreeze(GameTestHelper helper) {
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);

        helper.startSequence()
                .thenExecute(() -> {
                    var newStack = GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(23);
                    SpoilUtils.update(newStack, SpoilContext.EMPTY);
                    crate.inventory.insertItem(0, newStack, false);
                })
                .thenIdle(4)
                .thenExecute(() -> {
                    ItemStack stack = crate.inventory.getStackInSlot(0);
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #1)");
                    TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 6, "incorrect ticks until spoiled");
                    spoilable.freezeSpoiling();
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    ItemStack stack = crate.inventory.getStackInSlot(0);
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #2)");
                    TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 6,
                            "ticks until spoiled changed while frozen");
                    spoilable.unfreezeSpoiling();
                })
                .thenIdle(4)
                .thenExecute(() -> {
                    ItemStack stack = crate.inventory.getStackInSlot(0);
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #3)");
                    TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 2,
                            "incorrect ticks until spoiled after unfreeze");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty", batch = "spoilageTests")
    public static void entitySpoilage(GameTestHelper helper) {
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);

        helper.startSequence()
                .thenExecute(() -> crate.inventory.insertItem(0,
                        GTItems.ENTITY_SPOILABLE.get().getDefaultInstance().copyWithCount(2), false))
                .thenIdle(12)
                .thenExecute(() -> {
                    TestUtils.assertEqual(helper, crate.inventory.getStackInSlot(0),
                            Items.DRAGON_EGG.getDefaultInstance().copyWithCount(6));
                    List<Pig> pigs = helper.getLevel().getEntities(EntityType.PIG,
                            new AABB(helper.absolutePos(new BlockPos(1, 0, 1))), Entity::isAlive);
                    TestUtils.assertEqual(helper, pigs.size(), 6, "incorrect amount of entities spawned");
                })
                .thenSucceed();
    }

    @GameTest(template = "lcr_input_separation", batch = "spoilageTests")
    public static void spoilageTransfersInRecipe(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        var inputBus = busHolder.inputBus1;

        helper.startSequence()
                .thenExecute(() -> {
                    ItemStack input = new ItemStack(GTItems.SPOILABLE_4);
                    SpoilUtils.update(input, new SpoilContext(inputBus, inputBus.getInventory(), 0));
                    Objects.requireNonNull(GTCapabilityHelper.getSpoilable(input)).setTicksUntilSpoiled(8);
                    inputBus.getInventory().setStackInSlot(0, input);
                })
                .thenIdle(21)
                .thenExecute(() -> {
                    ItemStack stack = busHolder.outputBus1.getInventory().getStackInSlot(0);
                    helper.assertTrue(TestUtils.isItemStackEqual(
                            stack,
                            new ItemStack(GTItems.SPOILABLE_2)),
                            "incorrect recipe output (%s != %s)".formatted(stack.toString(),
                                    new ItemStack(GTItems.SPOILABLE_2).toString()));
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "recipe output was not spoilable");
                    TestUtils.assertBetween(helper, spoilable.getTicksUntilSpoiled(), 26, 30,
                            "recipe output didn't have correct ticks until spoiled");
                })
                .thenSucceed();
    }

    @GameTest(template = "lcr_input_separation", batch = "spoilageTests")
    public static void spoilageDoesntTransferInRecipe(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        var inputBus = busHolder.inputBus1;

        helper.startSequence()
                .thenExecute(() -> {
                    ItemStack input = new ItemStack(GTItems.SPOILABLE_1.get());
                    SpoilUtils.update(input, new SpoilContext(inputBus, inputBus.getInventory(), 0));
                    Objects.requireNonNull(GTCapabilityHelper.getSpoilable(input)).setTicksUntilSpoiled(8);
                    inputBus.getInventory().setStackInSlot(0, input);
                })
                .thenIdle(21)
                .thenExecute(() -> {
                    ItemStack stack = busHolder.outputBus1.getInventory().getStackInSlot(0);
                    helper.assertTrue(TestUtils.isItemStackEqual(
                            stack,
                            new ItemStack(GTItems.SPOILABLE_2)),
                            "incorrect recipe output (%s != %s)".formatted(stack.toString(),
                                    new ItemStack(GTItems.SPOILABLE_2).toString()));
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    TestUtils.assertNotNull(helper, spoilable, "recipe output was not spoilable");
                    TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 40,
                            "recipe output didn't have correct ticks until spoiled");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void droppedItemSpoils(GameTestHelper helper) {
        ItemEntity item = helper.spawnItem(GTItems.SPOILABLE_4.asItem(), new BlockPos(1, 1, 1));
        SpoilUtils.update(item.getItem(), SpoilContext.EMPTY);

        helper.startSequence()
                .thenIdle(12)
                .thenExecute(() -> helper.assertTrue(TestUtils.isItemStackEqual(
                        item.getItem(),
                        GTItems.SPOILABLE_5.get().getDefaultInstance()), "item didn't spoil when dropped"))
                .thenSucceed();
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void spoilableFiltering(GameTestHelper helper) {
        CrateMachine crate1 = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1),
                GTMachines.STEEL_CRATE);
        CrateMachine crate2 = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 2, 1),
                GTMachines.STEEL_CRATE);
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate1, GTItems.CONVEYOR_MODULE_HV.asStack(),
                Direction.UP);

        helper.startSequence()
                .thenExecute(() -> {
                    ItemStack filter = SimpleItemFilter.forItems(true, GTItems.SPOILABLE_2.get().getDefaultInstance())
                            .getFilterItemStack();

                    cover.setWorkingEnabled(false);
                    cover.getFilterHandler().setFilterItem(filter);

                    var newStack = GTItems.SPOILABLE_2.get().getDefaultInstance();
                    SpoilUtils.update(newStack, SpoilContext.EMPTY);

                    crate1.inventory.setStackInSlot(0, newStack);
                })
                .thenIdle(10)
                .thenExecute(() -> cover.setWorkingEnabled(true))
                .thenIdle(10)
                .thenExecute(() -> {
                    ItemStack stack = crate2.inventory.getStackInSlot(0);
                    ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
                    helper.assertTrue(TestUtils.isItemStackEqual(stack, GTItems.SPOILABLE_2.get().getDefaultInstance()),
                            "wrong item");
                    TestUtils.assertNotNull(helper, spoilable, "spoilable was null");
                    TestUtils.assertEqual(helper, 20, spoilable.getTicksUntilSpoiled(), "wrong ticks until spoiled");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void spoilableMerging(GameTestHelper helper) {
        ItemStack stack1 = GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(9);
        ItemStack stack2 = GTItems.SPOILABLE_4.get().getDefaultInstance().copyWithCount(5);

        ISpoilableItem spoilable1 = GTCapabilityHelper.getSpoilable(stack1);
        ISpoilableItem spoilable2 = GTCapabilityHelper.getSpoilable(stack2);
        TestUtils.assertNotNull(helper, spoilable1, "spoilable1 was null");
        TestUtils.assertNotNull(helper, spoilable2, "spoilable2 was null");
        spoilable1.setTicksUntilSpoiled(7);
        spoilable2.setTicksUntilSpoiled(4);
        helper.assertTrue(ItemHandlerHelper.canItemStacksStack(stack1, stack2), "stacks were not equal");
        // (9*7 + 5*4)/(9 + 5) = 5.928
        TestUtils.assertEqual(helper, spoilable1.getTicksUntilSpoiled(), 5, "spoilable1 had wrong ticks until spoiled");
        TestUtils.assertEqual(helper, spoilable2.getTicksUntilSpoiled(), 5, "spoilable2 has wrong ticks until spoiled");

        helper.succeed();
    }
}
