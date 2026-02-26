package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.storage.BufferMachine;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@SuppressWarnings("DataFlowIssue")
@GameTestHolder(GTCEu.MOD_ID)
@PrefixGameTestTemplate(false)
@ForEachTest(groups = "coverTests")
public class PumpCoverTest {

    public static void setupCrates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
    }

    // Test for seeing if pumps pass fluids
    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void pumpTransfersFluidsTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getFluidHandlerCap(Direction.NORTH, false).setFluidInTank(0, new FluidStack(Fluids.WATER, 1000));
        // LV Cover
        PumpCover cover = (PumpCover) TestUtils.placeCover(helper, crate2, GTItems.ELECTRIC_PUMP_LV.asStack(),
                Direction.DOWN);
        cover.setIo(IO.IN);

        helper.succeedWhen(() -> helper.assertTrue(
                TestUtils.isFluidStackEqual(
                        crate2.getFluidHandlerCap(Direction.NORTH, false).getFluidInTank(0),
                        new FluidStack(Fluids.WATER, 1000)),
                "Pump transferred didn't transfer fluid"));
        TestUtils.succeedAfterTest(helper);
    }

    // Test for seeing if conveyors don't pass items if set to the wrong direction
    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void pumpTransfersFluidsWrongDirectionTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getFluidHandlerCap(Direction.NORTH, false).setFluidInTank(0, new FluidStack(Fluids.WATER, 1000));
        // LV Cover
        PumpCover cover = (PumpCover) TestUtils.placeCover(helper, crate2, GTItems.ELECTRIC_PUMP_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate2 to crate1
        // This shouldn't do anything, as the items fluids in crate1
        cover.setIo(IO.OUT);

        helper.onEachTick(() -> helper.assertFalse(
                TestUtils.isFluidStackEqual(
                        crate2.getFluidHandlerCap(Direction.NORTH, false).getFluidInTank(0),
                        new FluidStack(Fluids.WATER, 1000)),
                "Pump transferred when it shouldn't have"));
        TestUtils.succeedAfterTest(helper);
    }

    // Test for seeing if pumps transfer items
    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void pumpDoesntTransferItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        // LV Cover
        PumpCover cover = (PumpCover) TestUtils.placeCover(helper, crate2, GTItems.ELECTRIC_PUMP_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);

        helper.onEachTick(() -> helper.assertFalse(
                TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(0), new ItemStack(Items.FLINT, 16)),
                "Pump transferred when it shouldn't have"));
        TestUtils.succeedAfterTest(helper);
    }
}
