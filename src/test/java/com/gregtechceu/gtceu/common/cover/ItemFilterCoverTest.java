package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.BufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
@ForEachTest(groups = "coverTests")
public class ItemFilterCoverTest {

    private static ItemStack makeDiamondFilter() {
        ItemStack stack = GTItems.ITEM_FILTER.asStack();
        SimpleItemFilter filter = SimpleItemFilter.loadFilter(stack);
        filter.getMatches()[0] = Items.DIAMOND.getDefaultInstance();
        stack.set(GTDataComponents.SIMPLE_ITEM_FILTER, filter);
        return stack;
    }

    public static void setupCrates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
    }

    // Test for seeing if conveyors pass filtered items correctly
    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorTransfersFilteredItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 8));
        crate1.getInventory().setStackInSlot(1, new ItemStack(Items.DIAMOND, 16));
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate2, GTItems.CONVEYOR_MODULE_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);
        // Filter to whitelist diamonds
        ItemFilterCover filterCover = (ItemFilterCover) TestUtils.placeCover(helper, crate1,
                makeDiamondFilter(), Direction.UP);
        filterCover.setFilterMode(FilterMode.FILTER_EXTRACT);

        helper.succeedWhen(() -> {
            TestUtils.assertEqual(helper, crate2.getInventory().getStackInSlot(0), new ItemStack(Items.DIAMOND, 8));
            TestUtils.assertEqual(helper, crate2.getInventory().getStackInSlot(1), ItemStack.EMPTY);
            helper.succeed();
        });
    }

    // Test for seeing if conveyors pass filtered items correctly
    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorDoesntTransferFilteredItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        crate1.getInventory().setStackInSlot(1, new ItemStack(Items.DIAMOND, 16));
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate2, GTItems.CONVEYOR_MODULE_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);
        // Filter to whitelist diamonds
        ItemFilterCover filterCover = (ItemFilterCover) TestUtils.placeCover(helper, crate1, makeDiamondFilter(),
                Direction.UP);
        filterCover.setFilterMode(FilterMode.FILTER_INSERT); // filter is for insert only, so should block transfer

        helper.runAtTickTime(40, () -> {
            TestUtils.assertEqual(helper, crate2.getInventory().getStackInSlot(0), ItemStack.EMPTY);
            TestUtils.assertEqual(helper, crate2.getInventory().getStackInSlot(1), ItemStack.EMPTY);
            helper.succeed();
        });
    }
}
