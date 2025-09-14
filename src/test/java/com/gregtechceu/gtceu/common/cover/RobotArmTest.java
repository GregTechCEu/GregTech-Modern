package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.gregtechceu.gtceu.common.data.GTItems;
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

import static com.gregtechceu.gtceu.common.data.GTCovers.*;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RobotArmTest {

    public static void setupCrates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.BRONZE_CRATE.getBlock());
    }

    // Test for seeing if robot arm transfers more than keepExact's limit
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void robotArmKeepExactTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        RobotArmCover cover = (RobotArmCover) TestUtils.placeCover(helper, crate2, GTItems.ROBOT_ARM_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2 exactly 7 items
        cover.setIo(IO.IN);
        cover.setTransferMode(TransferMode.KEEP_EXACT);
        cover.setGlobalTransferLimit(7);

        helper.runAtTickTime(20, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 7)),
                    "Conveyor didn't transfer right amount of items");
            helper.succeed();
        });
    }

    // Test for seeing if robot arm transfers correct amount when using transfer exact
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void robotArmTransferExactTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        RobotArmCover cover = (RobotArmCover) TestUtils.placeCover(helper, crate2, GTItems.ROBOT_ARM_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2 exactly 7 items 2 times
        cover.setIo(IO.IN);
        cover.setTransferMode(TransferMode.TRANSFER_EXACT);
        cover.setGlobalTransferLimit(7);

        helper.runAtTickTime(40, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 14)),
                    "Conveyor didn't transfer right amount of items");
            helper.succeed();
        });
    }

    // Test for seeing if robot arm transfers all items when using transfer any
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void robotArmTransferAnyTest(GameTestHelper helper) {
        setupCrates(helper);
        CrateMachine crate1 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.inventory.setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        RobotArmCover cover = (RobotArmCover) TestUtils.placeCover(helper, crate2, GTItems.ROBOT_ARM_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2 all items
        cover.setIo(IO.IN);
        cover.setTransferMode(TransferMode.TRANSFER_ANY);
        cover.setGlobalTransferLimit(13); // arbitrary amount, if the cover works correctly it shouldn't matter

        helper.runAtTickTime(40, () -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.inventory.getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                    "Conveyor didn't transfer right amount of items");
            helper.succeed();
        });
    }
}
