package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.cover.ConveyorCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.gregtechceu.gtceu.common.data.GTCovers.CONVEYORS;
import static com.gregtechceu.gtceu.common.data.GTCovers.PUMPS;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ConveyorTest {

    public static void setupCrates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.BRONZE_CRATE.getBlock());
    }

    // Test for seeing if conveyors pass items
    @GameTest(template = "empty_5x5")
    public static void conveyorTransfersItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        crate2.getCoverContainer().setCoverAtSide(
                CONVEYORS[0].createCoverBehavior(crate2.getCoverContainer(), Direction.DOWN), Direction.DOWN);
        ConveyorCover cover = (ConveyorCover) crate2.getCoverContainer().getCovers().get(0);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);

        helper.succeedOnTickWhen(2, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Conveyor didn't transfer right amount of items");
            helper.succeed();
        });

        helper.succeed();
    }

    // Test for seeing if conveyors don't pass items if set to the wrong direction
    @GameTest(template = "empty_5x5")
    public static void conveyorTransfersItemsWrongDirectionTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        crate2.getCoverContainer().setCoverAtSide(
                CONVEYORS[0].createCoverBehavior(crate2.getCoverContainer(), Direction.DOWN), Direction.DOWN);
        ConveyorCover cover = (ConveyorCover) crate2.getCoverContainer().getCovers().get(0);
        // Set the cover to import from crate2 to crate1
        // This shouldn't do anything, as the items are in crate1
        cover.setIo(IO.OUT);

        helper.failIfEver(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Conveyor transferred when it shouldn't have");
        });

        helper.succeed();
    }

    // Test for seeing if pumps transfer items
    @GameTest(template = "empty_5x5")
    public static void conveyorPumpDoesntTransferItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        crate2.getCoverContainer().setCoverAtSide(
                PUMPS[0].createCoverBehavior(crate2.getCoverContainer(), Direction.DOWN), Direction.DOWN);
        PumpCover cover = (PumpCover) crate2.getCoverContainer().getCovers().get(0);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);

        helper.failIfEver(() -> {
            helper.assertFalse(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Pump transferred when it shouldn't have");
        });

        helper.succeed();
    }
}
