package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.block.ItemPipeBlock;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.cover.data.DistributionMode;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.storage.BufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ConveyorCoverTest {

    private static BufferMachine placeCrate(GameTestHelper helper, BlockPos pos) {
        return (BufferMachine) TestUtils.setMachine(helper, pos, GTMachines.BUFFER[GTValues.LV]);
    }

    // Test for seeing if conveyors pass items
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorTransfersItemsTest(GameTestHelper helper) {
        BufferMachine crate1 = placeCrate(helper, new BlockPos(0, 1, 0));
        BufferMachine crate2 = placeCrate(helper, new BlockPos(0, 2, 0));
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate2, GTItems.CONVEYOR_MODULE_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Conveyor didn't transfer right amount of items");
        });
    }

    // Test for seeing if conveyors don't pass items if set to the wrong direction
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorTransfersItemsWrongDirectionTest(GameTestHelper helper) {
        BufferMachine crate1 = placeCrate(helper, new BlockPos(0, 1, 0));
        BufferMachine crate2 = placeCrate(helper, new BlockPos(0, 2, 0));
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate2, GTItems.CONVEYOR_MODULE_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate2 to crate1
        // This shouldn't do anything, as the items are in crate1
        cover.setIo(IO.OUT);

        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Conveyor transferred when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for seeing if pumps transfer items
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorPumpDoesntTransferItemsTest(GameTestHelper helper) {
        BufferMachine crate1 = placeCrate(helper, new BlockPos(0, 1, 0));
        BufferMachine crate2 = placeCrate(helper, new BlockPos(0, 2, 0));
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        PumpCover cover = (PumpCover) TestUtils.placeCover(helper, crate2, GTItems.ELECTRIC_PUMP_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);

        helper.onEachTick(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Pump transferred when it shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for conveyor round-robin mode
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorRoundRobinTest(GameTestHelper helper) {
        BufferMachine inputCrate = placeCrate(helper, new BlockPos(0, 1, 0));
        BufferMachine outCrate1 = placeCrate(helper, new BlockPos(0, 2, 0));
        BufferMachine outCrate2 = placeCrate(helper, new BlockPos(0, 3, 0));
        inputCrate.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 32));

        // Item pipes connecting crates
        ItemPipeBlock itemPipeBlock = GTMaterialBlocks.ITEM_PIPE_BLOCKS
                .get(TagPrefix.pipeNormalItem, GTMaterials.Tin).get();
        for (int y = 1; y <= 3; y++) {
            BlockPos pos = new BlockPos(1, y, 0);
            helper.setBlock(pos, itemPipeBlock);
            ItemPipeBlockEntity pipeBlockEntity = (ItemPipeBlockEntity) helper.getBlockEntity(pos);
            pipeBlockEntity.setConnection(Direction.WEST, true, false);
            pipeBlockEntity.setConnection(Direction.UP, true, false);
            pipeBlockEntity.setConnection(Direction.DOWN, true, false);
        }
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, inputCrate,
                GTItems.CONVEYOR_MODULE_LV.asStack(), Direction.EAST);
        // Set cover to push from crate into item pipes, round-robin mode
        cover.setIo(IO.OUT);
        cover.setDistributionMode(DistributionMode.ROUND_ROBIN_GLOBAL);

        helper.succeedWhen(() -> helper.assertTrue(
                TestUtils.isItemStackEqual(outCrate1.getInventory().getStackInSlot(0),
                        new ItemStack(Items.FLINT, 16)) &&
                        TestUtils.isItemStackEqual(outCrate2.getInventory().getStackInSlot(0),
                                new ItemStack(Items.FLINT, 16)),
                "Conveyor didn't split items equally"));
    }
}
