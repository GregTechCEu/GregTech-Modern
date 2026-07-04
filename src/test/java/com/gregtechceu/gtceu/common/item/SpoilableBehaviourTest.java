package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.item.component.ISpoilableItem;
import com.gregtechceu.gtceu.api.item.component.SpoilContext;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
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
                .inputItems(new ItemStack(Items.JIGSAW))
                .outputItems(new ItemStack(Items.STRUCTURE_BLOCK))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getAdditionHandler().addStaging(LCR_RECIPE_TYPE
                .recipeBuilder(GTCEu.id("test_spoilage_doesnt_transfer"))
                .inputItems(new ItemStack(Items.APPLE))
                .outputItems(new ItemStack(Items.STRUCTURE_BLOCK))
                .EUt(GTValues.V[GTValues.HV])
                .duration(20)
                .keepSpoilingProgress(false)
                .buildRawRecipe());
        LCR_RECIPE_TYPE.getAdditionHandler().completeStaging();
        attachSpoilables();
    }

    private static void attachSpoilables() {
        SpoilableBehavior.builder()
                .ticks(10)
                .result(Items.DIRT)
                .build()
                .attachTo(Items.JIGSAW);
        SpoilableBehavior.builder()
                .ticks(10)
                .result(Items.STRUCTURE_BLOCK)
                .build()
                .attachTo(Items.APPLE);
        SpoilableBehavior.builder()
                .ticks(40)
                .result(Items.STRUCTURE_VOID)
                .build()
                .attachTo(Items.STRUCTURE_BLOCK);
        SpoilableBehavior.builder()
                .ticks(10)
                .result(Items.JIGSAW)
                .build()
                .attachTo(Items.STRUCTURE_VOID);
        SpoilableBehavior.builder()
                .ticks(10)
                .result(Items.DRAGON_EGG)
                .result(EntityType.PIG)
                .multiplyResult(3)
                .build()
                .attachTo(Items.EGG);
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
        TestUtils.succeedAfterTest(helper);
        helper.setBlock(1, 1, 1, Blocks.CHEST);
        IItemHandler itemHandler = TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1));
        itemHandler.insertItem(0, Items.JIGSAW.getDefaultInstance().copyWithCount(23), false);
        TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1));
        helper.runAtTickTime(9, () -> {
            ItemStack stack = TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1)).getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemStackEqual(
                    Items.JIGSAW.getDefaultInstance().copyWithCount(23),
                    stack), "jigsaw spoiled 1 tick earlier");
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have");
            helper.assertTrue(spoilable.shouldSpoil(), "shouldSpoil returned false on spoilable item");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 1,
                    "spoilable didn't return correct ticks until spoiled amount");
            helper.assertTrue(spoilable.getSpoilTicks() == 10,
                    "spoilable didn't return correct total tick amount");
        });
        helper.runAtTickTime(10, () -> helper.assertTrue(TestUtils.isItemStackEqual(
                Items.DIRT.getDefaultInstance().copyWithCount(23),
                TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1)).getStackInSlot(0)),
                "jigsaw didn't spoil when should have"));
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsRecursively(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        helper.setBlock(1, 1, 1, Blocks.CHEST);
        IItemHandler itemHandler = TestUtils.getItemHandler(helper, new BlockPos(1, 1, 1));
        ItemStack in = Items.APPLE.getDefaultInstance().copyWithCount(41);
        SpoilUtils.update(in, new SpoilContext(
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)))
                .withItemHandlerSide(null)
                .withSlot(0));
        itemHandler.insertItem(0, in, false);
        helper.runAtTickTime(70, () -> helper.assertTrue(TestUtils.isItemStackEqual(
                Items.DIRT.getDefaultInstance().copyWithCount(41),
                itemHandler.getStackInSlot(0)),
                "apple didn't spoil recursively (apple -> structure block -> structure void -> jigsaw -> dirt), got " +
                        itemHandler.getStackInSlot(0) + " instead"));
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsInCrate(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);
        crate.inventory.insertItem(0, Items.JIGSAW.getDefaultInstance().copyWithCount(23), false);
        helper.runAtTickTime(9, () -> {
            ItemStack stack = crate.inventory.getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemStackEqual(
                    Items.JIGSAW.getDefaultInstance().copyWithCount(23),
                    stack), "jigsaw spoiled 1 tick earlier");
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have");
            helper.assertTrue(spoilable.shouldSpoil(), "shouldSpoil returned false on spoilable item");
            helper.assertTrue(spoilable.getTicksUntilSpoiled() == 1,
                    "spoilable didn't return correct ticks until spoiled amount");
            helper.assertTrue(spoilable.getSpoilTicks() == 10,
                    "spoilable didn't return correct total tick amount");
        });
        helper.runAtTickTime(10, () -> helper.assertTrue(TestUtils.isItemStackEqual(
                Items.DIRT.getDefaultInstance().copyWithCount(23),
                crate.inventory.getStackInSlot(0)), "jigsaw didn't spoil when should have"));
    }

    @GameTest(template = "empty", batch = "spoilageTests")
    public static void spoilageFreeze(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);
        crate.inventory.insertItem(0, Items.JIGSAW.getDefaultInstance().copyWithCount(23), false);
        helper.runAtTickTime(4, () -> {
            ItemStack stack = crate.inventory.getStackInSlot(0);
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #1)");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 6, "incorrect ticks until spoiled");
            spoilable.freezeSpoiling();
        });
        helper.runAtTickTime(9, () -> {
            ItemStack stack = crate.inventory.getStackInSlot(0);
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #2)");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 6,
                    "ticks until spoiled changed while frozen");
            spoilable.unfreezeSpoiling();
        });
        helper.runAtTickTime(13, () -> {
            ItemStack stack = crate.inventory.getStackInSlot(0);
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null when shouldn't have (check #3)");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 2,
                    "incorrect ticks until spoiled after unfreeze");
        });
    }

    @GameTest(template = "empty", batch = "spoilageTests")
    public static void entitySpoilage(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        CrateMachine crate = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1), GTMachines.STEEL_CRATE);
        crate.inventory.insertItem(0, Items.EGG.getDefaultInstance().copyWithCount(2), false);
        helper.runAtTickTime(10, () -> {
            TestUtils.assertEqual(helper, crate.inventory.getStackInSlot(0),
                    Items.DRAGON_EGG.getDefaultInstance().copyWithCount(6));
            List<Pig> pigs = helper.getLevel().getEntities(EntityType.PIG,
                    new AABB(helper.absolutePos(new BlockPos(1, 0, 1))), Entity::isAlive);
            TestUtils.assertEqual(helper, pigs.size(), 6, "incorrect amount of entities spawned");
        });
    }

    @GameTest(template = "lcr_input_separation", batch = "spoilageTests")
    public static void spoilageTransfersInRecipe(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        BusHolder busHolder = getBussesAndForm(helper);
        ItemStack input = new ItemStack(Items.JIGSAW);
        SpoilUtils.update(input, new SpoilContext());
        Objects.requireNonNull(GTCapabilityHelper.getSpoilable(input)).setTicksUntilSpoiled(8);
        busHolder.inputBus1.getInventory().setStackInSlot(0, input);
        helper.runAtTickTime(21, () -> {
            ItemStack stack = busHolder.outputBus1.getInventory().getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemStackEqual(
                    stack,
                    new ItemStack(Items.STRUCTURE_BLOCK)),
                    "incorrect recipe output (%s != %s)".formatted(stack.toString(),
                            new ItemStack(Items.STRUCTURE_BLOCK).toString()));
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "recipe output was not spoilable");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 27,
                    "recipe output didn't have correct ticks until spoiled");
        });
    }

    @GameTest(template = "lcr_input_separation", batch = "spoilageTests")
    public static void spoilageDoesntTransferInRecipe(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        BusHolder busHolder = getBussesAndForm(helper);
        ItemStack input = new ItemStack(Items.APPLE);
        SpoilUtils.update(input, new SpoilContext());
        Objects.requireNonNull(GTCapabilityHelper.getSpoilable(input)).setTicksUntilSpoiled(8);
        busHolder.inputBus1.getInventory().setStackInSlot(0, input);
        helper.runAtTickTime(21, () -> {
            ItemStack stack = busHolder.outputBus1.getInventory().getStackInSlot(0);
            helper.assertTrue(TestUtils.isItemStackEqual(
                    stack,
                    new ItemStack(Items.STRUCTURE_BLOCK)),
                    "incorrect recipe output (%s != %s)".formatted(stack.toString(),
                            new ItemStack(Items.STRUCTURE_BLOCK).toString()));
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            TestUtils.assertNotNull(helper, spoilable, "recipe output was not spoilable");
            TestUtils.assertEqual(helper, spoilable.getTicksUntilSpoiled(), 40,
                    "recipe output didn't have correct ticks until spoiled");
        });
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void droppedItemSpoils(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        ItemEntity item = helper.spawnItem(Items.JIGSAW, new BlockPos(1, 1, 1));
        helper.runAtTickTime(10, () -> helper.assertTrue(TestUtils.isItemStackEqual(
                item.getItem(),
                Items.DIRT.getDefaultInstance()), "item didn't spoil when dropped"));
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void itemSpoilsInInventory(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        Player player = helper.makeMockPlayer();
        player.getInventory().setItem(0, Items.JIGSAW.getDefaultInstance());
        player.tick();
        helper.runAtTickTime(10, () -> helper.assertTrue(TestUtils.isItemStackEqual(
                player.getInventory().getItem(0),
                Items.DIRT.getDefaultInstance()),
                "item didn't spoil in a player inventory (%s != %s)".formatted(player.getInventory().getItem(0),
                        Items.DIRT.getDefaultInstance())));
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void spoilableFiltering(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        CrateMachine crate1 = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1),
                GTMachines.STEEL_CRATE);
        CrateMachine crate2 = (CrateMachine) TestUtils.setMachine(helper, new BlockPos(1, 2, 1),
                GTMachines.STEEL_CRATE);
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate1, GTItems.CONVEYOR_MODULE_HV.asStack(),
                Direction.UP);
        CompoundTag filterTag = SimpleItemFilter.forItems(true, Items.STRUCTURE_BLOCK.getDefaultInstance())
                .saveFilter();
        ItemStack filter = GTItems.ITEM_FILTER.asStack();
        filter.setTag(filterTag);
        cover.getFilterHandler().loadFilter(filter);
        cover.setWorkingEnabled(false);
        crate1.inventory.setStackInSlot(0, Items.STRUCTURE_BLOCK.getDefaultInstance());
        helper.runAtTickTime(10, () -> cover.setWorkingEnabled(true));
        helper.runAtTickTime(20, () -> {
            ItemStack stack = crate2.inventory.getStackInSlot(0);
            ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(stack);
            helper.assertTrue(TestUtils.isItemStackEqual(stack, Items.STRUCTURE_BLOCK.getDefaultInstance()),
                    "wrong item");
            TestUtils.assertNotNull(helper, spoilable, "spoilable was null");
            TestUtils.assertEqual(helper, 20, spoilable.getTicksUntilSpoiled(), "wrong ticks until spoiled");
        });
    }

    @GameTest(template = "empty_5x5", batch = "spoilageTests")
    public static void spoilableMerging(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        ItemStack stack1 = Items.JIGSAW.getDefaultInstance().copyWithCount(9);
        ItemStack stack2 = Items.JIGSAW.getDefaultInstance().copyWithCount(5);
        SpoilUtils.update(stack1, new SpoilContext());
        SpoilUtils.update(stack2, new SpoilContext());
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
    }
}
